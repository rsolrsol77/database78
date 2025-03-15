package com.example.database78;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TotalAccountUpdatesActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;
    ListView totalAccountUpdatesListView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_account_updates);

        dbHelper = new DatabaseHelper(this);

        // إعداد Toolbar
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // تمكين زر الرجوع
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("الواردات المالية");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }


        totalAccountUpdatesListView = findViewById(R.id.total_account_updates_list_view);

        List<String> totalAccountUpdates = getTotalAccountUpdates();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, totalAccountUpdates);
        totalAccountUpdatesListView.setAdapter(adapter);
    }

    public List<String> getTotalAccountUpdates() {
        List<String> updates = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT total_amount, currency, total_timestamp FROM total_account_updates ORDER BY total_timestamp DESC", null);
        if (cursor.moveToFirst()) {
            do {
                String totalAmount = cursor.getString(0);
                String currency = cursor.getString(1);
                String timestamp = cursor.getString(2);

                String formattedTimestamp = formatTimestamp(timestamp);
                String message = "وارد مالي قدره " + totalAmount + " " + currency + "\nفي التاريخ " + formattedTimestamp;
                updates.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return updates;
    }

    private String formatTimestamp(String timestamp) {
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dbFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault());
        displayFormat.setTimeZone(TimeZone.getDefault());

        try {
            Date date = dbFormat.parse(timestamp);
            return date != null ? displayFormat.format(date) : timestamp;
        } catch (ParseException e) {
            e.printStackTrace();
            return timestamp;
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        // معالجة الضغط على زر الرجوع
        finish(); // العودة إلى النشاط السابق
        return true;
    }

}
