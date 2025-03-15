package com.example.database78;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class UpdatesFragment extends Fragment {

    DatabaseHelper dbHelper;
    ListView updatesListView;


    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_updates, container, false);

        // تهيئة قاعدة البيانات
        dbHelper = new DatabaseHelper(getActivity());

        // ربط ListView بعنصر الواجهة
        updatesListView = view.findViewById(R.id.updates_list_view);



        // استرجاع التحديثات وعرضها
        List<String> updates = getAllUpdates();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, updates);
        updatesListView.setAdapter(adapter);





        return view;
    }

    // دالة لاسترجاع جميع التحديثات من قاعدة البيانات
    public List<String> getAllUpdates() {
        List<String> updates = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT message, timestamp FROM updates ORDER BY timestamp DESC", null);
        if (cursor.moveToFirst()) {
            do {
                String message = cursor.getString(0);
                String timestamp = cursor.getString(1);

                // تحسين تنسيق التاريخ والوقت
                String formattedTimestamp = formatTimestamp(timestamp);
                updates.add(formattedTimestamp + ": " + message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return updates;
    }


    // دالة لتنسيق التاريخ والوقت بشكل أكثر دقة
    private String formatTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return "Unknown Date";
        }

        // الشكل الذي يتم فيه تخزين البيانات في SQLite (UTC)
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dbFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // ضبط المنطقة الزمنية للتوقيت المخزن كـ UTC

        // الشكل الذي نريد عرضه للمستخدم (توقيت محلي)
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault());
        displayFormat.setTimeZone(TimeZone.getDefault()); // ضبط المنطقة الزمنية للتوقيت المحلي

        try {
            // تحويل النص إلى كائن تاريخ باستخدام التوقيت المخزن كـ UTC
            Date date = dbFormat.parse(timestamp);
            if (date != null) {
                // تحويل التاريخ إلى التوقيت المحلي للعرض
                return displayFormat.format(date);
            } else {
                return timestamp; // في حالة فشل التحويل، نعرض التوقيت كما هو
            }
        } catch (ParseException e) {
            e.printStackTrace();
            // إذا حدث خطأ، نعرض التوقيت كما هو بدون تعديل
            return timestamp;
        }
    }


}
