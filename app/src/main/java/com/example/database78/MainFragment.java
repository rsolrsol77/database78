
package com.example.database78;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainFragment extends Fragment {

    // تعريف المتغيرات الخاصة بـ MainActivity
    EditText edName, edCourse, edFee, edMaterial, edQuantity;
    Button btnAddRecord, btnAddSubRecord, btnSelectRecord ;
    LinearLayout btnMainRecord ,  btnSubRecord;
    DatabaseHelper dbHelper;
    ViewGroup mainRecordLayout, subRecordLayout;
    ArrayList<String> recordsList;
    String selectedRecord = null; // متغير لتخزين السجل الرئيسي المحدد
    TextView selectedRecordTextView; // لإظهار السجل المختار

    Spinner spinnerUnit;

    int selectedRecordId = -1; // متغير لتخزين الـ ID الخاص بالسجل الرئيسي المحدد



    // الخلفيات الأصلية
    Drawable originalBackgroundName, originalBackgroundCourse, originalBackgroundFee, originalBackgroundMaterial, originalBackgroundQuantity;


    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);



        // تهيئة DatabaseHelper
        dbHelper = new DatabaseHelper(getActivity());




        // ربط المتغيرات


        edName = view.findViewById(R.id.name);
        edCourse = view.findViewById(R.id.course);
        edFee = view.findViewById(R.id.fee);
        edMaterial = view.findViewById(R.id.material);
        edQuantity = view.findViewById(R.id.quantity);
        spinnerUnit = view.findViewById(R.id.spinner_unit);


        btnAddRecord = view.findViewById(R.id.bt1);
        btnAddSubRecord = view.findViewById(R.id.bt_add_sub_record);
        btnMainRecord = view.findViewById(R.id.btn_main_record);
        btnSubRecord = view.findViewById(R.id.btn_sub_record);
        btnSelectRecord = view.findViewById(R.id.btn_select_record);


        mainRecordLayout = view.findViewById(R.id.main_record_layout);
        subRecordLayout = view.findViewById(R.id.sub_record_layout);




        // حفظ الخلفيات الأصلية
        originalBackgroundName = edName.getBackground();
        originalBackgroundCourse = edCourse.getBackground();
        originalBackgroundFee = edFee.getBackground();
        originalBackgroundMaterial = edMaterial.getBackground();
        originalBackgroundQuantity = edQuantity.getBackground();



        // TextView لإظهار السجل المختار
        selectedRecordTextView = view.findViewById(R.id.selected_record_text);




        recordsList = new ArrayList<>();
        loadRecordsIntoList(); // تحميل السجلات في الـ ArrayList







        // إضافة مراقب النصوص للتحقق من الحقول
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateEditTextBackground(edName, originalBackgroundName);
                updateEditTextBackground(edCourse, originalBackgroundCourse);
                updateEditTextBackground(edFee, originalBackgroundFee);
                updateEditTextBackground(edMaterial, originalBackgroundMaterial);
                updateEditTextBackground(edQuantity, originalBackgroundQuantity);
            }
        };

        // إضافة مراقب النصوص للحقول المطلوبة
        edName.addTextChangedListener(textWatcher);
        edCourse.addTextChangedListener(textWatcher);
        edFee.addTextChangedListener(textWatcher);
        edMaterial.addTextChangedListener(textWatcher);
        edQuantity.addTextChangedListener(textWatcher);








        btnAddRecord.setOnClickListener(v -> {
            if (validateMainRecordFields()) {
                insertRecord();
            } else {
                Toast.makeText(getActivity(), "يرجى ملء جميع الحقول المطلوبة", Toast.LENGTH_LONG).show();
            }
        });

        btnAddSubRecord.setOnClickListener(v -> {
            if (validateSubRecordFields()) {
                insertSubRecord();
            } else {
                Toast.makeText(getActivity(), "يرجى ملء جميع الحقول المطلوبة", Toast.LENGTH_LONG).show();
            }
        });

        btnMainRecord.setOnClickListener(v -> {

            mainRecordLayout.setVisibility(View.VISIBLE);
            subRecordLayout.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "إنشاء مستودع", Toast.LENGTH_SHORT).show();
        });


        btnSubRecord.setOnClickListener(v -> {

            mainRecordLayout.setVisibility(View.GONE);
            subRecordLayout.setVisibility(View.VISIBLE);
            Toast.makeText(getActivity(), "إضافة مادة إلى مستودع", Toast.LENGTH_SHORT).show();
        });

        // زر لاختيار السجل الرئيسي من الـ Dialog
        btnSelectRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecordSelectionDialog();
            }
        });



        return view;


    }





    private void updateEditTextBackground(EditText editText, Drawable originalBackground) {
        if (editText.getText().toString().isEmpty()) {
            editText.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.edittext_warning));
        } else {
            editText.setBackground(originalBackground);
        }
    }



    // التحقق من الحقول المطلوبة للسجل الرئيسي
    private boolean validateMainRecordFields() {
        boolean isValid = true;

        if (edName.getText().toString().isEmpty()) {
            edName.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.edittext_warning));
            isValid = false;
        } else {
            edName.setBackground(originalBackgroundName);
        }

        if (edCourse.getText().toString().isEmpty()) {
            edCourse.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.edittext_warning));
            isValid = false;
        } else {
            edCourse.setBackground(originalBackgroundCourse);
        }

        if (edFee.getText().toString().isEmpty()) {
            edFee.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.edittext_warning));
            isValid = false;
        } else {
            edFee.setBackground(originalBackgroundFee);
        }

        return isValid;
    }

    // التحقق من الحقول المطلوبة للسجل الفرعي
    private boolean validateSubRecordFields() {
        boolean isValid = true;

        if (edMaterial.getText().toString().isEmpty()) {
            edMaterial.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.edittext_warning));
            isValid = false;
        } else {
            edMaterial.setBackground(originalBackgroundMaterial);
        }

        if (edQuantity.getText().toString().isEmpty()) {
            edQuantity.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.edittext_warning));
            isValid = false;
        } else {
            edQuantity.setBackground(originalBackgroundQuantity);
        }

        if (selectedRecord == null) {
            Toast.makeText(getActivity(), "الرجاء اختيار المستودع أولاً", Toast.LENGTH_LONG).show();
            isValid = false;
        }

        return isValid;
    }




    // دالة لعرض الـ Dialog مع الـ ListView لاختيار السجل الرئيسي

    private void showRecordSelectionDialog() {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_record_list);
        ListView listView = dialog.findViewById(R.id.listView_records);

        // قائمة لتخزين السجلات مع الـ ID
        ArrayList<String> recordsWithId = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name FROM records", null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
                recordsWithId.add(id + ": " + name); // عرض الـ ID مع الاسم
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, recordsWithId);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedRecordWithId = recordsWithId.get(position);
            String[] parts = selectedRecordWithId.split(": ");
            selectedRecordId = Integer.parseInt(parts[0]); // تخزين الـ ID
            selectedRecord = parts[1]; // تخزين الاسم
            selectedRecordTextView.setText("المستودع المحدد: " + selectedRecord + " (ID: " + selectedRecordId + ")");
            Toast.makeText(getActivity(), "تم اختيار المستودع: " + selectedRecord, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }




    private void logUpdate(String message) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("message", message);
            db.insert("updates", null, values);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "فشل في تسجيل التحديث: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }





    // دالة لإدخال السجل الرئيسي
    public void insertRecord() {
        SQLiteDatabase db = null;
        try {
            String name = edName.getText().toString();
            String course = edCourse.getText().toString();
            String fee = edFee.getText().toString();




            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("course", course);
            values.put("fee", fee);

            long newRowId = db.insert("records", null, values);
            if (newRowId != -1) {
                Toast.makeText(getActivity(), "تم أنشاء المستودع بنجاح", Toast.LENGTH_LONG).show();
                recordsList.add(name); // تحديث قائمة السجلات



                // إضافة الـ ID إلى ContentValues للمزامنة
                ContentValues firebaseValues = new ContentValues();
                firebaseValues.put("id", newRowId);
                firebaseValues.put("name", name);
                firebaseValues.put("course", course);
                firebaseValues.put("fee", fee);



                // إضافة العملية المعلقة
                DatabaseHelper.addPendingOperation(db, "INSERT", "records", String.valueOf(newRowId), firebaseValues);

                // محاولة المزامنة الفورية إذا كان الاتصال متاحًا
                if (isNetworkConnected()) {
                    DatabaseHelper.syncPendingOperations(getActivity());
                }


                // تسجيل التحديث
                String message = "تم إنشاء مستودع جديد باسم " + name;
                logUpdate(message);


            } else {
                Toast.makeText(getActivity(), "فشل في أنشاء المستودع", Toast.LENGTH_LONG).show();
            }

            edName.setText("");
            edCourse.setText("");
            edFee.setText("");
            edName.requestFocus();
        } catch (Exception ex) {
            Toast.makeText(getActivity(), "فشل في أنشاء المستودع " + ex.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (db != null) db.close();
        }
    }

    // دالة لإدخال السجل الفرعي
    public void insertSubRecord() {
        SQLiteDatabase db = null;
        try {
            String material = edMaterial.getText().toString();
            String quantity = edQuantity.getText().toString().trim();
            String selectedUnit = spinnerUnit.getSelectedItem().toString();

            if (selectedRecordId == -1) {
                Toast.makeText(getActivity(), "الرجاء اختيار المستودع أولاً", Toast.LENGTH_LONG).show();
                return;
            }

            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("parent_id", selectedRecordId); // استخدام الـ ID المخزن
            values.put("material", material);
            values.put("quantity", quantity);
            values.put("unit", selectedUnit);

            long newRowId = db.insert("sub_records", null, values);
            if (newRowId != -1) {




                // إضافة العملية المعلقة
                ContentValues firebaseValues = new ContentValues();
                firebaseValues.put("id", newRowId);
                firebaseValues.put("parent_id", selectedRecordId);
                firebaseValues.put("material", material);
                firebaseValues.put("quantity", quantity);
                firebaseValues.put("unit", selectedUnit);




                DatabaseHelper.addPendingOperation(db, "INSERT", "sub_records", String.valueOf(newRowId), firebaseValues);

                // محاولة المزامنة الفورية إذا كان الاتصال متاحًا
                if (isNetworkConnected()) {
                    DatabaseHelper.syncPendingOperations(getActivity());
                }





                // المزامنة مع Firebase باستخدام الـ ID من SQLite
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // <-- إصلاح هنا
                if (user != null && isNetworkConnected()) {
                    DatabaseReference ref = FirebaseDatabase.getInstance()
                            .getReference("users/" + user.getUid() + "/sub_records/" + newRowId);
                    ref.setValue(DatabaseHelper.valuesToMap(values)); // <-- استدعاء صحيح للدالة
                }

                Toast.makeText(getActivity(), "تم إضافة المادة الى المستودع بنجاح", Toast.LENGTH_LONG).show();




                // تسجيل التحديث
                String message = "تم إضافة مادة جديدة: " + material + " (الكمية: " + quantity + " " + selectedUnit + ") إلى السجل " + selectedRecord;
                logUpdate(message);

            } else {
                Toast.makeText(getActivity(), "فشل في إضافة المادة", Toast.LENGTH_LONG).show();
            }

            edMaterial.setText("");
            edQuantity.setText("");
            edMaterial.requestFocus();
        } catch (Exception ex) {
            Toast.makeText(getActivity(), "فشل في إدخال المادة: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (db != null) db.close();
        }
    }



    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }





    // تحميل السجلات الرئيسية في ArrayList
    @SuppressLint("Range")
    private void loadRecordsIntoList() {
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = dbHelper.getReadableDatabase();
            c = db.rawQuery("SELECT * FROM records", null);

            if (c.moveToFirst()) {
                do {
                    recordsList.add(c.getString(c.getColumnIndex("name")));
                } while (c.moveToNext());
            }
        } catch (Exception ex) {
            Toast.makeText(getActivity(), "فشل في تحميل المستودعات: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (c != null) c.close();
            if (db != null) db.close();
        }
    }
}
