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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class TopSellingProductsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private BarChart barChart;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_selling_products);



        // إعداد Toolbar
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // تمكين زر الرجوع
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("احصائيات وتحليل المنتجات");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }




        dbHelper = new DatabaseHelper(this);
        barChart = findViewById(R.id.barChart);
        listView = findViewById(R.id.productsListView);

        loadTopSellingProducts();
    }

    private void loadTopSellingProducts() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT material, SUM(quantity) AS total_sold " +
                "FROM sales_items GROUP BY material ORDER BY total_sold DESC";

        List<String> productList = new ArrayList<>();
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String material = cursor.getString(cursor.getColumnIndex("material"));
                    @SuppressLint("Range") int total = cursor.getInt(cursor.getColumnIndex("total_sold"));

                    // For ListView
                    productList.add(material + " - الكمية المباعة: " + total);

                    // For BarChart
                    entries.add(new BarEntry(index, total));
                    labels.add(material); // إضافة اسم المادة كتسمية
                    index++;

                } while (cursor.moveToNext());
            }
        }

        // Setup ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, productList);
        listView.setAdapter(adapter);

        // Setup BarChart
        BarDataSet dataSet = new BarDataSet(entries, "الكمية المباعة");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        BarData barData = new BarData(dataSet);

        // تعيين التسميات للمحور الأفقي (X-axis)
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }


    @Override
    public boolean onSupportNavigateUp() {
        // معالجة الضغط على زر الرجوع
        finish(); // العودة إلى النشاط السابق
        return true;
    }


}