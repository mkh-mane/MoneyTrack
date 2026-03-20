package com.example.moneytrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            }

            if (item.getItemId() == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                finish();
                return true;
            }

            if (item.getItemId() == R.id.nav_analyze) {
                startActivity(new Intent(this, AnalyzeActivity.class));
                finish();
                return true;
            }

            return item.getItemId() == R.id.nav_profile;
        });

        logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logoutUser())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logoutUser() {

        // Firebase logout
        FirebaseAuth.getInstance().signOut();

        // Clear SharedPreferences (Remember + PIN + PIN_VERIFIED)
        SharedPreferences prefs = getSharedPreferences("MoneyTrackPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("REMEMBER", false);
        editor.putBoolean("PIN_VERIFIED", false);
        editor.remove("user_pin");
        editor.apply();

        // Go to RegisterActivity
        Intent intent = new Intent(ProfileActivity.this, RegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}