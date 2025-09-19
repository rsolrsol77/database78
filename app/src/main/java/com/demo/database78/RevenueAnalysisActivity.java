package com.demo.database78;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RevenueAnalysisActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private Spinner spinnerPeriod, spinnerCurrency;
    private CheckBox cbCompareYoY, cbCompareMoM;
    private LineChart lineChart;
    private TextView tvRevenueSummary;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());



    //admob
    private InterstitialAd mInterstitialAd;
    private final String AD_UNIT_ID = "ca-app-pub-9825698675981083/4540168745";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue_analysis);





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

        // إعداد Toolbar
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // تمكين زر الرجوع
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.financial_revenue_analysis);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        dbHelper = new DatabaseHelper(this);
        spinnerPeriod = findViewById(R.id.spinnerPeriod);
        spinnerCurrency = findViewById(R.id.spinnerCurrency);
        cbCompareYoY = findViewById(R.id.cbCompareYoY);
        cbCompareMoM = findViewById(R.id.cbCompareMoM);
        lineChart = findViewById(R.id.lineChart);
        tvRevenueSummary = findViewById(R.id.tvRevenueSummary);

        setupChart();
        setupListeners();
        loadData("يومي", "ALL");
    }
    // ========== نهاية نظام إدارة الإعلانات ==========







    private void setupListeners() {
        spinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedPeriod = parent.getItemAtPosition(position).toString();
                String selectedCurrency = spinnerCurrency.getSelectedItem().toString();
                loadData(selectedPeriod, selectedCurrency);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCurrency = parent.getItemAtPosition(position).toString();
                String selectedPeriod = spinnerPeriod.getSelectedItem().toString();
                loadData(selectedPeriod, selectedCurrency);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        View.OnClickListener comparisonListener = v -> {
            String selectedPeriod = spinnerPeriod.getSelectedItem().toString();
            String selectedCurrency = spinnerCurrency.getSelectedItem().toString();
            loadData(selectedPeriod, selectedCurrency);
        };
        cbCompareYoY.setOnClickListener(comparisonListener);
        cbCompareMoM.setOnClickListener(comparisonListener);
    }

    private void setupChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(true);
    }

    private void loadData(String period, String currency) {
        boolean compareYoY = cbCompareYoY.isChecked();
        boolean compareMoM = cbCompareMoM.isChecked();

        HashMap<String, Double> currentData = getRevenueData(period, currency, 0);
        HashMap<String, Double> previousData = new HashMap<>();

        if (compareYoY) previousData = getRevenueData(period, currency, -1);
        else if (compareMoM) previousData = getRevenueData(period, currency, -1);

        updateChart(currentData, previousData, period);
        updateSummary(currentData, previousData);
    }

    private HashMap<String, Double> getRevenueData(String period, String currency, int yearOffset) {
        HashMap<String, Double> revenueMap = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, yearOffset);

        String currencyFilter = currency.equals("ALL") ? "" : " AND currency = '" + currency + "'";
        String query = "SELECT strftime('%Y-%m-%d', total_timestamp) AS date, SUM(total_amount) AS total " +
                "FROM total_account_updates WHERE total_timestamp BETWEEN datetime('now', '-1 year') AND datetime('now') " +
                currencyFilter + " GROUP BY strftime('%Y-%m-%d', total_timestamp)";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                String label = cursor.getString(0);
                double total = cursor.getDouble(1);
                revenueMap.put(label, total);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return revenueMap;
    }

    private void updateChart(HashMap<String, Double> currentData, HashMap<String, Double> previousData, String period) {
        List<Entry> currentEntries = new ArrayList<>();
        List<Entry> previousEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int index = 0;
        for (String key : currentData.keySet()) {
            currentEntries.add(new Entry(index, currentData.get(key).floatValue()));
            labels.add(formatLabel(key, period));
            index++;
        }

        index = 0;
        for (String key : previousData.keySet()) {
            previousEntries.add(new Entry(index, previousData.get(key).floatValue()));
            index++;
        }

        LineDataSet currentDataSet = new LineDataSet(currentEntries, getString(R.string.current_filtering));
        currentDataSet.setColor(getResources().getColor(R.color.colorPrimary));
        currentDataSet.setCircleColor(getResources().getColor(R.color.colorPrimaryDark));

        LineDataSet previousDataSet = new LineDataSet(previousEntries, "Previous filtering");
        previousDataSet.setColor(getResources().getColor(android.R.color.holo_red_dark));
        previousDataSet.setCircleColor(getResources().getColor(android.R.color.holo_red_dark));

        LineData lineData = new LineData(currentDataSet, previousDataSet);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    private String formatLabel(String dateStr, String period) {
        try {
            Date date = dateFormat.parse(dateStr);
            SimpleDateFormat format = new SimpleDateFormat(getLabelFormat(period), Locale.getDefault());
            return format.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }

    private String getLabelFormat(String period) {
        switch (period) {
            case "يومي": return "dd/MM";
            case "أسبوعي": return "ww/yyyy";
            case "شهري": return "MMM yyyy";
            case "سنوي": return "yyyy";
            default: return "dd/MM/yyyy";
        }
    }

    private void updateSummary(HashMap<String, Double> current, HashMap<String, Double> previous) {
        double currentTotal = current.values().stream().mapToDouble(Double::doubleValue).sum();
        double previousTotal = previous.values().stream().mapToDouble(Double::doubleValue).sum();

        double change = 0.0;
        if (previousTotal > 0) {
            change = ((currentTotal - previousTotal) / previousTotal) * 100;
        }

        String summary = String.format(Locale.getDefault(),
                "الإيرادات الحالية: %.2f\nالإيرادات السابقة: %.2f\nالتغير: %s",
                currentTotal, previousTotal, (previousTotal > 0) ? String.format("%.2f%%", change) : "لا يوجد بيانات سابقة");

        tvRevenueSummary.setText(summary);
    }


    @Override
    public boolean onSupportNavigateUp() {
        // معالجة الضغط على زر الرجوع
        finish(); // العودة إلى النشاط السابق
        return true;
    }



}