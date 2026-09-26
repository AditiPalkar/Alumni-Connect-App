package com.example.alumniconnectingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class RoleChoose extends AppCompatActivity {

    View btnStudent, btnAlumni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_choose);

        btnStudent = findViewById(R.id.btnStudent);
        btnAlumni = findViewById(R.id.btnAlumni);

        btnStudent.setOnClickListener(v -> {
            Intent intent = new Intent(RoleChoose.this, StudentRegister.class);
            startActivity(intent);
        });

        btnAlumni.setOnClickListener(v -> {
            Intent intent = new Intent(RoleChoose.this, AlumniRegister.class);
            startActivity(intent);
        });
    }
}