package com.example.moneytrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moneytrack.data.db.AppDatabase;
import com.example.moneytrack.data.db.TransactionDao;
import com.example.moneytrack.data.db.TransactionEntity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvBalance;
    private Button btnIncome, btnExpense, btnHistory, btnVoice;

    private AppDatabase database;
    private TransactionDao transactionDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("MoneyTrackPrefs", MODE_PRIVATE);
        boolean remember = prefs.getBoolean("REMEMBER", false);
        boolean pinVerified = prefs.getBoolean("PIN_VERIFIED", false);

        if (remember && !pinVerified) {
            startActivity(new Intent(this, VerifyPinActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        tvBalance = findViewById(R.id.tvBalance);
        btnIncome = findViewById(R.id.btnIncome);
        btnExpense = findViewById(R.id.btnExpense);
        btnHistory = findViewById(R.id.btnHistory);
        btnVoice = findViewById(R.id.btnVoice);

        database = AppDatabase.getInstance(this);
        transactionDao = database.transactionDao();

        calculateAndUpdateBalance();

        btnIncome.setOnClickListener(v -> showAmountDialog("income"));
        btnExpense.setOnClickListener(v -> showAmountDialog("expense"));

        btnHistory.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, HistoryActivity.class))
        );

        btnVoice.setOnClickListener(v -> startVoiceInput());
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_home) return true;

            if (item.getItemId() == R.id.nav_history) {
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_analyze) {
                startActivity(new Intent(MainActivity.this, AnalyzeActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            }

            return false;
        });
    }

    private void showAmountDialog(String type) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Transaction");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_transaction, null);
        builder.setView(view);

        EditText etAmount = view.findViewById(R.id.etAmount);
        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        EditText etCustomCategory = view.findViewById(R.id.etCustomCategory);

        String[] categories = {"Food", "Transport", "Salary", "Shopping", "Other"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories
        );

        spinnerCategory.setAdapter(adapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {

                if (categories[position].equals("Other")) {
                    etCustomCategory.setVisibility(View.VISIBLE);
                } else {
                    etCustomCategory.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        builder.setPositiveButton("OK", (dialog, which) -> {

            String value = etAmount.getText().toString().trim();

            if (!value.isEmpty()) {

                double amount = Double.parseDouble(value);

                String selectedCategory;

                if (spinnerCategory.getSelectedItem().toString().equals("Other")) {

                    selectedCategory = etCustomCategory.getText().toString().trim();

                    if (selectedCategory.isEmpty()) {
                        selectedCategory = "Other";
                    }

                } else {
                    selectedCategory = spinnerCategory.getSelectedItem().toString();
                }

                String finalSelectedCategory = selectedCategory;

                new Thread(() -> {

                    TransactionEntity transaction = new TransactionEntity(
                            amount,
                            finalSelectedCategory,
                            type.toUpperCase(),
                            System.currentTimeMillis(),
                            ""
                    );

                    transactionDao.insert(transaction);

                    runOnUiThread(this::calculateAndUpdateBalance);

                }).start();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void calculateAndUpdateBalance() {

        new Thread(() -> {

            List<TransactionEntity> list = transactionDao.getAllTransactions();

            double total = 0;

            for (TransactionEntity t : list) {

                if (t.type.equals("INCOME")) {
                    total += t.amount;
                } else {
                    total -= t.amount;
                }
            }

            double finalTotal = total;

            runOnUiThread(() ->
                    tvBalance.setText("Balance: " + finalTotal)
            );

        }).start();
    }

    private void startVoiceInput() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");

        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {

            ArrayList<String> result =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (result != null && !result.isEmpty()) {

                String speechText = result.get(0);

                processVoiceInput(speechText);
            }
        }
    }

    private void processVoiceInput(String text) {

        text = text.toLowerCase();

        double amount = 0;

        for (String word : text.split(" ")) {

            word = word.replace(",", "").replace(".", "");

            try {
                amount = Double.parseDouble(word);
                break;
            } catch (Exception ignored) {}
        }

        if (amount == 0) return;

        String type = "EXPENSE";

        if (text.contains("income") || text.contains("salary")) {
            type = "INCOME";
        }

        String category = "Other";

        for (String word : text.split(" ")) {

            word = word.replaceAll("[^a-z]", "");

            if (!word.isEmpty()
                    && !word.equals("spent")
                    && !word.equals("on")
                    && !word.equals("for")
                    && !word.equals("the")
                    && !word.equals("a")
                    && !word.equals("i")
                    && !word.equals("income")
                    && !word.equals("expense")
                    && !word.equals("salary")) {

                category = word;
                break;
            }
        }

        category = category.substring(0,1).toUpperCase() + category.substring(1);

        double finalAmount = amount;
        String finalCategory = category;
        String finalType = type;

        new Thread(() -> {

            TransactionEntity transaction = new TransactionEntity(
                    finalAmount,
                    finalCategory,
                    finalType,
                    System.currentTimeMillis(),
                    ""
            );

            transactionDao.insert(transaction);

            runOnUiThread(this::calculateAndUpdateBalance);

        }).start();
    }
}