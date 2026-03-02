package com.example.moneytrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

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

        // 🔹 SharedPreferences
        sharedPreferences = getSharedPreferences("MoneyTrackPrefs", MODE_PRIVATE);
        balance = sharedPreferences.getFloat("balance", 0);

        updateBalance();

        // ➕ Add Income
        btnIncome.setOnClickListener(v -> {
            balance += 100;   // Ժամանակավոր 100 ավելացնում ենք
            saveBalance();
            updateBalance();
            Toast.makeText(this, "Income Added", Toast.LENGTH_SHORT).show();
        });

        // ➖ Add Expense
        btnExpense.setOnClickListener(v -> {
            balance -= 50;   // Ժամանակավոր 50 հանում ենք
            saveBalance();
            updateBalance();
            Toast.makeText(this, "Expense Added", Toast.LENGTH_SHORT).show();
        });

        // 🚪 Logout
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // 💾 Save balance
    private void saveBalance() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("balance", (float) balance);
        editor.apply();
    }

    // 🔄 Update UI
    private void updateBalance() {
        tvBalance.setText("Balance: " + balance);
    }
}