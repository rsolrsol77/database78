package com.demo.database78;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;



public class FilterTotalAccountUpdatesActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;
    ListView totalAccountUpdatesListView;
    EditText startDateEditText, endDateEditText;
    Button filterButton;
    TextView totalSumTextView;
    Spinner currencySpinner;


    //admob
    private InterstitialAd mInterstitialAd;
    private final String AD_UNIT_ID = "ca-app-pub-9825698675981083/2059083864"; // مثال Test ID


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_total_account_updates);





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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.financial_import_filtering);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        dbHelper = new DatabaseHelper(this);
        totalAccountUpdatesListView = findViewById(R.id.total_account_updates_list_view);
        startDateEditText = findViewById(R.id.start_date_edit_text);
        endDateEditText = findViewById(R.id.end_date_edit_text);
        filterButton = findViewById(R.id.filter_button);
        totalSumTextView = findViewById(R.id.total_sum_text_view);
        currencySpinner = findViewById(R.id.currency_spinner);

        setupCurrencySpinner();

        startDateEditText.setOnClickListener(v -> showDatePickerDialog(startDateEditText));
        endDateEditText.setOnClickListener(v -> showDatePickerDialog(endDateEditText));

        filterButton.setOnClickListener(v -> {
            String startDate = startDateEditText.getText().toString();
            String endDate = endDateEditText.getText().toString();
            List<TotalAccountUpdate> filteredUpdates = getTotalAccountUpdates(startDate, endDate);
            CustomAdapter adapter = new CustomAdapter(this, filteredUpdates);
            totalAccountUpdatesListView.setAdapter(adapter);
            updateTotalSumTextView();
        });

        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTotalSumTextView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    // ========== نهاية نظام إدارة الإعلانات ==========









    private void setupCurrencySpinner() {
        List<String> currencies = getCurrenciesFromDatabase();
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currencyAdapter);
    }

    private List<String> getCurrenciesFromDatabase() {
        List<String> currencies = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT currency FROM total_account_updates", null);

        if (cursor.moveToFirst()) {
            do {
                currencies.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return currencies;
    }

    private void updateTotalSumTextView() {
        String startDate = startDateEditText.getText().toString();
        String endDate = endDateEditText.getText().toString();
        String selectedCurrency = currencySpinner.getSelectedItem() != null ? currencySpinner.getSelectedItem().toString() : "";

        double totalSum = calculateTotalAmountSum(startDate, endDate, selectedCurrency);
        totalSumTextView.setText(String.valueOf(totalSum));
    }

    private void showDatePickerDialog(final EditText dateEditText) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            dateEditText.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private List<TotalAccountUpdate> getTotalAccountUpdates(String startDate, String endDate) {
        List<TotalAccountUpdate> updates = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT total_amount, currency, DATE(total_timestamp) AS date_only FROM total_account_updates";
        if (!startDate.isEmpty() && !endDate.isEmpty()) {
            query += " WHERE date_only BETWEEN ? AND ?";
        }
        query += " ORDER BY date_only DESC";

        Cursor cursor = !startDate.isEmpty() && !endDate.isEmpty() ?
                db.rawQuery(query, new String[]{startDate, endDate}) :
                db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                updates.add(new TotalAccountUpdate(
                        cursor.getString(0),
                        cursor.getString(1),
                        formatTimestamp(cursor.getString(2))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return updates;
    }

    private double calculateTotalAmountSum(String startDate, String endDate, String currency) {
        double sum = 0.0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT total_amount FROM total_account_updates WHERE currency = ?";
        List<String> args = new ArrayList<>();
        args.add(currency);

        if (!startDate.isEmpty() && !endDate.isEmpty()) {
            query += " AND DATE(total_timestamp) BETWEEN ? AND ?";
            args.add(startDate);
            args.add(endDate);
        }

        Cursor cursor = db.rawQuery(query, args.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                sum += cursor.getDouble(0);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return sum;
    }

    private String formatTimestamp(String timestamp) {
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date date = dbFormat.parse(timestamp);
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
            return displayFormat.format(date);
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
    private static class CustomAdapter extends ArrayAdapter<TotalAccountUpdate> {
        public CustomAdapter(Context context, List<TotalAccountUpdate> updates) {
            super(context, R.layout.list_item_filter_total_account, updates);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TotalAccountUpdate item = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_filter_total_account, parent, false);
            }

            TextView tvAmount = convertView.findViewById(R.id.tv_amount);
            TextView tvCurrency = convertView.findViewById(R.id.tv_currency);
            TextView tvDate = convertView.findViewById(R.id.tv_date);

            if (item != null) {
                tvAmount.setText(item.amount);
                tvCurrency.setText(item.currency);
                tvDate.setText(item.date);
            }

            return convertView;
        }
    }

    // Data Model Class
    private static class TotalAccountUpdate {
        String amount;
        String currency;
        String date;

        public TotalAccountUpdate(String amount, String currency, String date) {
            this.amount = amount;
            this.currency = currency;
            this.date = date;
        }
    }
}