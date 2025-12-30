package com.example.integraa_android_junaid.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.integraa_android_junaid.R;
import com.example.integraa_android_junaid.data.api.models.Action;
import com.example.integraa_android_junaid.ui.login.LoginActivity;
import com.example.integraa_android_junaid.ui.settings.BluetoothSettingsFragment;
import com.example.integraa_android_junaid.util.PermissionHelper;
import com.example.integraa_android_junaid.util.WorkManagerHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private FloatingActionButton settingsFab;
    private MainPagerAdapter pagerAdapter;
    private TabLayoutMediator tabLayoutMediator;
    private long lastFabClickTime = 0;
    private static final long FAB_CLICK_THROTTLE_MS = 1000; // Prevent rapid clicks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);

            viewModel = new ViewModelProvider(this).get(MainViewModel.class);

            initViews();
            setupObservers();
            
            // Load permissions after a short delay to ensure activity is fully initialized
            if (viewPager != null) {
                viewPager.post(() -> {
                    if (viewModel != null) {
                        viewModel.loadPermissions();
                    }
                });
            } else {
                // Fallback if viewPager is null
                viewModel.loadPermissions();
            }
            
            // Schedule background permission refresh
            WorkManagerHelper.schedulePermissionRefresh(this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detach TabLayoutMediator to prevent leaks
        if (tabLayoutMediator != null) {
            tabLayoutMediator.detach();
        }
    }

    private void initViews() {
        try {
            // Initialize views first
            viewPager = findViewById(R.id.viewPager);
            tabLayout = findViewById(R.id.tabLayout);
            progressBar = findViewById(R.id.progressBar);
            errorTextView = findViewById(R.id.errorTextView);
            settingsFab = findViewById(R.id.settingsFab);
            
            // Set toolbar as action bar (only works with NoActionBar theme)
            // Wrap in try-catch to prevent crash if theme issue persists
            try {
                androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
                if (toolbar != null) {
                    setSupportActionBar(toolbar);
                }
            } catch (IllegalStateException e) {
                // Theme might still have action bar - just log and continue
                e.printStackTrace();
            }

            if (viewPager == null || tabLayout == null) {
                throw new IllegalStateException("ViewPager or TabLayout not found");
            }

            pagerAdapter = new MainPagerAdapter(this);
            viewPager.setAdapter(pagerAdapter);

            if (settingsFab != null) {
                settingsFab.setOnClickListener(v -> showSettingsDialog());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getActionGroups().observe(this, actionGroups -> {
            try {
                if (actionGroups != null && !actionGroups.isEmpty() && viewPager != null && tabLayout != null) {
                    pagerAdapter.setActionGroups(actionGroups);
                    
                    // Detach previous mediator if exists
                    if (tabLayoutMediator != null) {
                        tabLayoutMediator.detach();
                        tabLayoutMediator = null;
                    }
                    
                    // Wait for adapter to be ready before attaching mediator
                    viewPager.post(() -> {
                        try {
                            if (pagerAdapter.getItemCount() > 0) {
                                tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                                    if (position < actionGroups.size()) {
                                        tab.setText(actionGroups.get(position).getLabel());
                                    }
                                });
                                tabLayoutMediator.attach();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error setting up tabs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    
                    if (errorTextView != null) {
                        errorTextView.setVisibility(View.GONE);
                    }
                } else {
                    if (errorTextView != null) {
                        errorTextView.setVisibility(View.VISIBLE);
                        errorTextView.setText("No actions available");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (errorTextView != null) {
                    errorTextView.setVisibility(View.VISIBLE);
                    errorTextView.setText("Error loading actions: " + e.getMessage());
                }
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                if (errorTextView != null) {
                    errorTextView.setText(error);
                    errorTextView.setVisibility(View.VISIBLE);
                }
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            } else {
                if (errorTextView != null) {
                    errorTextView.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getSessionExpired().observe(this, expired -> {
            if (expired) {
                new AlertDialog.Builder(this)
                        .setTitle("Session Expired")
                        .setMessage("Your session has expired. Please login again.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            viewModel.logout();
                            Intent intent = new Intent(this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .setCancelable(false)
                        .show();
            }
        });
    }

    private void showSettingsDialog() {
        // Throttle rapid clicks
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFabClickTime < FAB_CLICK_THROTTLE_MS) {
            return; // Ignore rapid clicks
        }
        lastFabClickTime = currentTime;

        try {
            // Prevent multiple dialogs from opening
            if (getSupportFragmentManager().findFragmentByTag("BluetoothSettings") != null) {
                return; // Dialog already open
            }

            // Request Bluetooth permissions if not granted
            if (!PermissionHelper.hasBluetoothPermissions(this)) {
                PermissionHelper.requestBluetoothPermissions(this);
                Toast.makeText(this, "Please grant Bluetooth permissions to use this feature", Toast.LENGTH_LONG).show();
                return;
            }

            BluetoothSettingsFragment fragment = new BluetoothSettingsFragment();
            fragment.show(getSupportFragmentManager(), "BluetoothSettings");
        } catch (IllegalStateException e) {
            // Fragment manager not ready - ignore
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error opening settings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionHelper.BLUETOOTH_PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                Toast.makeText(this, "Bluetooth permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bluetooth permissions are required to use this feature", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            viewModel.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else if (item.getItemId() == R.id.menu_refresh) {
            viewModel.refreshPermissions();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

