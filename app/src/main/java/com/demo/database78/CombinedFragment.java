package com.demo.database78;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;

public class CombinedFragment extends Fragment {

    private ListView listView;
    private Button btnRecords, btnSales;
    private DatabaseHelper dbHelper;
    private ArrayList<Student> students;
    private ArrayList<HashMap<String, Object>> salesList;
    private ArrayList<ArrayList<SubRecord>> allSubRecords;
    private StudentAdapter studentAdapter;
    private SalesAdapter salesAdapter;

    // متغيرات لتخزين نتائج البحث
    private ArrayList<Student> filteredStudents;
    private ArrayList<ArrayList<SubRecord>> filteredSubRecords;
    private ArrayList<HashMap<String, Object>> filteredSales;

    // متغير لتخزين آخر استعلام بحث
    private String lastSearchQuery = "";

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_combined, container, false);

        listView = view.findViewById(R.id.listView);
        btnRecords = view.findViewById(R.id.btnRecords);
        btnSales = view.findViewById(R.id.btnSales);
        dbHelper = new DatabaseHelper(getActivity());

        // تعيين النقرات على الأزرار
        btnRecords.setOnClickListener(v -> {
            btnRecords.setSelected(true);
            btnSales.setSelected(false);
            loadRecords();
            applySearchFilter(); // تطبيق البحث المخزن تلقائيًا
        });

        btnSales.setOnClickListener(v -> {
            btnSales.setSelected(true);
            btnRecords.setSelected(false);
            loadSalesData();
            applySearchFilter(); // تطبيق البحث المخزن تلقائيًا
        });

        // تحميل البيانات الافتراضية (Records)
        btnRecords.setSelected(true);
        btnSales.setSelected(false);
        loadRecords();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // عند العودة إلى النشاط، قم بتحميل البيانات مرة أخرى
        if (btnRecords.isSelected()) {
            loadRecords();
        } else if (btnSales.isSelected()) {
            loadSalesData();
        }
    }

    public void filterData(String query) {
        lastSearchQuery = query; // تخزين آخر استعلام بحث
        applySearchFilter(); // تطبيق البحث
    }

    private void applySearchFilter() {
        if (!lastSearchQuery.isEmpty()) {
            if (btnRecords.isSelected()) {
                filterRecords(lastSearchQuery);
                studentAdapter = new StudentAdapter(getActivity(), filteredStudents, filteredSubRecords);
                listView.setAdapter(studentAdapter);
            } else if (btnSales.isSelected()) {
                filterSales(lastSearchQuery);
                salesAdapter = new SalesAdapter(getActivity(), filteredSales);
                listView.setAdapter(salesAdapter);
            }
        } else {
            // إذا لم يكن هناك استعلام بحث، عرض جميع البيانات
            if (btnRecords.isSelected()) {
                studentAdapter = new StudentAdapter(getActivity(), students, allSubRecords);
                listView.setAdapter(studentAdapter);
            } else if (btnSales.isSelected()) {
                salesAdapter = new SalesAdapter(getActivity(), salesList);
                listView.setAdapter(salesAdapter);
            }
        }
    }

    private void filterRecords(String query) {
        filteredStudents = new ArrayList<>();
        filteredSubRecords = new ArrayList<>();

        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            if (student.name.toLowerCase().contains(query.toLowerCase()) ||
                    student.course.toLowerCase().contains(query.toLowerCase()) ||
                    student.fee.toLowerCase().contains(query.toLowerCase())) {
                filteredStudents.add(student);
                filteredSubRecords.add(allSubRecords.get(i));
            }
        }
    }

    private void filterSales(String query) {
        filteredSales = new ArrayList<>();

        for (HashMap<String, Object> sale : salesList) {
            if (sale.get("buyer_name").toString().toLowerCase().contains(query.toLowerCase()) ||
                    sale.get("phone").toString().toLowerCase().contains(query.toLowerCase()) ||
                    sale.get("address").toString().toLowerCase().contains(query.toLowerCase()) ||
                    sale.get("order_info").toString().toLowerCase().contains(query.toLowerCase()) ||
                    sale.get("order_status").toString().toLowerCase().contains(query.toLowerCase())) {
                filteredSales.add(sale);
            }
        }
    }

    @SuppressLint("Range")
    private void loadRecords() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM records", null);
        students = new ArrayList<>();
        allSubRecords = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                Student stu = new Student();
                stu.id = c.getString(c.getColumnIndex("id"));
                stu.name = c.getString(c.getColumnIndex("name"));
                stu.course = c.getString(c.getColumnIndex("course"));
                stu.fee = c.getString(c.getColumnIndex("fee"));

                Cursor subCursor = db.rawQuery("SELECT * FROM sub_records WHERE parent_id = ?", new String[]{stu.id});
                ArrayList<SubRecord> subRecords = new ArrayList<>();

                if (subCursor.moveToFirst()) {
                    do {
                        SubRecord subRecord = new SubRecord();
                        subRecord.id = subCursor.getInt(subCursor.getColumnIndex("id"));
                        subRecord.material = subCursor.getString(subCursor.getColumnIndex("material"));
                        subRecord.quantity = subCursor.getInt(subCursor.getColumnIndex("quantity"));
                        subRecord.unit = subCursor.getString(subCursor.getColumnIndex("unit"));
                        subRecords.add(subRecord);
                    } while (subCursor.moveToNext());
                }
                subCursor.close();

                students.add(stu);
                allSubRecords.add(subRecords);
            } while (c.moveToNext());
        }
        c.close();

        filteredStudents = new ArrayList<>(students);
        filteredSubRecords = new ArrayList<>(allSubRecords);

        applySearchFilter(); // تطبيق البحث المخزن تلقائيًا

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Student stu = filteredStudents.get(position);
            ArrayList<SubRecord> subRecords = filteredSubRecords.get(position);

            Intent i = new Intent(getActivity(), edit.class);
            i.putExtra("id", stu.id);
            i.putExtra("name", stu.name);
            i.putExtra("course", stu.course);
            i.putExtra("fee", stu.fee);
            i.putExtra("subRecords", subRecords);
            startActivity(i);
        });

        btnRecords.setSelected(true);
        btnSales.setSelected(false);
    }

    @SuppressLint("Range")
    private void loadSalesData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM sales", null);
        salesList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, Object> sale = new HashMap<>();
                String salesId = cursor.getString(cursor.getColumnIndex("id"));
                sale.put("id", salesId);
                sale.put("buyer_name", cursor.getString(cursor.getColumnIndex("buyer_name")));
                sale.put("phone", cursor.getString(cursor.getColumnIndex("phone")));
                sale.put("address", cursor.getString(cursor.getColumnIndex("address")));
                sale.put("date", cursor.getString(cursor.getColumnIndex("date")));
                sale.put("order_info", cursor.getString(cursor.getColumnIndex("order_info")));
                sale.put("order_status", cursor.getString(cursor.getColumnIndex("order_status")));
                sale.put("total_amount", cursor.getString(cursor.getColumnIndex("total_amount")));
                sale.put("currency", cursor.getString(cursor.getColumnIndex("currency")));


                Cursor itemsCursor = db.rawQuery("SELECT * FROM sales_items WHERE sales_id = ?", new String[]{salesId});
                ArrayList<HashMap<String, String>> items = new ArrayList<>();

                if (itemsCursor.moveToFirst()) {
                    do {
                        HashMap<String, String> item = new HashMap<>();
                        item.put("id", itemsCursor.getString(itemsCursor.getColumnIndex("id"))); // تم التصحيح هنا
                        item.put("material", itemsCursor.getString(itemsCursor.getColumnIndex("material")));
                        item.put("quantity", itemsCursor.getString(itemsCursor.getColumnIndex("quantity")));
                        item.put("unit", itemsCursor.getString(itemsCursor.getColumnIndex("unit")));
                        items.add(item);
                    } while (itemsCursor.moveToNext());
                }
                itemsCursor.close();

                sale.put("items", items);
                salesList.add(sale);
            } while (cursor.moveToNext());
        }
        cursor.close();

        filteredSales = new ArrayList<>(salesList);

        applySearchFilter(); // تطبيق البحث المخزن تلقائيًا

        listView.setOnItemClickListener((parent, view, position, id) -> {
            HashMap<String, Object> selectedSale = filteredSales.get(position);

            Intent intent = new Intent(getActivity(), SalesEditActivity.class);
            intent.putExtra("id", selectedSale.get("id").toString());
            intent.putExtra("buyer_name", selectedSale.get("buyer_name").toString());
            intent.putExtra("phone", selectedSale.get("phone").toString());
            intent.putExtra("address", selectedSale.get("address").toString());
            intent.putExtra("date", selectedSale.get("date").toString());
            intent.putExtra("order_info", selectedSale.get("order_info").toString());
            intent.putExtra("order_status", selectedSale.get("order_status").toString());
            intent.putExtra("items", (ArrayList<HashMap<String, String>>) selectedSale.get("items"));
            intent.putExtra("total_amount", selectedSale.get("total_amount").toString());
            intent.putExtra("currency", selectedSale.get("currency").toString());

            startActivity(intent);
        });

        btnRecords.setSelected(false);
        btnSales.setSelected(true);
    }
}