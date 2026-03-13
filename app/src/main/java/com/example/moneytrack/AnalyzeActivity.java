package com.example.moneytrack;

import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moneytrack.data.db.AppDatabase;
import com.example.moneytrack.data.db.TransactionDao;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class AnalyzeActivity extends AppCompatActivity {

    BarChart barChart;
    TransactionDao transactionDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze);

        barChart = findViewById(R.id.barChart);

        Button btnToday = findViewById(R.id.btnToday);
        Button btnWeek = findViewById(R.id.btnWeek);
        Button btnMonth = findViewById(R.id.btnMonth);

        AppDatabase db = AppDatabase.getInstance(this);
        transactionDao = db.transactionDao();

        btnToday.setOnClickListener(v -> loadData(1));
        btnWeek.setOnClickListener(v -> loadData(7));
        btnMonth.setOnClickListener(v -> loadData(30));

        // Default graph → Month
        loadData(30);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setSelectedItemId(R.id.nav_analyze);

        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_analyze) {
                return true;
            }

            if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }

            return false;
        });
    }

    private void loadData(int days){

        long now = System.currentTimeMillis();
        long startTime = now - (days * 24L * 60 * 60 * 1000);

        new Thread(() -> {

            Double income = transactionDao.getIncomeFrom(startTime);
            Double expense = transactionDao.getExpenseFrom(startTime);

            if(income == null) income = 0.0;
            if(expense == null) expense = 0.0;

            double finalIncome = income;
            double finalExpense = expense;

            runOnUiThread(() -> {

                ArrayList<BarEntry> entries = new ArrayList<>();

                entries.add(new BarEntry(1f,(float)finalIncome));
                entries.add(new BarEntry(2f,(float)finalExpense));

                BarDataSet dataSet = new BarDataSet(entries,"Money Flow");

                ArrayList<Integer> colors = new ArrayList<>();
                colors.add(Color.GREEN);
                colors.add(Color.RED);
                dataSet.setColors(colors);

                BarData barData = new BarData(dataSet);

                barChart.setData(barData);
                barChart.invalidate();

            });

        }).start();
    }
}
