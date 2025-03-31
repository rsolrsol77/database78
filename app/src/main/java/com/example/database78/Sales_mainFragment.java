
package com.example.database78;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class Sales_mainFragment extends Fragment {

    // تعريف المتغيرات
    EditText edBuyerName, edPhone, edAddress, edDate, edOrderInfo, edOrderStatus, edTotalAmount;
    Spinner currencySpinner;
    LinearLayout materialsListLayout;
    Button addMaterialButton, btnSave;
    DatabaseHelper dbHelper;
    ArrayList<Material> materialsList;
    ArrayList<String> parentList = new ArrayList<>();
    private int selectedMaterialId = -1;



    // الخلفيات الأصلية للحفاظ على التنسيق الافتراضي
    Drawable originalBackgroundBuyerName, originalBackgroundPhone, originalBackgroundAddress, originalBackgroundDate , originalBackgroundtotalamount;


    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sales_main, container, false);

        // تهيئة قاعدة البيانات
        dbHelper = new DatabaseHelper(getActivity());

        // ربط المتغيرات مع العناصر في واجهة المستخدم
        edBuyerName = view.findViewById(R.id.buyer_name);
        edPhone = view.findViewById(R.id.phone);
        edAddress = view.findViewById(R.id.address);
        edDate = view.findViewById(R.id.date);
        edOrderInfo = view.findViewById(R.id.order_info);
        edOrderStatus = view.findViewById(R.id.order_status);
        edTotalAmount = view.findViewById(R.id.total_amount); // الحقل الجديد للحساب الكلي
        currencySpinner = view.findViewById(R.id.currency_spinner); // قائمة اختيار العملة
        materialsListLayout = view.findViewById(R.id.materials_list_layout);
        addMaterialButton = view.findViewById(R.id.add_material_button);
        btnSave = view.findViewById(R.id.btn_save);


        // حفظ الخلفيات الأصلية
        originalBackgroundBuyerName = edBuyerName.getBackground();
        originalBackgroundPhone = edPhone.getBackground();
        originalBackgroundAddress = edAddress.getBackground();
        originalBackgroundDate = edDate.getBackground();
        originalBackgroundtotalamount = edTotalAmount.getBackground();


        // تهيئة قائمة المواد
        materialsList = new ArrayList<>();

        // تحميل أسماء العناصر الرئيسية من قاعدة البيانات
        loadParentRecords();


        // إضافة مراقب النصوص للتحقق من الحقول
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateEditTextBackground(edBuyerName, originalBackgroundBuyerName);
                updateEditTextBackground(edPhone, originalBackgroundPhone);
                updateEditTextBackground(edAddress, originalBackgroundAddress);
                updateEditTextBackground(edDate, originalBackgroundDate);
                updateEditTextBackground(edTotalAmount, originalBackgroundtotalamount);
            }
        };

        // إضافة مراقب النصوص للحقول المطلوبة
        edBuyerName.addTextChangedListener(textWatcher);
        edPhone.addTextChangedListener(textWatcher);
        edAddress.addTextChangedListener(textWatcher);
        edDate.addTextChangedListener(textWatcher);
        edTotalAmount.addTextChangedListener(textWatcher);




        // حدث عند الضغط على زر "إضافة مادة جديدة"
        addMaterialButton.setOnClickListener(v -> addMaterialInput());



        // حدث عند الضغط على زر الحفظ
        btnSave.setOnClickListener(v -> {
            if (validateFields()) {
                saveSalesAndMaterials();
                updateMaterialQuantities();
                resetFields(); // تفريغ الحقول بعد الحفظ
            } else {
                Toast.makeText(getActivity(), "يرجى ملء جميع الحقول المطلوبة", Toast.LENGTH_LONG).show();
            }
        });



        return view;
    }




    // دالة لإعادة تعيين الحقول إلى حالتها الافتراضية
    private void resetFields() {
        // تفريغ الحقول النصية
        edBuyerName.setText("");
        edPhone.setText("");
        edAddress.setText("");
        edDate.setText("");
        edOrderInfo.setText("");
        edOrderStatus.setText("");
        edTotalAmount.setText("");

        // إعادة اختيار Spinner إلى العنصر الافتراضي
        if (currencySpinner.getAdapter() != null && currencySpinner.getAdapter().getCount() > 0) {
            currencySpinner.setSelection(0);
        }

        // إزالة جميع إدخالات المواد
        materialsListLayout.removeAllViews();
        materialsList.clear();
    }






    // التحقق من الحقول المطلوبة وتلوين الفارغة منها
    private boolean validateFields() {
        boolean isValid = true;

        if (edBuyerName.getText().toString().isEmpty()) {
            edBuyerName.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.edittext_warning));
            isValid = false;
        } else {
            edBuyerName.setBackground(originalBackgroundBuyerName);
        }

        if (edPhone.getText().toString().isEmpty()) {
            edPhone.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.edittext_warning));
            isValid = false;
        } else {
            edPhone.setBackground(originalBackgroundPhone);
        }

        if (edAddress.getText().toString().isEmpty()) {
            edAddress.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.edittext_warning));
            isValid = false;
        } else {
            edAddress.setBackground(originalBackgroundAddress);
        }

        if (edDate.getText().toString().isEmpty()) {
            edDate.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.edittext_warning));
            isValid = false;
        } else {
            edDate.setBackground(originalBackgroundDate);
        }

        if (edTotalAmount.getText().toString().isEmpty()) {
            edTotalAmount.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.edittext_warning));
            isValid = false;
        } else {
            edTotalAmount.setBackground(originalBackgroundtotalamount);
        }

        return isValid;
    }


    private void updateEditTextBackground(EditText editText, Drawable originalBackground) {
        if (editText.getText().toString().isEmpty()) {
            editText.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.edittext_warning));
        } else {
            editText.setBackground(originalBackground);
        }
    }




    private void addMaterialInput() {
        // إضافة إدخال جديد لمادة في الواجهة
        View materialView = getLayoutInflater().inflate(R.layout.material_input, null);
        materialsListLayout.addView(materialView);

        // إنشاء كائن "Material" جديد وإضافته للقائمة
        Material material = new Material();
        materialsList.add(material);

        // ربط المتغيرات في واجهة الإدخال مع حقول إدخال المادة
        EditText materialName = materialView.findViewById(R.id.material_name);
        EditText materialQuantity = materialView.findViewById(R.id.material_quantity);
        TextView unitTextView = materialView.findViewById(R.id.textview_unit);
        ImageView deleteIcon = materialView.findViewById(R.id.delete_material_icon);


        // عند الضغط على حقل المادة يتم عرض قائمة المواد المرتبطة
        materialName.setOnClickListener(v -> showParentSelectionDialog(material, materialName, unitTextView));

        // تحديث كمية المادة
        materialQuantity.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    material.setQuantity(Integer.parseInt(s.toString()));
                }
            }
        });

        // حدث الحذف عند الضغط على الصورة
        deleteIcon.setOnClickListener(v -> {
            materialsListLayout.removeView(materialView);
            materialsList.remove(material);
        });

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






    private void saveSalesAndMaterials() {



        // حفظ بيانات المبيعات في قاعدة البيانات
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues salesValues = new ContentValues();

        salesValues.put("buyer_name", edBuyerName.getText().toString());
        salesValues.put("phone", edPhone.getText().toString());
        salesValues.put("address", edAddress.getText().toString());
        salesValues.put("date", edDate.getText().toString());
        salesValues.put("order_info", edOrderInfo.getText().toString());
        salesValues.put("order_status", edOrderStatus.getText().toString());

        // حفظ الحساب الكلي والعملة
        salesValues.put("total_amount", edTotalAmount.getText().toString());
        salesValues.put("currency", currencySpinner.getSelectedItem().toString());

        long salesId = db.insert("sales", null, salesValues);

        if (salesId != -1) {
            // إذا تم حفظ المبيعات بنجاح، حفظ المواد المرتبطة بالمبيعات
            saveAllMaterials(salesId);

            ContentValues firebaseValues = new ContentValues();
            firebaseValues.putAll(salesValues); // نسخ جميع القيم
            firebaseValues.put("id", salesId);  // إضافة الـ ID الجديد



            // إضافة عملية INSERT لـ sales
            ContentValues firebaseSalesValues = new ContentValues();
            firebaseSalesValues.putAll(salesValues);
            firebaseSalesValues.put("id", salesId);
            DatabaseHelper.addPendingOperation(db, "INSERT", "sales", String.valueOf(salesId), firebaseSalesValues);


            // تسجيل تحديث الحساب الكلي
            String totalAmount = edTotalAmount.getText().toString();
            String currency = currencySpinner.getSelectedItem().toString();
            logTotalAccountUpdate(totalAmount, currency);


            // تسجيل التحديث
            StringBuilder soldItems = new StringBuilder();
            for (Material material : materialsList) {
                soldItems.append(material.getName()).append(" (الكمية: ").append(material.getQuantity()).append(" ").append(material.getUnit()).append("), ");
            }
            String message = "تم بيع المواد التالية: " + soldItems.toString();
            logUpdate(message);


        } else {
            Toast.makeText(getActivity(), "فشل في حفظ المبيعات", Toast.LENGTH_LONG).show();
        }
    }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }



    private void saveAllMaterials(long salesId) {
        // حفظ المواد المرتبطة بالمبيعات في جدول المواد
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        for (Material material : materialsList) {
            ContentValues values = new ContentValues();
            values.put("sales_id", salesId);
            values.put("material", material.getName());
            values.put("quantity", material.getQuantity());
            values.put("unit", material.getUnit());

            // إدراج السجل الفرعي والحصول على الـ ID
            long subRecordId = db.insert("sales_items", null, values);

            if (subRecordId != -1) {
                // إنشاء نسخة جديدة من ContentValues مع الـ ID
                ContentValues firebaseValues = new ContentValues();
                firebaseValues.putAll(values);
                firebaseValues.put("id", subRecordId); // إضافة الـ ID هنا
                DatabaseHelper.addPendingOperation(db, "INSERT", "sales_items", String.valueOf(subRecordId), firebaseValues);

                // المزامنة مع Firebase
                // محاولة المزامنة الفورية إذا كان الاتصال متاحًا
                if (isNetworkConnected()) {
                    DatabaseHelper.syncPendingOperations(getActivity());
                }
            }
        }



        Toast.makeText(getActivity(), "تم حفظ المواد بنجاح", Toast.LENGTH_LONG).show();
    }

    // في Sales_mainFragment.java:
    private void updateMaterialQuantities() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        for (Material material : materialsList) {
            try {
                // 1. تحديث SQLite
                String query = "UPDATE sub_records SET quantity = quantity - ? WHERE id = ?";
                db.execSQL(query, new Object[]{material.getQuantity(), material.getId()});

                // 2. جلب الكمية المحدثة
                Cursor cursor = db.rawQuery(
                        "SELECT quantity FROM sub_records WHERE id = ?",
                        new String[]{String.valueOf(material.getId())}
                );

                int newQuantity = 0;
                if (cursor.moveToFirst()) {
                    newQuantity = cursor.getInt(0);
                }
                cursor.close();

                // 3. تسجيل عملية UPDATE في pending_operations
                ContentValues data = new ContentValues();
                data.put("quantity", newQuantity); // القيمة المطلقة الجديدة
                DatabaseHelper.addPendingOperation(
                        db,
                        "UPDATE",
                        "sub_records",
                        String.valueOf(material.getId()),
                        data
                );

            } catch (Exception e) {
                Log.e("DATABASE_ERROR", "Error: " + e.getMessage());
            }
        }
        db.close();
    }

    private void loadParentRecords() {
        // تحميل السجلات الرئيسية من قاعدة البيانات لعرضها عند اختيار المادة
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT id, name FROM records";
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));

                    parentList.add("ID: " + id + ", عنصر رئيسي: " + name);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DATABASE_ERROR", "Error fetching parent records: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    private void showParentSelectionDialog(Material material, EditText materialNameField, TextView unitTextView) {
        // عرض قائمة للاختيار من السجلات الرئيسية
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("اختر المادة");

        String[] parentArray = parentList.toArray(new String[0]);
        builder.setItems(parentArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String[] parts = parentArray[which].split(", عنصر رئيسي: ");
                int parentId = Integer.parseInt(parts[0].split("ID: ")[1]);

                // بعد اختيار المادة، عرض المواد الفرعية المرتبطة
                showSubRecordsDialog(parentId, material, materialNameField, unitTextView);
            }
        });

        builder.setNegativeButton("إلغاء", null);
        builder.create().show();
    }

    private void showSubRecordsDialog(int parentId, Material material, EditText materialNameField, TextView unitTextView) {
        // عرض قائمة للمواد الفرعية المرتبطة بالمادة المختارة
        ArrayList<String> subRecordsList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT id, material, quantity, unit FROM sub_records WHERE parent_id = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(parentId)});
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                    @SuppressLint("Range") String materialName = cursor.getString(cursor.getColumnIndex("material"));
                    @SuppressLint("Range") String unit = cursor.getString(cursor.getColumnIndex("unit"));
                    @SuppressLint("Range") int quantity = cursor.getInt(cursor.getColumnIndex("quantity"));

                    subRecordsList.add("ID: " + id + ", مادة: " + materialName + ", كمية: " + quantity + ", وحدة: " + unit);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DATABASE_ERROR", "Error fetching sub records: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("اختر المادة");

        String[] subRecordsArray = subRecordsList.toArray(new String[0]);
        builder.setItems(subRecordsArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String[] parts = subRecordsArray[which].split(", مادة: ");
                selectedMaterialId = Integer.parseInt(parts[0].split("ID: ")[1]);
                String materialName = parts[1].split(", كمية: ")[0];
                String unit = parts[1].split(", وحدة: ")[1];

                material.setId(selectedMaterialId); // <-- إضافة هذا السطر

                material.setName(materialName);
                material.setUnit(unit); // تأكد من تعيين الوحدة هنا
                materialNameField.setText(materialName);
                unitTextView.setText(unit);
            }
        });

        builder.setNegativeButton("إلغاء", null);
        builder.create().show();
    }



    // دالة لتسجيل تحديث الحساب الكلي
    private void logTotalAccountUpdate(String totalAmount, String currency) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("total_amount", totalAmount);
            values.put("currency", currency);
            db.insert("total_account_updates", null, values);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "فشل في تسجيل تحديث الحساب الكلي: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (db != null) db.close();
        }
    }




    // كلاس Material لتعريف العناصر المستخدمة
    public class Material {
        private int id; // <-- إضافة هذا الحقل
        private String name;
        private int quantity;
        private String unit;

        // إضافة التوابع الجديدة:
        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getUnit() {
            return unit;
        }
    }

    // كلاس SimpleTextWatcher لسهولة التعامل مع تغييرات النصوص
    public abstract class SimpleTextWatcher implements android.text.TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(android.text.Editable s) {}
    }
}
