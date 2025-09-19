package com.demo.database78;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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


        // ========== بدء عملية التحقق من حالة المستخدم ==========
        // 1. التحقق الأولي من حالة المستخدم المميز المخزنة محلياً
        checkUserPremiumStatus();






        dbHelper = new DatabaseHelper(this);
        setupToolbar();
        initializeListView();
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
        setupToolbar();
        initializeListView();
    }
    // ========== نهاية نظام إدارة الإعلانات ==========



















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