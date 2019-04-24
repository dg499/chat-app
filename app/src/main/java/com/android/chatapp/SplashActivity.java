package com.android.chatapp;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class SplashActivity extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH = 1500;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash);
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                mUserDatabase.child("online");
                AppHelper.LaunchActivityfinish(SplashActivity.this, DashboardActivity.class);
            } else {
                AppHelper.LaunchActivityfinish(SplashActivity.this, LoginActivity.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AppHelper.LaunchActivityfinish(SplashActivity.this, LoginActivity.class);
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
