package com.example.alumniconnectingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class StudentDashboard extends AppCompatActivity {

    // UI
    private TextView tvGreeting, tvName, tvConnections, tvSaved, tvActive;
    private NestedScrollView svHomeContent;
    private FrameLayout fragmentContainer;
    private BottomNavigationView bottomNav;

    // Firebase
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;
    private SwipeRefreshLayout swipeRefresh;


    // Role
    private String userRole = "student";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_student_dashboard);

        firestore = FirebaseFirestore.getInstance();

        // Bind views
        tvGreeting = findViewById(R.id.tvGreeting);
        tvName = findViewById(R.id.tvName);
        tvConnections = findViewById(R.id.tvConnections);
        tvSaved = findViewById(R.id.tvSaved);
        tvActive = findViewById(R.id.tvActive);

        svHomeContent = findViewById(R.id.svHomeContent);
        fragmentContainer = findViewById(R.id.fragment_container);
        bottomNav = findViewById(R.id.bottom_navigation);
        swipeRefresh = findViewById(R.id.swipeRefresh);


        // Account icon click (must exist in XML)
        findViewById(R.id.ivAccount).setOnClickListener(v -> showAccountOptions());

        setGreeting();
        loadUserData();
        setupNavigation();
        swipeRefresh.setOnRefreshListener(() -> {
            refreshHomeData();
        });
        Log.d("DEBUG", "BottomNav = " + bottomNav);
        Log.d("DEBUG", "FragmentContainer = " + fragmentContainer);
        Log.d("DEBUG", "ScrollView = " + svHomeContent);

    }

    private void setupNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_home) {
                svHomeContent.setVisibility(View.VISIBLE);
                fragmentContainer.setVisibility(View.GONE);
                return true;
            }

            svHomeContent.setVisibility(View.GONE);
            fragmentContainer.setVisibility(View.VISIBLE);

            Fragment fragment = null;

            if (item.getItemId() == R.id.nav_jobs) fragment = new JobsAlumni();
            else if (item.getItemId() == R.id.nav_stories) fragment = new Support();
            else if (item.getItemId() == R.id.nav_chat) fragment = new Chat();
            else if (item.getItemId() == R.id.nav_memories) fragment = new Memories();

            if (fragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            return true;
        });
    }

    private void setGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) tvGreeting.setText("● Good morning");
        else if (hour < 17) tvGreeting.setText("● Good afternoon");
        else tvGreeting.setText("● Good evening");
    }

    private void loadUserData() {
        firestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String name = doc.getString("name");
                    userRole = doc.getString("role");

                    if (name != null && !name.isEmpty()) {
                        tvName.setText(name);
                    }

                    applyRoleUI();
                })
                .addOnFailureListener(e -> tvName.setText("--"));
    }

    private void applyRoleUI() {
        if ("alumni".equals(userRole)) {
            tvConnections.setText("Alumni");
            tvSaved.setText("Referrals");
            tvActive.setText("Stories");
        }
        // students keep default labels
    }

    private void showAccountOptions() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Account")
                .setItems(new String[]{"Logout", "Reset App Data"}, (dialog, which) -> {

                    if (which == 0) {
                        FirebaseAuth.getInstance().signOut();

                        getSharedPreferences("loginPrefs", MODE_PRIVATE).edit().clear().apply();
                        getSharedPreferences("onboardingPrefs", MODE_PRIVATE).edit().clear().apply();

                        Intent i = new Intent(this, Login.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    }

                    if (which == 1) {
                        getSharedPreferences("loginPrefs", MODE_PRIVATE).edit().clear().apply();
                        getSharedPreferences("onboardingPrefs", MODE_PRIVATE).edit().clear().apply();
                        Toast.makeText(this, "App data reset", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
    private void refreshHomeData() {

        setGreeting();

        // Reload user data
        firestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        userRole = doc.getString("role");

                        if (name != null && !name.isEmpty()) {
                            tvName.setText(name);
                        }
                        applyRoleUI();
                    }

                    swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    swipeRefresh.setRefreshing(false);
                });
    }

}
