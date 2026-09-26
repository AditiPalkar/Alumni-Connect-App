package com.example.alumniconnectingapp;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class Myapplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
