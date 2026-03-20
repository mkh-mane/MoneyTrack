package com.example.moneytrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CreatePinActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pin);

        EditText pinEditText = findViewById(R.id.pinEditText);
        Button savePinButton = findViewById(R.id.savePinButton);

        savePinButton.setOnClickListener(v -> {

            String pin = pinEditText.getText().toString();

            if (pin.length() < 4) {
                Toast.makeText(this, "PIN must be 4 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("MoneyTrackPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("user_pin", pin);
            editor.putBoolean("PIN_VERIFIED", true);

            editor.apply();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}