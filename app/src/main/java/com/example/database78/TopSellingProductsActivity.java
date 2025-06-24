package com.example.database78;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.ArrayList;
import java.util.List;

public class TopSellingProductsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private BarChart barChart;
    private ListView listView;
    private Spinner unitSpinner;
    private ProgressBar progressBar;
    private List<String> unitsList;


    //admob
    private InterstitialAd mInterstitialAd;
    private final String AD_UNIT_ID = "ca-app-pub-9825698675981083/1914005401";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_selling_products);








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








        // إعداد الـ Toolbar
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.product_analysis);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        // ربط عناصر الواجهة
        dbHelper = new DatabaseHelper(this);
        barChart = findViewById(R.id.barChart);
        listView = findViewById(R.id.productsListView);
        unitSpinner = findViewById(R.id.unitSpinner);
        progressBar = findViewById(R.id.progressBar);

        // تجهيز قائمة الوحدات
        prepareUnitSpinner();

        // تحميل البيانات لأول مرة بدون تصفية
        loadTopSellingProducts(null);
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








    /**
     * جلب الوحدات الفريدة من قاعدة البيانات وإعداد Spinner
     */
    private void prepareUnitSpinner() {
        unitsList = new ArrayList<>();
        unitsList.add(getString(R.string.all_units));

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT unit FROM sales_items", null);
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String unit = cursor.getString(cursor.getColumnIndex("unit"));
                unitsList.add(unit);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                unitsList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(adapter);

        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = unitsList.get(position);
                // إذا الاختيار "كل الوحدات" فنعطي null للتصفية
                loadTopSellingProducts(getString(R.string.all_units).equals(selected) ? null : selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // لا شيء
            }
        });
    }

    /**
     * تحميل البيانات مع خيار تصفية حسب الوحدة وعرضها في ListView و BarChart
     * @param unitFilter الوحدة المختارة أو null لعرض الكل
     */
    private void loadTopSellingProducts(@Nullable final String unitFilter) {
        // إظهار ProgressBar
        progressBar.setVisibility(View.VISIBLE);

        // تنفيذ الاستعلام في Thread منفصل
        new Thread(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String sql;
            String[] args = null;

            if (unitFilter != null) {
                sql = "SELECT material, unit, SUM(quantity) AS total_sold " +
                        "FROM sales_items WHERE unit = ? " +
                        "GROUP BY material, unit ORDER BY total_sold DESC";
                args = new String[]{ unitFilter };
            } else {
                sql = "SELECT material, unit, SUM(quantity) AS total_sold " +
                        "FROM sales_items GROUP BY material, unit ORDER BY total_sold DESC";
            }

            List<Product> productList = new ArrayList<>();
            List<BarEntry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            int idx = 0;

            try (Cursor cursor = (args != null)
                    ? db.rawQuery(sql, args)
                    : db.rawQuery(sql, null)) {
                if (cursor.moveToFirst()) {
                    do {
                        @SuppressLint("Range") String material = cursor.getString(cursor.getColumnIndex("material"));
                        @SuppressLint("Range") String unit     = cursor.getString(cursor.getColumnIndex("unit"));
                        @SuppressLint("Range") int total       = cursor.getInt(cursor.getColumnIndex("total_sold"));

                        productList.add(new Product(material, total, unit, idx + 1));
                        entries.add(new BarEntry(idx, total));
                        labels.add(material + " (" + unit + ")");
                        idx++;
                    } while (cursor.moveToNext());
                }
            }
            db.close();

            // تحديث UI في الـ Main Thread
            runOnUiThread(() -> {
                // تحديث ListView
                ProductAdapter adapter = new ProductAdapter(this, productList);
                listView.setAdapter(adapter);

                // تحديث BarChart
                BarDataSet dataSet = new BarDataSet(entries, getString(R.string.quantity_sold));
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                BarData barData = new BarData(dataSet);

                barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                barChart.setData(barData);
                barChart.getDescription().setEnabled(false);
                barChart.animateY(500);
                barChart.invalidate();

                // إخفاء ProgressBar بعد انتهاء التحديث
                progressBar.setVisibility(View.GONE);
            });
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Adapter مخصص لعرض المنتجات في ListView
     */
    private class ProductAdapter extends ArrayAdapter<Product> {
        public ProductAdapter(Context context, List<Product> products) {
            super(context, R.layout.list_item_product, products);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Product product = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.list_item_product, parent, false);
            }

            TextView tvName = convertView.findViewById(R.id.tv_product_name);
            TextView tvQty  = convertView.findViewById(R.id.tv_sold_quantity);
            TextView tvRank = convertView.findViewById(R.id.tv_rank);

            if (product != null) {
                tvName.setText(product.getMaterial());

                // استخدام النص المترجم مع إدخال القيم ديناميكياً
                String soldQtyText = getString(
                        R.string.sold_quantity,
                        product.getTotalSold(),
                        product.getUnit()
                );
                tvQty.setText(soldQtyText);

                // إذا أردت إضافة التنسيق للـ Rank أيضاً
                String rankText = getString(R.string.rank, product.getRank());
                tvRank.setText(rankText);
            }

            return convertView;
        }
    }

    /**
     * نموذج بيانات المنتج
     */
    private static class Product {
        private final String material;
        private final int totalSold;
        private final String unit;
        private final int rank;

        public Product(String material, int totalSold, String unit, int rank) {
            this.material  = material;
            this.totalSold = totalSold;
            this.unit      = unit;
            this.rank      = rank;
        }

        public String getMaterial() { return material; }
        public int    getTotalSold() { return totalSold; }
        public String getUnit()     { return unit; }
        public int    getRank()     { return rank; }
    }
}
