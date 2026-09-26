package com.example.alumniconnectingapp;

import android.content.Intent;
import android.os.Bundle;
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

public class AlumniDashboard extends AppCompatActivity {

    // UI
    private TextView tvStudents, tvJobs, tvStories;
    private NestedScrollView svHomeContent;
    private FrameLayout fragmentContainer;
    private BottomNavigationView bottomNav;
    private SwipeRefreshLayout swipeRefresh;

    // Firebase
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_alumni_dashboard);

        firestore = FirebaseFirestore.getInstance();

        // Bind views
        tvStudents = findViewById(R.id.tvStudents);
        tvJobs = findViewById(R.id.tvJobs);
        tvStories = findViewById(R.id.tvStories);

        svHomeContent = findViewById(R.id.svHomeContent);
        fragmentContainer = findViewById(R.id.fragment_container);
        bottomNav = findViewById(R.id.bottom_navigation);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        findViewById(R.id.ivAccount).setOnClickListener(v -> showAccountOptions());

        loadCounts();
        setupNavigation();

        swipeRefresh.setOnRefreshListener(this::refreshHomeData);
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

    private void loadCounts() {

        firestore.collection("connections")
                .get()
                .addOnSuccessListener(snapshot ->
                        tvStudents.setText(String.valueOf(snapshot.size())));

        firestore.collection("jobs")
                .get()
                .addOnSuccessListener(snapshot ->
                        tvJobs.setText(String.valueOf(snapshot.size())));

        firestore.collection("stories")
                .get()
                .addOnSuccessListener(snapshot ->
                        tvStories.setText(String.valueOf(snapshot.size())));
    }

    private void refreshHomeData() {
        loadCounts();
        swipeRefresh.setRefreshing(false);
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
}