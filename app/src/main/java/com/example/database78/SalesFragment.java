package com.example.database78;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;

public class SalesFragment extends Fragment {

    private ListView salesListView;
    private DatabaseHelper dbHelper;
    private ArrayList<HashMap<String, Object>> salesList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sales, container, false);

        salesListView = view.findViewById(R.id.salesListView);
        dbHelper = new DatabaseHelper(getActivity());

        loadSalesData();

        // إضافة مستمع للنقر على العناصر في القائمة
        salesListView.setOnItemClickListener((parent, view1, position, id) -> {
            HashMap<String, Object> selectedSale = salesList.get(position);

            // إنشاء نية (Intent) للانتقال إلى SalesEditActivity
            Intent intent = new Intent(getActivity(), SalesEditActivity.class);
            intent.putExtra("id", selectedSale.get("id").toString());
            intent.putExtra("buyer_name", selectedSale.get("buyer_name").toString());
            intent.putExtra("phone", selectedSale.get("phone").toString());
            intent.putExtra("address", selectedSale.get("address").toString());
            intent.putExtra("date", selectedSale.get("date").toString());
            intent.putExtra("order_info", selectedSale.get("order_info").toString());
            intent.putExtra("order_status", selectedSale.get("order_status").toString());
            intent.putExtra("items", (ArrayList<HashMap<String, String>>) selectedSale.get("items")); // تمرير المواد
            intent.putExtra("total_amount", selectedSale.get("total_amount").toString());
            intent.putExtra("currency", selectedSale.get("currency").toString());

            // بدء النشاط
            startActivity(intent);
        });

        return view;
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

                // Fetch materials related to this sale
                Cursor itemsCursor = db.rawQuery("SELECT * FROM sales_items WHERE sales_id = ?", new String[]{salesId});
                ArrayList<HashMap<String, String>> items = new ArrayList<>();

                if (itemsCursor.moveToFirst()) {
                    do {
                        HashMap<String, String> item = new HashMap<>();
                        item.put("id", itemsCursor.getString(itemsCursor.getColumnIndex("id"))); // إضافة مفتاح العنصر الفريد
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

        SalesAdapter adapter = new SalesAdapter(getActivity(), salesList);
        salesListView.setAdapter(adapter);
    }
}
