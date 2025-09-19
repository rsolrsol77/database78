
package com.demo.database78;

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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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


    //admob
    private InterstitialAd mInterstitialAd;
    private final String AD_UNIT_ID = "ca-app-pub-9825698675981083/4859732947";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);





        // ========== بدء عملية التحقق من حالة المستخدم ==========
        // 1. التحقق الأولي من حالة المستخدم المميز المخزنة محلياً
        checkUserPremiumStatus();










    }









    // ========== نظام إدارة المستخدم المميز والإعلانات ==========

    /**
     * التحقق من حالة المستخدم المميز المخزنة محلياً
     * - إذا كان المستخدم مميزاً: تخطي الإعلانات وتهيئة الواجهة مباشرة
     * - إذا كان مستخدماً عادياً: التحقق المباشر من Firestore للتأكد من الحالة
     */
    private void checkUserPremiumStatus() {
        // التحقق من الحالة المخزنة في SharedPreferences
        boolean isPremiumUser = MainActivity.isUserPremium(this);

        if (isPremiumUser) {
            Log.d("AdSystem", "المستخدم مميز (محلياً) - تخطي الإعلانات");
            initializeUI(); // تهيئة الواجهة دون إعلان
        } else {
            Log.d("AdSystem", "المستخدم عادي (محلياً) - التحقق من Firestore");
            verifyPremiumStatusWithFirestore(); // التحقق المباشر من Firestore
        }
    }

    /**
     * التحقق المباشر من حالة المستخدم في Firestore
     * - يضمن تحديث الحالة حتى لو تم تغييرها مؤخراً
     * - يعالج حالات عدم التزامن بين التطبيق والخادم
     */
    private void verifyPremiumStatusWithFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d("AdSystem", "المستخدم غير مسجل - تحميل الإعلان");
            loadAd(); // تحميل الإعلان للزوار
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("premium_users").document(user.getUid());

        // التحقق المباشر من حالة المستخدم في Firestore
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                // المستخدم مميز في Firestore - تحديث الحالة محلياً
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                prefs.edit().putBoolean("is_premium", true).apply();

                Log.d("AdSystem", "التحقق المباشر: المستخدم مميز - تخطي الإعلانات");
                initializeUI(); // تهيئة الواجهة دون إعلان
            } else {
                Log.d("AdSystem", "التحقق المباشر: المستخدم عادي - تحميل الإعلان");
                loadAd(); // تحميل الإعلان للمستخدم العادي
            }
        });
    }

    /**
     * تحميل الإعلان بين الصفحات
     * - يتم تنفيذه فقط للمستخدمين العاديين
     * - تهيئة الواجهة بعد نجاح أو فشل تحميل الإعلان
     */
    private void loadAd() {
        Log.d("AdSystem", "بدء تحميل الإعلان...");
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, AD_UNIT_ID, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.d("AdSystem", "تم تحميل الإعلان بنجاح");
                        showInterstitial(); // عرض الإعلان
                        initializeUI(); // تهيئة الواجهة بعد تحميل الإعلان
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.e("AdSystem", "فشل تحميل الإعلان: " + loadAdError.getMessage());
                        initializeUI(); // تهيئة الواجهة حتى في حالة فشل الإعلان
                    }
                });
    }

    /**
     * عرض الإعلان للمستخدمين العاديين
     * - يتم عرضه كاملاً بين الصفحات
     * - لا يؤثر على تجربة المستخدم بعد إغلاقه
     */
    private void showInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    Log.d("AdSystem", "تم إغلاق الإعلان - متابعة الاستخدام");
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    Log.e("AdSystem", "فشل عرض الإعلان: " + adError.getMessage());
                }
            });
            mInterstitialAd.show(this);
        } else {
            Log.d("AdSystem", "لم يتم تحميل الإعلان - لا يمكن عرضه");
        }
    }

    /**
     * تهيئة واجهة المستخدم وعناصرها
     * - تنفيذها بعد التحقق من حالة المستخدم وقرار الإعلان
     * - تضمن أن الواجهة جاهزة للاستخدام
     */
    private void initializeUI() {
        Log.d("AdSystem", "تهيئة واجهة المستخدم...");

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
    // ========== نهاية نظام إدارة الإعلانات ==========
















    // تحميل السجلات الفرعية وعرضها
    private void loadSubRecords() {
        subRecordsLayout.removeAllViews(); // تأكد من تفريغ أي عرض سابق
        LayoutInflater inflater = LayoutInflater.from(this);

        // إعداد الـ Spinner Adapter لمرة واحدة
        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.units_array,
                android.R.layout.simple_spinner_item
        );
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        for (SubRecord sub : subRecords) {
            // نفخ الـ item
            View item = inflater.inflate(R.layout.sub_record_item_edit, subRecordsLayout, false);

            TextView tvId       = item.findViewById(R.id.tvSubId);
            EditText etMaterial = item.findViewById(R.id.etSubMaterial);
            EditText etQuantity = item.findViewById(R.id.etSubQuantity);
            Spinner spUnit      = item.findViewById(R.id.spSubUnit);
            ImageButton btnDel  = item.findViewById(R.id.btnDeleteSub);

            // تعبئة البيانات
            tvId.setText(String.valueOf(sub.id));
            etMaterial.setText(sub.material);
            etQuantity.setText(String.valueOf(sub.quantity));

            // إعداد الـ Spinner
            spUnit.setAdapter(unitAdapter);
            int pos = unitAdapter.getPosition(sub.unit);
            spUnit.setSelection(pos);

            // تخزين المراجع للتحديث لاحقًا
            subRecordIdViews.add(tvId);
            subMaterialEdits.add(etMaterial);
            subQuantityEdits.add(etQuantity);
            subUnitSpinners.add(spUnit);

            // زر الحذف
            btnDel.setOnClickListener(v -> {
                deleteSubRecord(sub.id);
                subRecordsLayout.removeView(item);
            });

            // إضافة الـ item إلى الـ container
            subRecordsLayout.addView(item);
        }
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


                // إضافة عملية DELETE المعلقة
                DatabaseHelper.addPendingOperation(db, "DELETE", "records", id, new ContentValues());

                // محاولة المزامنة الفورية إذا كان الاتصال متاحًا
                if (isNetworkConnected()) {
                    DatabaseHelper.syncPendingOperations(this);
                }


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


            Toast.makeText(this, R.string.the_master_record_was_deleted_successfully, Toast.LENGTH_LONG).show();
            edName.setText("");
            edCourse.setText("");
            edFee.setText("");
            edName.requestFocus();
            finish(); // إنهاء النشاط والعودة إلى النشاط السابق
        } catch (Exception ex) {
            Toast.makeText(this, R.string.failed_to_delete_master_record, Toast.LENGTH_LONG).show();
        }
    }

    // دالة لحذف السجل الفرعي
    public void deleteSubRecord(int subRecordId) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();

            // جلب parent_id من قاعدة البيانات
            Cursor cursor = db.rawQuery(
                    "SELECT parent_id FROM sub_records WHERE id = ?",
                    new String[]{String.valueOf(subRecordId)}
            );

            int parentId = -1;
            if (cursor.moveToFirst()) {
                parentId = cursor.getInt(0); // الحصول على parent_id
            }
            cursor.close();


            int rowsDeleted = db.delete("sub_records", "id = ?", new String[]{String.valueOf(subRecordId)});
            if (rowsDeleted > 0) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    DatabaseReference ref = FirebaseDatabase.getInstance()
                            .getReference("users/" + user.getUid() + "/sub_records/" + subRecordId);
                    ref.removeValue();
                }


                // إضافة عملية DELETE المعلقة
                ContentValues data = new ContentValues();
                data.put("parent_id", parentId); // استخدام parent_id المُستَجلَب
                DatabaseHelper.addPendingOperation(db, "DELETE", "sub_records", String.valueOf(subRecordId), data);

                // محاولة المزامنة الفورية إذا كان الاتصال متاحًا
                if (isNetworkConnected()) {
                    DatabaseHelper.syncPendingOperations(this);
                }


                Toast.makeText(this, R.string.the_sup_record_was_deleted_successfully, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.failed_to_delete_sub_record, Toast.LENGTH_LONG).show();
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
                Toast.makeText(this,  R.string.please_fill_in_all_required_fields, Toast.LENGTH_LONG).show();
                return;
            }


            // التحقق من الحقول الفرعية
            for (int i = 0; i < subMaterialEdits.size(); i++) {
                String material = subMaterialEdits.get(i).getText().toString();
                String quantity = subQuantityEdits.get(i).getText().toString();
                String unit = subUnitSpinners.get(i).getSelectedItem().toString();

                if (material.isEmpty() || quantity.isEmpty() || unit.isEmpty()) {
                    Toast.makeText(this, R.string.please_fill_in_all_fields_in_the_sub_record, Toast.LENGTH_LONG).show();
                    return;
                }

                // التحقق من أن الكمية رقم صحيح
                try {
                    Integer.parseInt(quantity);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, R.string.the_quantity_in_the_sub_register_must_be_a_valid_number, Toast.LENGTH_LONG).show();
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



                // إضافة عملية UPDATE المعلقة
                DatabaseHelper.addPendingOperation(db, "UPDATE", "records", id, mainValues);

                // محاولة المزامنة الفورية إذا كان الاتصال متاحًا
                if (isNetworkConnected()) {
                    DatabaseHelper.syncPendingOperations(this);
                }



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

                Toast.makeText(this, R.string.the_record_was_updated_successfully, Toast.LENGTH_LONG).show();
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

            Toast.makeText(this, R.string.the_record_was_updated_successfully, Toast.LENGTH_LONG).show();
            finish(); // إنهاء النشاط والعودة إلى النشاط السابق
        } catch (Exception ex) {
            Log.e("UPDATE_ERROR", "Error updating record: ", ex);
            Toast.makeText(this, "فشل في تحديث السجل: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }



    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }



    @Override
    public boolean onSupportNavigateUp() {
        // معالجة الضغط على زر الرجوع
        finish(); // العودة إلى النشاط السابق
        return true;
    }

}
