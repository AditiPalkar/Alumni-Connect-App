package com.example.alumniconnectingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class StudentRegister extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etInstitute, etPassword, etConfirmPassword;
    private MaterialButton btnSignUp;
    private TextView tvLogin;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_register);

        mAuth = FirebaseAuth.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etInstitute = findViewById(R.id.etInstitute);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);

        btnSignUp.setOnClickListener(v -> registerStudent());

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(StudentRegister.this, Login.class));
            finish();
        });
    }

    private void registerStudent() {

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String institute = etInstitute.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(institute) ||
                TextUtils.isEmpty(password) ||
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
                    if (user == null) {
                        Toast.makeText(this, "User creation failed", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = user.getUid();

                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    Map<String, Object> student = new HashMap<>();
                    student.put("name", name);
                    student.put("email", email);
                    student.put("institute", institute);
                    student.put("role", "student");
                    student.put("status", "approved"); // students auto-approved

                    db.collection("users")
                            .document(uid)
                            .set(student)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this,
                                        "Student Registered Successfully",
                                        Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(StudentRegister.this, Login.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                btnSignUp.setEnabled(true);
                                btnSignUp.setText("Sign Up");
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    btnSignUp.setEnabled(true);
                    btnSignUp.setText("Sign Up");
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
