package com.example.database78;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class edit extends AppCompatActivity {

    EditText edName, edCourse, edFee;
    TextView edId;
    Button btnUpdate, btnDeleteMain;
    DatabaseHelper dbHelper;
    LinearLayout subRecordsLayout;
    ArrayList<EditText> subMaterialEdits = new ArrayList<>();
    ArrayList<EditText> subQuantityEdits = new ArrayList<>();
    ArrayList<TextView> subRecordIdViews = new ArrayList<>();
    ArrayList<SubRecord> subRecords;
    private ArrayList<Spinner> subUnitSpinners = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        dbHelper = new DatabaseHelper(this);



        // إعداد Toolbar
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // تمكين زر الرجوع
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("تعديل المستودع");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }



        edName = findViewById(R.id.name);
        edCourse = findViewById(R.id.course);
        edFee = findViewById(R.id.fee);
        edId = findViewById(R.id.Id);
        subRecordsLayout = findViewById(R.id.sub_records_layout);

        Intent i = getIntent();
        edId.setText(i.getStringExtra("id"));
        edName.setText(i.getStringExtra("name"));
        edCourse.setText(i.getStringExtra("course"));
        edFee.setText(i.getStringExtra("fee"));

        // جلب بيانات السجلات الفرعية
        subRecords = (ArrayList<SubRecord>) i.getSerializableExtra("subRecords");
        loadSubRecords();

        btnDeleteMain = findViewById(R.id.bt2);  // زر لحذف السجل الرئيسي
        btnUpdate = findViewById(R.id.bt1);

        btnDeleteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteMainRecord();  // حذف السجل الرئيسي
            }
        });



        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();  // تحديث السجلات
            }
        });
    }

    // تحميل السجلات الفرعية وعرضها
    private void loadSubRecords() {
        TableLayout subRecordsTable = new TableLayout(this);
        subRecordsTable.setStretchAllColumns(true);

        // إضافة الصف العلوي للعناوين
        TableRow headerRow = new TableRow(this);
        TextView headerId = new TextView(this);
        headerId.setText("ID");
        TextView headerMaterial = new TextView(this);
        headerMaterial.setText("Material");
        TextView headerQuantity = new TextView(this);
        headerQuantity.setText("Quantity");
        TextView headerUnit = new TextView(this);
        headerUnit.setText("Unit");

        headerRow.addView(headerId);
        headerRow.addView(headerMaterial);
        headerRow.addView(headerQuantity);
        headerRow.addView(headerUnit);
        subRecordsTable.addView(headerRow);

        // عرض السجلات الفرعية
        for (SubRecord subRecord : subRecords) {
            TableRow subRecordRow = new TableRow(this);

            TextView subRecordIdView = new TextView(this);
            subRecordIdView.setText(String.valueOf(subRecord.id));
            subRecordRow.addView(subRecordIdView);
            subRecordIdViews.add(subRecordIdView);

            EditText materialEdit = new EditText(this);
            materialEdit.setText(subRecord.material);
            subMaterialEdits.add(materialEdit);
            subRecordRow.addView(materialEdit);

            EditText quantityEdit = new EditText(this);
            quantityEdit.setText(String.valueOf(subRecord.quantity));
            subQuantityEdits.add(quantityEdit);
            subRecordRow.addView(quantityEdit);

            Spinner unitSpinner = new Spinner(this);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.units_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            unitSpinner.setAdapter(adapter);

            int spinnerPosition = adapter.getPosition(subRecord.unit);
            unitSpinner.setSelection(spinnerPosition);
            subUnitSpinners.add(unitSpinner);
            subRecordRow.addView(unitSpinner);

            // إضافة زر لحذف السجل الفرعي
            Button deleteSubRecordButton = new Button(this);
            deleteSubRecordButton.setText("حذف");
            deleteSubRecordButton.setOnClickListener(v -> {
                deleteSubRecord(subRecord.id);  // حذف السجل الفرعي
                subRecordsLayout.removeView(subRecordRow);  // إزالة الصف من العرض
            });

            subRecordRow.addView(deleteSubRecordButton);
            subRecordsTable.addView(subRecordRow);
        }

        subRecordsLayout.addView(subRecordsTable);
    }

    // دالة لحذف السجل الرئيسي
    public void deleteMainRecord() {
        try {
            String id = edId.getText().toString();
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete("sub_records", "parent_id = ?", new String[]{id});
            db.delete("records", "id = ?", new String[]{id});


            // Firebase Deletion
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                DatabaseReference subRef = FirebaseDatabase.getInstance()
                        .getReference("users/" + user.getUid() + "/sub_records");

                // البحث عن السجلات الفرعية المرتبطة بالسجل الرئيسي
                Query query = subRef.orderByChild("parent_id").equalTo(Integer.parseInt(id));
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // حذف كل سجل فرعي
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ds.getRef().removeValue();
                        }

                        // 2. بعد حذف الفرعية، احذف السجل الرئيسي من Firebase
                        DatabaseReference mainRef = FirebaseDatabase.getInstance()
                                .getReference("users/" + user.getUid() + "/records/" + id);
                        mainRef.removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    // 3. أخيرًا، احذف من SQLite
                                    db.delete("sub_records", "parent_id = ?", new String[]{id});
                                    db.delete("records", "id = ?", new String[]{id});
                                    db.close();

                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firebase", "فشل حذف السجل الرئيسي: " + e.getMessage());
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "خطأ في حذف الفرعية: " + error.getMessage());
                    }
                });
            }


            Toast.makeText(this, "تم حذف السجل الرئيسي بنجاح", Toast.LENGTH_LONG).show();
            edName.setText("");
            edCourse.setText("");
            edFee.setText("");
            edName.requestFocus();
            finish(); // إنهاء النشاط والعودة إلى النشاط السابق
        } catch (Exception ex) {
            Toast.makeText(this, "فشل في حذف السجل الرئيسي", Toast.LENGTH_LONG).show();
        }
    }

    // دالة لحذف السجل الفرعي
    public void deleteSubRecord(int subRecordId) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            int rowsDeleted = db.delete("sub_records", "id = ?", new String[]{String.valueOf(subRecordId)});
            if (rowsDeleted > 0) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    DatabaseReference ref = FirebaseDatabase.getInstance()
                            .getReference("users/" + user.getUid() + "/sub_records/" + subRecordId);
                    ref.removeValue();
                }

                Toast.makeText(this, "تم حذف السجل الفرعي بنجاح", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "فشل في حذف السجل الفرعي", Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, "حدث خطأ أثناء حذف السجل الفرعي: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (db != null) db.close();
        }
    }

    // دالة لتحديث السجل
    public void update() {
        try {
            String id = edId.getText().toString();
            String name = edName.getText().toString();
            String course = edCourse.getText().toString();
            String fee = edFee.getText().toString();

            if (name.isEmpty() || course.isEmpty() || fee.isEmpty()) {
                Toast.makeText(this, "الرجاء ملء جميع الحقول", Toast.LENGTH_LONG).show();
                return;
            }


            // التحقق من الحقول الفرعية
            for (int i = 0; i < subMaterialEdits.size(); i++) {
                String material = subMaterialEdits.get(i).getText().toString();
                String quantity = subQuantityEdits.get(i).getText().toString();
                String unit = subUnitSpinners.get(i).getSelectedItem().toString();

                if (material.isEmpty() || quantity.isEmpty() || unit.isEmpty()) {
                    Toast.makeText(this, "الرجاء ملء جميع الحقول في السجلات الفرعية", Toast.LENGTH_LONG).show();
                    return;
                }

                // التحقق من أن الكمية رقم صحيح
                try {
                    Integer.parseInt(quantity);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "الكمية في السجل الفرعي يجب أن تكون رقمًا صالحًا", Toast.LENGTH_LONG).show();
                    return;
                }
            }





            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("course", course);
            values.put("fee", fee);

            int rowsAffected = db.update("records", values, "id = ?", new String[]{id});
            if (rowsAffected == 0) {
                Toast.makeText(this, "فشل في تحديث السجل الرئيسي", Toast.LENGTH_LONG).show();
                return;
            }


            if (rowsAffected > 0) {
                // مزامنة التغييرات مع Firebase للسجل الرئيسي
                ContentValues mainValues = new ContentValues();
                mainValues.put("name", name);
                mainValues.put("course", course);
                mainValues.put("fee", fee);
                DatabaseHelper.syncUpdateToFirebase("records", id, mainValues);

                // مزامنة التغييرات للسجلات الفرعية
                for (int i = 0; i < subRecords.size(); i++) {
                    ContentValues subValues = new ContentValues();
                    subValues.put("parent_id", id);
                    subValues.put("material", subMaterialEdits.get(i).getText().toString());
                    subValues.put("quantity", Integer.parseInt(subQuantityEdits.get(i).getText().toString()));
                    subValues.put("unit", subUnitSpinners.get(i).getSelectedItem().toString());

                    String subId = subRecordIdViews.get(i).getText().toString();
                    DatabaseHelper.syncUpdateToFirebase("sub_records", subId, subValues);
                }

                Toast.makeText(this, "تم تحديث السجل بنجاح", Toast.LENGTH_LONG).show();
            }




            for (int i = 0; i < subRecords.size(); i++) {
                ContentValues subValues = new ContentValues();
                subValues.put("parent_id", id);
                subValues.put("material", subMaterialEdits.get(i).getText().toString());
                subValues.put("quantity", Integer.parseInt(subQuantityEdits.get(i).getText().toString()));
                subValues.put("unit", subUnitSpinners.get(i).getSelectedItem().toString());

                int subRowsAffected = db.update("sub_records", subValues, "id = ?", new String[]{subRecordIdViews.get(i).getText().toString()});
                if (subRowsAffected == 0) {
                    Toast.makeText(this, "فشل في تحديث السجل الفرعي ID: " + subRecordIdViews.get(i).getText().toString(), Toast.LENGTH_LONG).show();
                }
            }

            Toast.makeText(this, "تم تحديث السجل بنجاح", Toast.LENGTH_LONG).show();
            finish(); // إنهاء النشاط والعودة إلى النشاط السابق
        } catch (Exception ex) {
            Log.e("UPDATE_ERROR", "Error updating record: ", ex);
            Toast.makeText(this, "فشل في تحديث السجل: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }



    @Override
    public boolean onSupportNavigateUp() {
        // معالجة الضغط على زر الرجوع
        finish(); // العودة إلى النشاط السابق
        return true;
    }

}
