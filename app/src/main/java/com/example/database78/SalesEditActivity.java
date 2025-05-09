
package com.example.database78;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SalesEditActivity extends AppCompatActivity {

    EditText edBuyerName, edPhone, edAddress, edDate, edOrderInfo, edOrderStatus, edTotalAmount;
    TextView tvCurrency, tvId; // لإظهار العملة
    LinearLayout subRecordsLayout;
    Button btnUpdate, btnDelete;
    DatabaseHelper dbHelper;
    String salesId;
    ArrayList<TextView> subMaterialViews = new ArrayList<>();
    ArrayList<TextView> subQuantityViews = new ArrayList<>();
    ArrayList<TextView> subUnitViews = new ArrayList<>();  // قائمة للوحدات (Unit) كـ TextView

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_edit);

        dbHelper = new DatabaseHelper(this);

        // إعداد Toolbar
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // تمكين زر الرجوع
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("تعديل قائمة البيع");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        // ربط المتغيرات مع الحقول في واجهة المستخدم
        tvId = findViewById(R.id.tv_id);
        edBuyerName = findViewById(R.id.buyer_name);
        edPhone = findViewById(R.id.phone);
        edAddress = findViewById(R.id.address);
        edDate = findViewById(R.id.date);
        edOrderInfo = findViewById(R.id.order_info);
        edOrderStatus = findViewById(R.id.order_status);
        edTotalAmount = findViewById(R.id.total_amount); // ربط الحقل الجديد للحساب الكلي
        tvCurrency = findViewById(R.id.currency); // لعرض العملة
        subRecordsLayout = findViewById(R.id.sub_records_layout);
        btnUpdate = findViewById(R.id.btn_update);
        btnDelete = findViewById(R.id.btn_delete);

        // استقبال البيانات من النشاط السابق
        Intent intent = getIntent();
        salesId = intent.getStringExtra("id");
        tvId.setText(salesId);
        edBuyerName.setText(intent.getStringExtra("buyer_name"));
        edPhone.setText(intent.getStringExtra("phone"));
        edAddress.setText(intent.getStringExtra("address"));
        edDate.setText(intent.getStringExtra("date"));
        edOrderInfo.setText(intent.getStringExtra("order_info"));
        edOrderStatus.setText(intent.getStringExtra("order_status"));
        edTotalAmount.setText(intent.getStringExtra("total_amount")); // تعيين الحساب الكلي
        tvCurrency.setText(intent.getStringExtra("currency")); // تعيين العملة

        // تحميل السجلات الفرعية (العناصر المرتبطة بالمبيعات)
        loadSubRecords();

        // تحديث السجل عند الضغط على زر التحديث
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSalesRecord();
            }
        });

        // حذف السجل عند الضغط على زر الحذف
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSalesRecord();
            }
        });
    }

    // دالة لتحميل السجلات الفرعية
    @SuppressLint("Range")
    private void loadSubRecords() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM sales_items WHERE sales_id = ?", new String[]{salesId});


        // تفريغ القوائم قبل التحميل
        subRecordsLayout.removeAllViews();
        subMaterialViews.clear();
        subQuantityViews.clear();
        subUnitViews.clear();


        subRecordsLayout.removeAllViews(); // مسح السجلات الفرعية القديمة

        while (cursor.moveToNext()) {
            // إنشاء حقول الإدخال للعناصر المرتبطة
            View subRecordView = getLayoutInflater().inflate(R.layout.sub_record_item, null);

            TextView tvId = subRecordView.findViewById(R.id.tv_id);
            TextView tvMaterial = subRecordView.findViewById(R.id.tv_material);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView tvQuantity = subRecordView.findViewById(R.id.tv_quantity);
            TextView tvUnit = subRecordView.findViewById(R.id.tv_unit);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView btnIncrease = subRecordView.findViewById(R.id.btn_increase);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView btnDecrease = subRecordView.findViewById(R.id.btn_decrease);
            ImageView btnDelete = subRecordView.findViewById(R.id.btn_delete); // زر الحذف

            // تعيين القيم النصية
            String subRecordId = cursor.getString(cursor.getColumnIndex("id"));

            tvId.setText(cursor.getString(cursor.getColumnIndex("id"))); // عرض المعرف
            tvMaterial.setText(cursor.getString(cursor.getColumnIndex("material")));
            tvQuantity.setText(cursor.getString(cursor.getColumnIndex("quantity")));
            tvUnit.setText(cursor.getString(cursor.getColumnIndex("unit")));

            // تخزين الكمية الحالية كقيمة رقمية
            final int[] currentQuantity = {cursor.getInt(cursor.getColumnIndex("quantity"))};

            // معالجة زر الزيادة
            btnIncrease.setOnClickListener(v -> {
                currentQuantity[0]++;
                tvQuantity.setText(String.valueOf(currentQuantity[0]));  // تحديث عرض الكمية
            });

            // معالجة زر النقصان
            btnDecrease.setOnClickListener(v -> {
                if (currentQuantity[0] > 1) {  // الحد الأدنى هو 1
                    currentQuantity[0]--;
                    tvQuantity.setText(String.valueOf(currentQuantity[0]));  // تحديث عرض الكمية
                }
            });

            // إضافة مستمع لزر الحذف
            btnDelete.setOnClickListener(v -> {
                deleteSubRecord(subRecordId); // استدعاء دالة الحذف
                loadSubRecords(); // إعادة تحميل السجلات لتحديث العرض
            });

            subMaterialViews.add(tvMaterial);
            subQuantityViews.add(tvQuantity);
            subUnitViews.add(tvUnit);

            subRecordsLayout.addView(subRecordView);  // إضافة العنصر إلى التخطيط
        }

        cursor.close();
    }

    private void deleteSubRecord(String subRecordId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // حذف السجل الفرعي من جدول sales_items
        int rowsAffected = db.delete("sales_items", "id = ?", new String[]{subRecordId});


        // إضافة عملية DELETE المعلقة
        ContentValues data = new ContentValues();
        data.put("sales_id", salesId); // بيانات إضافية
        DatabaseHelper.addPendingOperation(db, "DELETE", "sales_items", subRecordId, data);

        // محاولة المزامنة الفورية
        if (isNetworkConnected()) {
            DatabaseHelper.syncPendingOperations(this);
        }


        // حذف من Firebase
        if (isNetworkConnected()) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("users/" + user.getUid() + "/sales_items/" + subRecordId);
                ref.removeValue();
            }
        }

        if (rowsAffected > 0) {
            Toast.makeText(this,  R.string.the_sup_record_was_deleted_successfully, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.failed_to_delete_sub_record, Toast.LENGTH_SHORT).show();
        }
    }

    // دالة للتحقق من ملء جميع الحقول المطلوبة
    private boolean areRequiredFieldsFilled() {
        return !edBuyerName.getText().toString().trim().isEmpty() &&
                !edPhone.getText().toString().trim().isEmpty() &&
                !edAddress.getText().toString().trim().isEmpty() &&
                !edDate.getText().toString().trim().isEmpty() &&
                !edTotalAmount.getText().toString().trim().isEmpty();
    }

    // دالة لتحديث السجل الرئيسي والفرعي
    private void updateSalesRecord() {
        // التحقق من الحقول المطلوبة
        if (!areRequiredFieldsFilled()) {
            Toast.makeText(this,  R.string.please_fill_in_all_required_fields, Toast.LENGTH_SHORT).show();
            return; // إيقاف عملية التحديث
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // تحديث السجل الرئيسي
        values.put("buyer_name", edBuyerName.getText().toString());
        values.put("phone", edPhone.getText().toString());
        values.put("address", edAddress.getText().toString());
        values.put("date", edDate.getText().toString());
        values.put("order_info", edOrderInfo.getText().toString());
        values.put("order_status", edOrderStatus.getText().toString());
        values.put("total_amount", edTotalAmount.getText().toString()); // تحديث الحساب الكلي

        int rowsAffected = db.update("sales", values, "id = ?", new String[]{salesId});

        boolean quantityChanged = false;  // متغير لتحديد ما إذا كانت الكمية قد تغيرت

        if (rowsAffected > 0) {


            // مزامنة السجل الرئيسي مع Firebase
            ContentValues firebaseValues = new ContentValues();
            firebaseValues.putAll(values);
            firebaseValues.put("id", salesId); // إضافة الـ ID

            // إضافة عملية UPDATE المعلقة
            DatabaseHelper.addPendingOperation(db, "UPDATE", "sales", salesId, firebaseValues);

            // محاولة المزامنة الفورية
            if (isNetworkConnected()) {
                DatabaseHelper.syncPendingOperations(this);
            }



            // تحديث السجلات الفرعية
            if (!subMaterialViews.isEmpty()) {
                for (int i = 0; i < subMaterialViews.size(); i++) {
                    String oldQuantity = getOldQuantityFromDatabase(subMaterialViews.get(i).getText().toString());  // جلب الكمية القديمة من قاعدة البيانات
                    String newQuantity = subQuantityViews.get(i).getText().toString();




                    if (!oldQuantity.equals(newQuantity)) {
                        quantityChanged = true;  // إذا كانت الكمية مختلفة، تعيين المتغير
                    }


                    // الحصول على subRecordId بشكل آمن
                    View subRecordView = subRecordsLayout.getChildAt(i);
                    if (subRecordView == null) continue; // تخطي إذا كانت العناصر غير موجودة


                    // 1. الحصول على subRecordId من TextView الموجود في الواجهة
                    TextView tvSubId = (TextView) ((ViewGroup) subRecordsLayout.getChildAt(i)).findViewById(R.id.tv_id);
                    String subRecordId = tvSubId.getText().toString(); // <-- تعريف subRecordId


                    ContentValues subValues = new ContentValues();
                    subValues.put("material", subMaterialViews.get(i).getText().toString());
                    subValues.put("quantity", newQuantity);
                    subValues.put("unit", subUnitViews.get(i).getText().toString());  // حفظ الوحدة من TextView
                    subValues.put("sales_id", salesId); // إضافة sales_id


                    db.update("sales_items", subValues, "sales_id = ? AND material = ?",
                            new String[]{salesId, subMaterialViews.get(i).getText().toString()});



                    // 3. مزامنة Firebase
                    if (isNetworkConnected()) {
                        ContentValues firebaseSubValues = new ContentValues();
                        firebaseSubValues.putAll(subValues);
                        firebaseSubValues.put("id", subRecordId); // إضافة الـ ID
                        DatabaseHelper.syncUpdateToFirebase("sales_items", subRecordId, firebaseSubValues);
                    }
                }
            }







            Toast.makeText(this,  R.string.the_record_was_updated_successfully, Toast.LENGTH_SHORT).show();

            // عرض الرسالة المنبثقة للتذكير فقط إذا تغيرت الكمية
            if (quantityChanged) {
                showReminderDialog();
            } else {
                finish(); // إنهاء النشاط إذا لم تتغير الكمية
            }

        } else {
            Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
        }
    }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }



    // دالة لجلب الكمية القديمة من قاعدة البيانات
    @SuppressLint("Range")
    private String getOldQuantityFromDatabase(String material) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT quantity FROM sales_items WHERE sales_id = ? AND material = ?",
                new String[]{salesId, material}
        );

        String oldQuantity = "";
        if (cursor.moveToFirst()) {
            oldQuantity = cursor.getString(cursor.getColumnIndex("quantity"));
        }
        cursor.close();
        return oldQuantity;
    }

    // دالة لعرض الرسالة المنبثقة للتذكير
    private void showReminderDialog() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean dontShowAgain = prefs.getBoolean("dont_show_again", false);

        if (!dontShowAgain) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.quantities_in_warehouses_are_not_updated_when_modified_please_update_them_manually)
                    .setCancelable(false)
                    .setPositiveButton(R.string.understood, (dialog, id) -> {
                        dialog.dismiss();
                        finish(); // إنهاء النشاط بعد الضغط على "فهمت"
                    })
                    .setNegativeButton(R.string.do_not_show_again, (dialog, id) -> {
                        // حفظ حالة "عدم العرض مرة أخرى"
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("dont_show_again", true);
                        editor.apply();
                        dialog.dismiss();
                        finish(); // إنهاء النشاط بعد الضغط على "عدم العرض مرة أخرى"
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            finish(); // إنهاء النشاط إذا كانت الرسالة لا تُعرض
        }
    }

    // دالة لحذف السجل الرئيسي والفرعي
    private void deleteSalesRecord() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction(); // بدء Transaction لضمان التسجيل الذري

        try {
            // 1. جلب جميع السجلات الفرعية المرتبطة
            Cursor subCursor = db.rawQuery(
                    "SELECT id FROM sales_items WHERE sales_id = ?",
                    new String[]{salesId}
            );

            // 2. تسجيل عمليات DELETE للسجلات الفرعية
            while (subCursor.moveToNext()) {
                @SuppressLint("Range") String subId = subCursor.getString(subCursor.getColumnIndex("id"));
                DatabaseHelper.addPendingOperation(
                        db,
                        "DELETE",
                        "sales_items",
                        subId,
                        new ContentValues() // يمكن إضافة بيانات إضافية إذا لزم الأمر
                );
            }
            subCursor.close();

            // 3. تسجيل عملية DELETE للسجل الرئيسي
            DatabaseHelper.addPendingOperation(
                    db,
                    "DELETE",
                    "sales",
                    salesId,
                    new ContentValues()
            );

            // 4. حذف البيانات محليًا بعد التسجيل
            db.delete("sales", "id = ?", new String[]{salesId});
            db.delete("sales_items", "sales_id = ?", new String[]{salesId});

            db.setTransactionSuccessful(); // تأكيد Transaction
        } catch (Exception e) {
            Log.e("DATABASE_ERROR", "Error deleting sales record: " + e.getMessage());
        } finally {
            db.endTransaction();
        }

        // 5. محاولة المزامنة الفورية إذا كان الاتصال متاحًا
        if (isNetworkConnected()) {
            DatabaseHelper.syncPendingOperations(this);
        }

        Toast.makeText(this, R.string.the_master_record_was_deleted_successfully, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // معالجة الضغط على زر الرجوع
        finish(); // العودة إلى النشاط السابق
        return true;
    }
}
