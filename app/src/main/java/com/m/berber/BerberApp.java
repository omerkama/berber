package com.m.berber;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class BerberApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            FirebaseApp.initializeApp(this);
        } catch (Exception ignored) {}
    }
}