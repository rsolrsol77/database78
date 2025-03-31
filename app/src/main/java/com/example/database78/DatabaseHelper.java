
package com.example.database78;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SliteDb.db";
    private static final int DATABASE_VERSION = 2;

    // جدول السجلات الأساسي
    private static final String TABLE_RECORDS = "records";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_COURSE = "course";
    private static final String COLUMN_FEE = "fee";

    // جدول السجلات الفرعي
    public static final String TABLE_SUB_RECORDS = "sub_records";
    private static final String COLUMN_PARENT_ID = "parent_id";
    private static final String COLUMN_MATERIAL = "material";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_UNIT = "unit"; // وحدة القياس


    // جدول المبيعات
    private static final String TABLE_SALES = "sales";
    private static final String COLUMN_ID_S = "id";
    private static final String COLUMN_BUYER_NAME = "buyer_name";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_ORDER_INFO = "order_info";
    private static final String COLUMN_ORDER_STATUS = "order_status";
    private static final String COLUMN_TOTAL_AMOUNT = "total_amount"; // الحساب الكلي
    private static final String COLUMN_CURRENCY = "currency"; // العملة



    // جدول المواد المرتبطة بالمبيعات
    private static final String TABLE_SALES_ITEMS = "sales_items";
    private static final String COLUMN_SALES_ID = "sales_id";
    private static final String COLUMN_MATERIAL_S = "material";
    private static final String COLUMN_QUANTITY_S = "quantity";
    private static final String COLUMN_UNIT_S = "unit";




    // أعمدة جدول التحديثات
    private static final String TABLE_UPDATES = "updates";
    private static final String COLUMN_UPDATE_ID = "update_id";
    private static final String COLUMN_UPDATE_MESSAGE = "message";
    private static final String COLUMN_TIMESTAMP = "timestamp";



    // أعمدة جدول التحديثات الكلية
    private static final String TABLE_TOTAL_ACCOUNT_UPDATES = "total_account_updates";  // جدول التحديثات الكلية
    private static final String COLUMN_TOTAL_ACCOUNT_ID = "total_account_id";
    private static final String COLUMN_TOTAL_TIMESTAMP = "total_timestamp";




    // جدول العمليات المعلقة
    private static final String TABLE_PENDING_OPERATIONS = "pending_operations";
    private static final String COLUMN_OPERATION_ID = "operation_id";
    private static final String COLUMN_OPERATION_TYPE = "operation_type";
    private static final String COLUMN_TABLE_NAME = "table_name";
    private static final String COLUMN_RECORD_ID = "record_id";
    private static final String COLUMN_DATA = "data";
    private static final String COLUMN_TIMESTAMP_OP = "timestamp_op";






    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        // إنشاء جدول السجلات الأساسي
        String CREATE_TABLE_RECORDS = "CREATE TABLE " + TABLE_RECORDS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " VARCHAR, "
                + COLUMN_COURSE + " VARCHAR, "
                + COLUMN_FEE + " VARCHAR)";
        db.execSQL(CREATE_TABLE_RECORDS);

        // إنشاء جدول السجلات الفرعي
        String CREATE_TABLE_SUB_RECORDS = "CREATE TABLE " + TABLE_SUB_RECORDS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_PARENT_ID + " INTEGER, "
                + COLUMN_MATERIAL + " VARCHAR, "
                + COLUMN_QUANTITY + " INTEGER, "
                + COLUMN_UNIT + " VARCHAR, " // إضافة وحدة القياس

                + "FOREIGN KEY(" + COLUMN_PARENT_ID + ") REFERENCES " + TABLE_RECORDS + "(" + COLUMN_ID + "))";
        db.execSQL(CREATE_TABLE_SUB_RECORDS);

        // إنشاء جدول المبيعات
        String CREATE_TABLE_SALES = "CREATE TABLE " + TABLE_SALES + "("
                + COLUMN_ID_S + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_BUYER_NAME + " TEXT, "
                + COLUMN_PHONE + " TEXT, "
                + COLUMN_ADDRESS + " TEXT, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_ORDER_INFO + " TEXT, "
                + COLUMN_ORDER_STATUS + " TEXT,"
                + COLUMN_TOTAL_AMOUNT + " TEXT, "
                + COLUMN_CURRENCY + " TEXT)";  // الحقل الجديد
        db.execSQL(CREATE_TABLE_SALES);



        // إنشاء جدول المواد المرتبطة بجدول المبيعات
        String CREATE_TABLE_SALES_ITEMS = "CREATE TABLE " + TABLE_SALES_ITEMS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SALES_ID + " INTEGER, "
                + COLUMN_MATERIAL_S + " TEXT, "
                + COLUMN_QUANTITY_S + " INTEGER, "
                + COLUMN_UNIT_S + " TEXT, "
                + "FOREIGN KEY(" + COLUMN_SALES_ID + ") REFERENCES "
                + TABLE_SALES + "(" + COLUMN_ID_S + ") ON DELETE CASCADE)"; // التعديل هنا
        db.execSQL(CREATE_TABLE_SALES_ITEMS);




        // إنشاء جدول التحديثات
        String CREATE_TABLE_UPDATES = "CREATE TABLE " + TABLE_UPDATES + "("
                + COLUMN_UPDATE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_UPDATE_MESSAGE + " TEXT, "
                + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(CREATE_TABLE_UPDATES);



        // إنشاء جدول التحديثات الكلية
        String CREATE_TABLE_TOTAL_ACCOUNT_UPDATES = "CREATE TABLE " + TABLE_TOTAL_ACCOUNT_UPDATES + "("
                + COLUMN_TOTAL_ACCOUNT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TOTAL_AMOUNT + " TEXT, "
                + COLUMN_CURRENCY + " TEXT, "
                + COLUMN_TOTAL_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(CREATE_TABLE_TOTAL_ACCOUNT_UPDATES);





        // إنشاء جدول العمليات المعلقة
        String CREATE_TABLE_PENDING_OPERATIONS = "CREATE TABLE " + TABLE_PENDING_OPERATIONS + "("
                + COLUMN_OPERATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_OPERATION_TYPE + " TEXT, "
                + COLUMN_TABLE_NAME + " TEXT, "
                + COLUMN_RECORD_ID + " TEXT, "
                + COLUMN_DATA + " TEXT, "
                + COLUMN_TIMESTAMP_OP + " DATETIME DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(CREATE_TABLE_PENDING_OPERATIONS);








    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUB_RECORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SALES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SALES_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_UPDATES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TOTAL_ACCOUNT_UPDATES);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PENDING_OPERATIONS); // إضافة هذا السطر

        onCreate(db);
    }



    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // تفعيل القيود الخارجية (Foreign Keys) لكل اتصال بقاعدة البيانات
        db.execSQL("PRAGMA foreign_keys = ON;");
    }



    public static void syncUpdateToFirebase(String tableName, String recordId, ContentValues values) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // استخدام recordId (ID من SQLite) كمفتاح
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users/" + user.getUid() + "/" + tableName + "/" + recordId);

        Map<String, Object> updateData = valuesToMap(values);
        ref.updateChildren(updateData)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "تم التحديث بنجاح"))
                .addOnFailureListener(e -> Log.e("Firebase", "فشل التحديث: " + e.getMessage()));
    }



    public static void syncToFirebase(String tableName, ContentValues values) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // الحصول على الـ ID من SQLite
        String key = values.getAsString("id");

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users/" + user.getUid() + "/" + tableName + "/" + key); // استخدام الـ ID في المسار

        ref.setValue(valuesToMap(values)) // استخدام setValue بدلاً من push()
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "تم المزامنة بنجاح"))
                .addOnFailureListener(e -> Log.e("Firebase", "فشل المزامنة: " + e.getMessage()));
    }

    public static void syncFromFirebase(Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users/" + user.getUid());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
                db.beginTransaction();

                try {
                    // مزامنة كل جدول
                    syncTable(db, snapshot, TABLE_RECORDS);
                    syncTable(db, snapshot, TABLE_SUB_RECORDS);
                    syncTable(db, snapshot, TABLE_SALES);
                    syncTable(db, snapshot, TABLE_SALES_ITEMS);
                    syncTable(db, snapshot, TABLE_UPDATES);
                    syncTable(db, snapshot, TABLE_TOTAL_ACCOUNT_UPDATES);

                    db.setTransactionSuccessful();
                    Log.d("Firebase", "All data synced to SQLite");
                } catch (Exception e) {
                    Log.e("Firebase", "Sync error: " + e.getMessage());
                } finally {
                    db.endTransaction();
                    db.close();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Sync cancelled: " + error.getMessage());
            }
        });
    }

    private static void syncTable(SQLiteDatabase db, DataSnapshot snapshot, String table) {
        db.delete(table, null, null); // Clear existing data

        for (DataSnapshot ds : snapshot.child(table).getChildren()) {
            ContentValues values = new ContentValues();
            Map<String, Object> map = (Map<String, Object>) ds.getValue();

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() != null) {
                    values.put(entry.getKey(), entry.getValue().toString());
                }
            }

            db.insert(table, null, values);
        }
    }

    // تحويل ContentValues إلى Map ل Firebase
    public static Map<String, Object> valuesToMap(ContentValues values) {
        Map<String, Object> map = new HashMap<>();
        for (String key : values.keySet()) {
            map.put(key, values.get(key));
        }
        return map;
    }









    // دالة لإضافة عملية معلقة
    public static void addPendingOperation(SQLiteDatabase db, String operationType, String tableName, String recordId, ContentValues data) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_OPERATION_TYPE, operationType);
        values.put(COLUMN_TABLE_NAME, tableName);
        values.put(COLUMN_RECORD_ID, recordId);
        values.put(COLUMN_DATA, dataToString(data)); // تحويل ContentValues إلى String
        db.insert(TABLE_PENDING_OPERATIONS, null, values);
    }

    // دالة لتحويل ContentValues إلى String
    private static String dataToString(ContentValues data) {
        JSONObject json = new JSONObject();
        try {
            for (Map.Entry<String, Object> entry : data.valueSet()) {
                json.put(entry.getKey(), entry.getValue());
            }
            return json.toString();
        } catch (JSONException e) {
            Log.e("DatabaseHelper", "Error converting to JSON: " + e.getMessage());
            return "";
        }
    }

    // دالة لتحويل String إلى ContentValues
    private static ContentValues stringToData(String dataString) {
        ContentValues values = new ContentValues();
        try {
            JSONObject json = new JSONObject(dataString);
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = json.get(key);
                if (value instanceof String) {
                    values.put(key, (String) value);
                } else if (value instanceof Integer) {
                    values.put(key, (Integer) value);
                } else if (value instanceof Long) {
                    values.put(key, (Long) value);
                }
            }
        } catch (JSONException e) {
            Log.e("DatabaseHelper", "Error parsing JSON: " + e.getMessage());
        }
        return values;
    }

    // دالة لمزامنة العمليات المعلقة
    public static void syncPendingOperations(Context context) {
        SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PENDING_OPERATIONS, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String operationType = cursor.getString(cursor.getColumnIndex(COLUMN_OPERATION_TYPE));
                @SuppressLint("Range") String tableName = cursor.getString(cursor.getColumnIndex(COLUMN_TABLE_NAME));
                @SuppressLint("Range") String recordId = cursor.getString(cursor.getColumnIndex(COLUMN_RECORD_ID));
                @SuppressLint("Range") String dataString = cursor.getString(cursor.getColumnIndex(COLUMN_DATA));
                @SuppressLint("Range") int operationId = cursor.getInt(cursor.getColumnIndex(COLUMN_OPERATION_ID));

                ContentValues data = stringToData(dataString);

                // تنفيذ العملية على Firebase
                boolean success = executeFirebaseOperation(operationType, tableName, recordId, data);

                if (success) {
                    // حذف العملية بعد النجاح
                    db.delete(TABLE_PENDING_OPERATIONS, COLUMN_OPERATION_ID + "=?", new String[]{String.valueOf(operationId)});
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }

    // دالة مساعدة لتنفيذ العمليات على Firebase
    private static boolean executeFirebaseOperation(String operationType, String tableName, String recordId, ContentValues data) {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) return false;

            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("users/" + user.getUid() + "/" + tableName + "/" + recordId);

            switch (operationType) {
                case "INSERT":
                case "UPDATE":
                    // تحديث جميع الحقول الموجودة في data
                    Map<String, Object> updates = new HashMap<>();
                    for (String key : data.keySet()) {
                        updates.put(key, data.get(key));
                    }
                    ref.updateChildren(updates);
                    break;
                case "DELETE":
                    ref.removeValue();
                    break;
            }
            return true;
        } catch (Exception e) {
            Log.e("PendingOps", "Error executing operation: " + e.getMessage());
            return false;
        }
    }










}
