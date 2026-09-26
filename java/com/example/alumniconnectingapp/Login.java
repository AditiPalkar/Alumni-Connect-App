package com.example.alumniconnectingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

public class Login extends AppCompatActivity {

    private EditText emailLogin, passwordLogin;
    private MaterialButton signInButton, adminLoginButton;
    private TextView createAccountText, adminModeText;
    private CheckBox checkboxRemember;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private SharedPreferences loginPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences themePrefs = getSharedPreferences("themePrefs", MODE_PRIVATE);
        boolean isDarkMode = themePrefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        setContentView(R.layout.activity_login);

        emailLogin = findViewById(R.id.emailLogin);
        passwordLogin = findViewById(R.id.passwordLogin);
        signInButton = findViewById(R.id.signInButton);
        adminLoginButton = findViewById(R.id.adminLoginButton);
        createAccountText = findViewById(R.id.createAccountText);
        checkboxRemember = findViewById(R.id.checkbox_remember);
        adminModeText = findViewById(R.id.adminModeText);

        loginPrefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loadSavedCredentials();

        signInButton.setOnClickListener(v -> {
            adminModeText.setVisibility(View.GONE);
            login(false);
        });

        adminLoginButton.setOnClickListener(v -> {
            adminModeText.setVisibility(View.VISIBLE);
            login(true);
        });

        createAccountText.setOnClickListener(v ->
                startActivity(new Intent(Login.this, RoleChoose.class))
        );
    }

    private void login(boolean isAdminLogin) {

        getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        String email = emailLogin.getText().toString().trim();
        String pass = passwordLogin.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLogin.setError("Enter valid email");
            return;
        }

        if (pass.isEmpty()) {
            passwordLogin.setError("Password required");
            return;
        }

        disableButtons();

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {

                    // Save remember state
                    if (checkboxRemember.isChecked()) {
                        loginPrefs.edit()
                                .putBoolean("remember", true)
                                .putString("email", email)
                                .apply();
                    } else {
                        loginPrefs.edit().clear().apply();
                    }

                    if (isAdminLogin) {
                        if (!email.equals("admin123@gmail.com")) {
                            Toast.makeText(this,
                                    "Not an admin account",
                                    Toast.LENGTH_LONG).show();
                            FirebaseAuth.getInstance().signOut();
                            enableButtons();
                            return;
                        }

                        startActivity(new Intent(this, AdminDashboard.class));
                        finish();
                        return;
                    }

                    redirectUserByRole();
                })
                .addOnFailureListener(e -> {
                    enableButtons();
                    Toast.makeText(this,
                            "Invalid email or password",
                            Toast.LENGTH_LONG).show();
                });
    }

    private void redirectUserByRole() {

        String uid = auth.getCurrentUser().getUid();

        firestore.collection("users")
                .document(uid)
                .get(Source.SERVER)
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(this,
                                "User record not found",
                                Toast.LENGTH_LONG).show();
                        FirebaseAuth.getInstance().signOut();
                        enableButtons();
                        return;
                    }

                    String role = doc.getString("role");
                    String status = doc.getString("status");

                    if (role == null) {
                        Toast.makeText(this,
                                "Role missing. Contact support.",
                                Toast.LENGTH_LONG).show();
                        FirebaseAuth.getInstance().signOut();
                        enableButtons();
                        return;
                    }

                    SharedPreferences onboardingPrefs =
                            getSharedPreferences("onboardingPrefs", MODE_PRIVATE);

                    boolean onboardingDone =
                            onboardingPrefs.getBoolean("onboarding_completed", false);

                    if (!onboardingDone) {
                        startActivity(new Intent(this, Onboarding1.class));
                        finish();
                        return;
                    }

                    // Save role locally
                    SharedPreferences.Editor editor =
                            getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
                    editor.putString("userRole", role);
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();

                    // Role-based navigation
                    if ("student".equalsIgnoreCase(role)) {
                        startActivity(new Intent(this, StudentDashboard.class));
                        finish();
                        return;
                    }

                    if ("alumni".equalsIgnoreCase(role)) {

                        if (!"approved".equalsIgnoreCase(status)) {
                            Toast.makeText(this,
                                    "Waiting for admin approval",
                                    Toast.LENGTH_LONG).show();
                            FirebaseAuth.getInstance().signOut();
                            enableButtons();
                            return;
                        }

                        startActivity(new Intent(this, AlumniDashboard.class));
                        finish();
                        return;
                    }

                    if ("admin".equalsIgnoreCase(role)) {
                        startActivity(new Intent(this, AdminDashboard.class));
                        finish();
                        return;
                    }

                    Toast.makeText(this,
                            "Invalid role assigned",
                            Toast.LENGTH_LONG).show();
                    FirebaseAuth.getInstance().signOut();
                    enableButtons();
                })
                .addOnFailureListener(e -> {
                    enableButtons();
                    Toast.makeText(this,
                            "Network error. Try again.",
                            Toast.LENGTH_LONG).show();
                });
    }

    private void disableButtons() {
        signInButton.setEnabled(false);
        adminLoginButton.setEnabled(false);
    }

    private void enableButtons() {
        signInButton.setEnabled(true);
        adminLoginButton.setEnabled(true);
    }

    private void loadSavedCredentials() {
        if (loginPrefs.getBoolean("remember", false)) {
            emailLogin.setText(loginPrefs.getString("email", ""));
            checkboxRemember.setChecked(true);
        }
    }
}