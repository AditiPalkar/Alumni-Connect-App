package com.example.alumniconnectingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(this::decideNextScreen, SPLASH_DELAY);
    }

    private void decideNextScreen() {

        SharedPreferences loginPrefs =
                getSharedPreferences("loginPrefs", MODE_PRIVATE);

        SharedPreferences onboardingPrefs =
                getSharedPreferences("onboardingPrefs", MODE_PRIVATE);

        boolean rememberMe = loginPrefs.getBoolean("remember", false);
        boolean onboardingDone =
                onboardingPrefs.getBoolean("onboarding_completed", false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        if (!onboardingDone) {
            startActivity(new Intent(this, Onboarding1.class));
            finish();
            return;
        }

        if (!rememberMe) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        redirectUserByRole(user.getUid());
    }

    private void redirectUserByRole(String uid) {

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        goToLogin();
                        return;
                    }

                    String role = doc.getString("role");

                    if ("student".equalsIgnoreCase(role)) {
                        startActivity(new Intent(this, StudentDashboard.class));
                    } else if ("alumni".equalsIgnoreCase(role)) {
                        startActivity(new Intent(this, AlumniDashboard.class));
                    } else if ("admin".equalsIgnoreCase(role)) {
                        startActivity(new Intent(this, AdminDashboard.class));
                    } else {
                        goToLogin();
                        return;
                    }

                    finish();
                })
                .addOnFailureListener(e -> goToLogin());
    }

    private void goToLogin() {
        startActivity(new Intent(this, Login.class));
        finish();
    }
}