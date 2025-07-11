package com.example.database78;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TotalAccountUpdatesActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListView totalAccountUpdatesListView;

    private InterstitialAd mInterstitialAd;
    private final String AD_UNIT_ID = "ca-app-pub-9825698675981083/4099757998";


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_account_updates);




        //admob
        // 1. تحميل الإعلان
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, AD_UNIT_ID, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        // 2. عرض الإعلان مباشرة
                        showInterstitial();
                    }
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // فشل التحميل → تابع العمل الطبيعي
                    }
                });







        dbHelper = new DatabaseHelper(this);
        setupToolbar();
        initializeListView();
    }






    //admob
    private void showInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    // بعد إغلاق الإعلان، يمكن متابعة تهيئة الواجهة
                }
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    // إذا فشل العرض، تتابع كالمعتاد
                }
            });
            mInterstitialAd.show(this);
        }
    }








    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.financial_imports);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    private void initializeListView() {
        totalAccountUpdatesListView = findViewById(R.id.total_account_updates_list_view);
        List<TotalAccountItem> items = fetchTotalAccountUpdates();
        TotalAccountAdapter adapter = new TotalAccountAdapter(items);
        totalAccountUpdatesListView.setAdapter(adapter);
    }

    private List<TotalAccountItem> fetchTotalAccountUpdates() {
        List<TotalAccountItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT total_amount, currency, total_timestamp FROM total_account_updates ORDER BY total_timestamp DESC",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                String amount = cursor.getString(0);
                String currency = cursor.getString(1);
                String timestamp = formatTimestamp(cursor.getString(2));
                items.add(new TotalAccountItem(amount, currency, timestamp));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    private String formatTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return "Unknown Date";
        }

        // نقرأ النص مباشرة كـ "yyyy-MM-dd HH:mm:ss" دون تعيين منطقة زمنية
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dbFormat.setTimeZone(TimeZone.getTimeZone("UTC"));


        // نعرض بنفس التنسيق أيضاً
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
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
        finish();
        return true;
    }

    // Custom Adapter Class
    private class TotalAccountAdapter extends ArrayAdapter<TotalAccountItem> {

        TotalAccountAdapter(List<TotalAccountItem> items) {
            super(TotalAccountUpdatesActivity.this, R.layout.list_item_total_account, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            TotalAccountItem item = getItem(position);

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_total_account, parent, false);
                holder = new ViewHolder();
                holder.amountTextView = convertView.findViewById(R.id.tv_amount);
                holder.currencyTextView = convertView.findViewById(R.id.tv_currency);
                holder.timestampTextView = convertView.findViewById(R.id.tv_timestamp);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.amountTextView.setText(String.format("%s %s", item.getTotalAmount(), item.getCurrency()));
            holder.timestampTextView.setText(item.getTimestamp());

            return convertView;
        }

        private class ViewHolder {
            TextView amountTextView;
            TextView currencyTextView;
            TextView timestampTextView;
        }
    }
}