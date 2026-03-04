package com.example.moneytrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moneytrack.data.db.AppDatabase;
import com.example.moneytrack.data.db.TransactionDao;
import com.example.moneytrack.data.db.TransactionEntity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private Button btnHistory;
    private AppDatabase database;
    private TransactionDao transactionDao;

    private TextView tvBalance;
    private Button btnIncome, btnExpense, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvBalance = findViewById(R.id.tvBalance);
        btnIncome = findViewById(R.id.btnIncome);
        btnExpense = findViewById(R.id.btnExpense);
        logoutButton = findViewById(R.id.logoutButton);

        database = AppDatabase.getInstance(this);
        transactionDao = database.transactionDao();

        btnHistory = findViewById(R.id.btnHistory);

        btnHistory.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, HistoryActivity.class))
        );

        calculateAndUpdateBalance(); // app start-ին հաշվարկ

        btnIncome.setOnClickListener(v -> showAmountDialog("income"));
        btnExpense.setOnClickListener(v -> showAmountDialog("expense"));

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
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

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        categories);

        spinnerCategory.setAdapter(adapter);

        // 👇 Եթե ընտրեն Other → ցույց տանք custom input
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
}