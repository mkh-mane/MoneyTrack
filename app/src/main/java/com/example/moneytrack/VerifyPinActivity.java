package com.example.moneytrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class VerifyPinActivity extends AppCompatActivity {

    private EditText pinEditText;
    private Button verifyButton, forgotButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_pin);

        pinEditText = findViewById(R.id.pinEditText);
        verifyButton = findViewById(R.id.verifyPinButton);
        forgotButton = findViewById(R.id.forgotPinButton);

        SharedPreferences prefs = getSharedPreferences("MoneyTrackPrefs", MODE_PRIVATE);
        String savedPin = prefs.getString("user_pin", null);

        // ❗ Եթե PIN չկա → ուղարկում ենք Create PIN
        if (savedPin == null) {
            startActivity(new Intent(this, CreatePinActivity.class));
            finish();
            return;
        }

        verifyButton.setOnClickListener(v -> {

            String enteredPin = pinEditText.getText().toString().trim();

            // ❗ Դատարկ PIN
            if (enteredPin.isEmpty()) {
                Toast.makeText(this, "Enter PIN", Toast.LENGTH_SHORT).show();
                return;
            }

            if (enteredPin.equals(savedPin)) {

                // ✅ ԱՄԵՆԱԿԱՐԵՎՈՐԸ
                prefs.edit().putBoolean("PIN_VERIFIED", true).apply();

                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            } else {
                Toast.makeText(this, "Wrong PIN", Toast.LENGTH_SHORT).show();
            }
        });

        forgotButton.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();

            // ❗ Մաքրում ենք միայն կարևորները
            prefs.edit()
                    .remove("user_pin")
                    .putBoolean("PIN_VERIFIED", false)
                    .putBoolean("REMEMBER", false)
                    .apply();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}