package com.example.database78;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

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

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue_analysis);



        // إعداد Toolbar
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // تمكين زر الرجوع
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("احصائيات الواردات المالية");
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

        LineDataSet currentDataSet = new LineDataSet(currentEntries, "الفترة الحالية");
        currentDataSet.setColor(getResources().getColor(R.color.colorPrimary));
        currentDataSet.setCircleColor(getResources().getColor(R.color.colorPrimaryDark));

        LineDataSet previousDataSet = new LineDataSet(previousEntries, "الفترة السابقة");
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