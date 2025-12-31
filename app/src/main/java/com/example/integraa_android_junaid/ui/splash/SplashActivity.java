package com.example.integraa_android_junaid.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.integraa_android_junaid.R;
import com.example.integraa_android_junaid.data.local.SharedPreferencesManager;
import com.example.integraa_android_junaid.ui.login.LoginActivity;
import com.example.integraa_android_junaid.ui.main.MainActivity;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY = 2000; // 2 seconds

    @Inject
    SharedPreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Navigate after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            navigateToNextScreen();
        }, SPLASH_DELAY);
    }

    private void navigateToNextScreen() {
        Intent intent;
        
        // Check if user is already logged in
        if (preferencesManager.getToken() != null && !preferencesManager.getToken().isEmpty()) {
            // User is logged in, go to main activity
            intent = new Intent(this, MainActivity.class);
        } else {
            // User is not logged in, go to login activity
            intent = new Intent(this, LoginActivity.class);
        }
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

