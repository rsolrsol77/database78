package com.example.database78;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class FilterTotalAccountUpdatesActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;
    ListView totalAccountUpdatesListView;
    EditText startDateEditText, endDateEditText;
    Button filterButton;
    TextView totalSumTextView;
    Spinner currencySpinner;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_total_account_updates);



        // إعداد Toolbar
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // تمكين زر الرجوع
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("فلترة الواردات المالية");
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

        startDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(startDateEditText);
            }
        });

        endDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(endDateEditText);
            }
        });

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startDate = startDateEditText.getText().toString();
                String endDate = endDateEditText.getText().toString();
                List<String> filteredUpdates = getTotalAccountUpdates(startDate, endDate);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(FilterTotalAccountUpdatesActivity.this, android.R.layout.simple_list_item_1, filteredUpdates);
                totalAccountUpdatesListView.setAdapter(adapter);

                updateTotalSumTextView();
            }
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
        totalSumTextView.setText("مجموع القيم: " + totalSum);
    }

    private void showDatePickerDialog(final EditText dateEditText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                calendar.set(year, month, dayOfMonth);
                String formattedDate = dateFormat.format(calendar.getTime());
                dateEditText.setText(formattedDate);
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    public List<String> getTotalAccountUpdates(String startDate, String endDate) {
        List<String> updates = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT total_amount, currency, DATE(total_timestamp) AS date_only FROM total_account_updates";
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            query += " WHERE date_only BETWEEN ? AND ?";
        }
        query += " ORDER BY date_only DESC";

        Cursor cursor = startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()
                ? db.rawQuery(query, new String[]{startDate, endDate})
                : db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String totalAmount = cursor.getString(0);
                String currency = cursor.getString(1);
                String dateOnly = formatTimestamp(cursor.getString(2));
                String message = "وارد مالي قدره " + totalAmount + " " + currency + "\nفي التاريخ " + dateOnly;
                updates.add(message);
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

        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
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
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        dbFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
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
