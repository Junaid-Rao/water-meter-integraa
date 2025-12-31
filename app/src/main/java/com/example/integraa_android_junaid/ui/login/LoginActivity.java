package com.example.integraa_android_junaid.ui.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.integraa_android_junaid.R;
import com.example.integraa_android_junaid.data.api.models.LoginResponse;
import com.example.integraa_android_junaid.ui.main.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private LoginViewModel viewModel;
    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;
    private MaterialButton loginButton;
    private ProgressBar progressBar;
    private TextView errorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Hide the action bar for login screen
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        initViews();
        setupObservers();
        requestLocationPermission();
    }

    private void initViews() {
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        errorTextView = findViewById(R.id.errorTextView);

        loginButton.setOnClickListener(v -> attemptLogin());
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            loginButton.setEnabled(!isLoading);
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getLoginSuccess().observe(this, this::onLoginSuccess);

        viewModel.getLoginError().observe(this, error -> {
            if (error != null) {
                errorTextView.setText(error);
                errorTextView.setVisibility(View.VISIBLE);
            } else {
                errorTextView.setVisibility(View.GONE);
            }
        });
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.error_location_permission), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void attemptLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty()) {
            usernameEditText.setError(getString(R.string.error_username_required));
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError(getString(R.string.error_password_required));
            return;
        }

        // Get location
        double latitude = 0.0;
        double longitude = 0.0;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            android.location.LocationManager locationManager =
                    (android.location.LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager != null) {
                Location location = null;
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    location = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
                    if (location == null) {
                        location = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
                    }
                }
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }
        }

        viewModel.login(username, password, latitude, longitude);
    }

    private void onLoginSuccess(LoginResponse response) {
        // Ensure navigation happens on main thread
        runOnUiThread(() -> {
            try {
                // Small delay to ensure token is saved to SharedPreferences
                loginButton.postDelayed(() -> {
                    try {
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, getString(R.string.error_login_failed), Toast.LENGTH_LONG).show();
                    }
                }, 200);
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback without delay
                try {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(this, getString(R.string.error_login_failed), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

