package com.example.moneytrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moneytrack.data.db.TransactionEntity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private AppDatabase database;
    private TransactionDao transactionDao;

    private TextView tvBalance;
    private Button btnIncome, btnExpense, logoutButton;

    private SharedPreferences sharedPreferences;
    private double balance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔹 Initialize Views
        tvBalance = findViewById(R.id.tvBalance);
        btnIncome = findViewById(R.id.btnIncome);
        btnExpense = findViewById(R.id.btnExpense);
        logoutButton = findViewById(R.id.logoutButton);

        database = AppDatabase.getInstance(this);
        transactionDao = database.transactionDao();

        // 🔹 SharedPreferences
        sharedPreferences = getSharedPreferences("MoneyTrackPrefs", MODE_PRIVATE);
        balance = sharedPreferences.getFloat("balance", 0);

        updateBalance();

        btnIncome.setOnClickListener(v -> showAmountDialog("income"));
        btnExpense.setOnClickListener(v -> showAmountDialog("expense"));
        calculateAndUpdateBalance();

        // 🚪 Logout
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
        builder.setTitle("Enter Amount");

        final EditText input = new EditText(this);
        input.setHint("Enter amount");
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {

            String value = input.getText().toString().trim();

            if (!value.isEmpty()) {

                double amount = Double.parseDouble(value);

                new Thread(() -> {

                    TransactionEntity transaction = new TransactionEntity(
                            amount,
                            "General",                     // category
                            type.toUpperCase(),           // INCOME / EXPENSE
                            System.currentTimeMillis(),   // date
                            ""                            // note
                    );

                    transactionDao.insert(transaction);

                    runOnUiThread(() -> calculateAndUpdateBalance());

                }).start();

                saveBalance();
                updateBalance();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    // 💾 Save balance
    private void saveBalance() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("balance", (float) balance);
        editor.apply();
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

            runOnUiThread(() -> {
                balanceTextView.setText("Balance: " + finalTotal);
            });

        }).start();
    }

    // 🔄 Update UI
    private void updateBalance() {
        tvBalance.setText("Balance: " + balance);
    }
}