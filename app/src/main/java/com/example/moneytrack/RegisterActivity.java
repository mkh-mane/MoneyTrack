package com.example.moneytrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText email, password, confirmPassword;
    private Button registerButton;
    private CheckBox rememberMe;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("MoneyTrackPrefs", MODE_PRIVATE);
        email = findViewById(R.id.registerEmail);
        password = findViewById(R.id.registerPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
        rememberMe = findViewById(R.id.rememberMe);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();
        String confirmPass = confirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(userEmail)) {
            email.setError("Enter email");
            return;
        }

        if (TextUtils.isEmpty(userPassword)) {
            password.setError("Enter password");
            return;
        }

        if (TextUtils.isEmpty(confirmPass)) {
            confirmPassword.setError("Confirm your password");
            return;
        }

        if (!userPassword.equals(confirmPass)) {
            confirmPassword.setError("Passwords do not match");
            return;
        }

        if (userPassword.length() < 6) {
            password.setError("Password must be at least 6 characters");
            return;
        }

        mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        String userId = mAuth.getCurrentUser().getUid();

                        // 🔹 Firestore
                        Map<String, Object> user = new HashMap<>();
                        user.put("email", userEmail);
                        user.put("balance", 0);
                        user.put("createdAt", System.currentTimeMillis());

                        db.collection("users")
                                .document(userId)
                                .set(user)
                                .addOnSuccessListener(unused -> {

                                    // ✅ Remember Me պահում
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putBoolean("REMEMBER", rememberMe.isChecked());
                                    editor.apply();

                                    Toast.makeText(this,
                                            "Registration Successful!",
                                            Toast.LENGTH_SHORT).show();

                                    // 👉 անցնում ենք PIN setup
                                    startActivity(new Intent(this, CreatePinActivity.class));
                                    finish();

                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this,
                                            "Firestore Error: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });

                    } else {
                        Toast.makeText(this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}