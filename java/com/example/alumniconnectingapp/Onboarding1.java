package com.example.alumniconnectingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Onboarding1 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_1);

        Button btnNext = findViewById(R.id.btnNext);
        Button btnSkip = findViewById(R.id.btnSkip);

        btnNext.setOnClickListener(v ->
                startActivity(new Intent(this, Onboarding2.class))
        );

        btnSkip.setOnClickListener(v -> {

            getSharedPreferences("onboardingPrefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("onboarding_completed", true)
                    .apply();

            redirectUserByRole();
        });
    }

    private void redirectUserByRole() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    String role = doc.getString("role");

                    if ("student".equalsIgnoreCase(role)) {
                        startActivity(new Intent(this, StudentDashboard.class));
                    } else if ("alumni".equalsIgnoreCase(role)) {
                        startActivity(new Intent(this, AlumniDashboard.class));
                    } else {
                        startActivity(new Intent(this, Login.class));
                    }

                    finish();
                });
    }
}