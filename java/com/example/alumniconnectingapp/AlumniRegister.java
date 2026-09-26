package com.example.alumniconnectingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlumniRegister extends AppCompatActivity {

    // Existing fields (UNCHANGED)
    private TextInputEditText etName, etEmail, etInstitute,
            etState, etCity, etPassword, etConfirmPassword;
    private AutoCompleteTextView autoCompleteYear, autoCompleteCountry;
    private MaterialButton btnSignUp;
    private TextView tvLogin;

    // New (REQUIRED for XML)
    private View layoutStateCity;

    private FirebaseAuth mAuth;

    // Country → State → City data
    private final Map<String, List<String>> countryStateMap = new HashMap<>();
    private final Map<String, List<String>> stateCityMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alumni_register);

        mAuth = FirebaseAuth.getInstance();

        // Bind views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etInstitute = findViewById(R.id.etInstitute);

        autoCompleteYear = findViewById(R.id.autoCompleteYear);
        autoCompleteCountry = findViewById(R.id.autoCompleteCountry);

        etState = findViewById(R.id.etState);
        etCity = findViewById(R.id.etCity);
        layoutStateCity = findViewById(R.id.layoutStateCity);

        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);

        setupYearDropdown();
        setupLocationData();
        setupCountryDropdown();

        btnSignUp.setOnClickListener(v -> registerAlumni());

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(AlumniRegister.this, Login.class));
            finish();
        });
    }

    // ---------------- YEAR DROPDOWN ----------------
    private void setupYearDropdown() {
        ArrayList<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        for (int year = currentYear; year >= currentYear - 50; year--) {
            years.add(String.valueOf(year));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                years
        );

        autoCompleteYear.setAdapter(adapter);
        autoCompleteYear.setOnClickListener(v -> autoCompleteYear.showDropDown());
    }

    // ---------------- LOCATION DATA ----------------
    private void setupLocationData() {

        countryStateMap.put("India", Arrays.asList("Maharashtra", "Karnataka"));
        countryStateMap.put("USA", Arrays.asList("California", "Texas"));

        stateCityMap.put("Maharashtra", Arrays.asList("Mumbai", "Pune", "Nagpur"));
        stateCityMap.put("Karnataka", Arrays.asList("Bangalore", "Mysuru"));
        stateCityMap.put("California", Arrays.asList("Los Angeles", "San Francisco"));
        stateCityMap.put("Texas", Arrays.asList("Houston", "Dallas"));
    }

    // ---------------- COUNTRY DROPDOWN ----------------
    private void setupCountryDropdown() {

        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(countryStateMap.keySet())
        );

        autoCompleteCountry.setAdapter(countryAdapter);

        autoCompleteCountry.setOnItemClickListener((parent, view, position, id) -> {
            String country = parent.getItemAtPosition(position).toString();

            etState.setText("");
            etCity.setText("");

            layoutStateCity.setVisibility(View.VISIBLE);
            setupStateDropdown(country);
        });
    }

    // ---------------- STATE DROPDOWN ----------------
    private void setupStateDropdown(String country) {

        List<String> states = countryStateMap.get(country);
        if (states == null) return;

        etState.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setItems(states.toArray(new String[0]), (dialog, which) -> {
                        String state = states.get(which);
                        etState.setText(state);
                        etCity.setText("");
                        setupCityDropdown(state);
                    })
                    .show();
        });
    }

    // ---------------- CITY DROPDOWN ----------------
    private void setupCityDropdown(String state) {

        List<String> cities = stateCityMap.get(state);
        if (cities == null) return;

        etCity.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setItems(cities.toArray(new String[0]), (dialog, which) ->
                            etCity.setText(cities.get(which)))
                    .show();
        });
    }

    // ---------------- REGISTER ALUMNI ----------------
    private void registerAlumni() {

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String institute = etInstitute.getText().toString().trim();
        String passedOutYear = autoCompleteYear.getText().toString().trim();
        String country = autoCompleteCountry.getText().toString().trim();
        String state = etState.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(institute) || TextUtils.isEmpty(passedOutYear) ||
                TextUtils.isEmpty(country) || TextUtils.isEmpty(state) ||
                TextUtils.isEmpty(city) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(confirmPassword)) {

            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSignUp.setEnabled(false);
        btnSignUp.setText("Creating account...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    FirebaseUser user = authResult.getUser();
                    if (user == null) return;

                    String uid = user.getUid();

                    Map<String, Object> alumni = new HashMap<>();
                    alumni.put("name", name);
                    alumni.put("email", email);
                    alumni.put("role", "alumni");
                    alumni.put("status", "pending");
                    alumni.put("institute", institute);
                    alumni.put("passoutYear", passedOutYear);
                    alumni.put("country", country);
                    alumni.put("state", state);
                    alumni.put("city", city);

                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .set(alumni)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, Login.class));
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e -> {
                    btnSignUp.setEnabled(true);
                    btnSignUp.setText("Register");
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
