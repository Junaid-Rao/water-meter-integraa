package com.example.integraa_android_junaid.ui.settings;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.integraa_android_junaid.R;
import com.example.integraa_android_junaid.data.bluetooth.BluetoothDeviceModel;
import com.example.integraa_android_junaid.data.bluetooth.BluetoothManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BluetoothSettingsFragment extends DialogFragment {
    private SettingsViewModel viewModel;
    private RecyclerView devicesRecyclerView;
    private TextView selectedDeviceTextView;
    private TextView emptyTextView;
    private MaterialButton refreshButton;
    private BluetoothDeviceAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Integraaandroidjunaid);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth_settings, container, false);

        try {
            // Check permissions first
            if (getActivity() != null && !com.example.integraa_android_junaid.util.PermissionHelper.hasBluetoothPermissions(getActivity())) {
                if (emptyTextView != null) {
                    emptyTextView.setText("Bluetooth permissions required. Please grant permissions in app settings.");
                    emptyTextView.setVisibility(View.VISIBLE);
                }
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Bluetooth permissions are required. Please grant them in app settings.", Toast.LENGTH_LONG).show();
                }
                // Still initialize views but don't load devices
            }

            viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

            devicesRecyclerView = view.findViewById(R.id.devicesRecyclerView);
            selectedDeviceTextView = view.findViewById(R.id.selectedDeviceTextView);
            emptyTextView = view.findViewById(R.id.emptyTextView);
            refreshButton = view.findViewById(R.id.refreshButton);

            if (devicesRecyclerView == null || getContext() == null) {
                return view;
            }

            adapter = new BluetoothDeviceAdapter();
            adapter.setOnDeviceClickListener(device -> {
                if (device != null && viewModel != null) {
                    viewModel.selectDevice(device);
                    Toast.makeText(getContext(), "Device selected: " + (device.getName() != null ? device.getName() : "Unknown"), Toast.LENGTH_SHORT).show();
                }
            });

            devicesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            devicesRecyclerView.setAdapter(adapter);

            if (refreshButton != null) {
                refreshButton.setOnClickListener(v -> {
                    try {
                        // Disable button to prevent multiple clicks
                        refreshButton.setEnabled(false);
                        refreshButton.setText("Refreshing...");

                        if (getActivity() == null || !isAdded()) {
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Fragment not ready", Toast.LENGTH_SHORT).show();
                            }
                            refreshButton.setEnabled(true);
                            refreshButton.setText("Refresh Devices");
                            return;
                        }

                        if (!com.example.integraa_android_junaid.util.PermissionHelper.hasBluetoothPermissions(getActivity())) {
                            com.example.integraa_android_junaid.util.PermissionHelper.requestBluetoothPermissions(getActivity());
                            Toast.makeText(getContext(), "Please grant Bluetooth permissions", Toast.LENGTH_LONG).show();
                            refreshButton.setEnabled(true);
                            refreshButton.setText("Refresh Devices");
                            return;
                        }

                        if (viewModel == null) {
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Settings not initialized. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                            refreshButton.setEnabled(true);
                            refreshButton.setText("Refresh Devices");
                            return;
                        }

                        // Force refresh by calling loadPairedDevices
                        viewModel.loadPairedDevices();

                        // Re-enable button after a delay (will also be re-enabled when data arrives)
                        refreshButton.postDelayed(() -> {
                            if (refreshButton != null && isAdded()) {
                                refreshButton.setEnabled(true);
                                refreshButton.setText("Refresh Devices");
                            }
                        }, 3000); // Re-enable after 3 seconds max

                    } catch (Exception e) {
                        e.printStackTrace();
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error refreshing devices: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                        }
                        if (refreshButton != null) {
                            refreshButton.setEnabled(true);
                            refreshButton.setText("Refresh Devices");
                        }
                    }
                });
            }

            setupObservers();
            
            // Delay loading devices slightly to ensure UI is ready and prevent ANR
            if (viewModel != null && getActivity() != null && com.example.integraa_android_junaid.util.PermissionHelper.hasBluetoothPermissions(getActivity())) {
                devicesRecyclerView.postDelayed(() -> {
                    if (viewModel != null && isAdded()) {
                        viewModel.loadPairedDevices();
                    }
                }, 100); // Small delay to ensure UI is ready
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            if (emptyTextView != null) {
                emptyTextView.setText("Bluetooth permission denied. Please grant permissions in app settings.");
                emptyTextView.setVisibility(View.VISIBLE);
            }
            if (getContext() != null) {
                Toast.makeText(getContext(), "Bluetooth permission denied. Please grant permissions.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (emptyTextView != null) {
                emptyTextView.setText("Error initializing Bluetooth settings: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
                emptyTextView.setVisibility(View.VISIBLE);
            }
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error initializing Bluetooth settings: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
            }
        }

        return view;
    }

    private void setupObservers() {
        if (viewModel == null) {
            return;
        }

        viewModel.getPairedDevices().observe(this, devices -> {
            if (adapter == null || devicesRecyclerView == null || emptyTextView == null) {
                return;
            }

            if (devices != null && !devices.isEmpty()) {
                // Show devices
                adapter.setDevices(devices);
                devicesRecyclerView.setVisibility(View.VISIBLE);
                emptyTextView.setVisibility(View.GONE);
            } else {
                // Show empty state
                adapter.setDevices(new java.util.ArrayList<>());
                devicesRecyclerView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
                
                // Set appropriate message based on state
                String message = "No paired devices found.\nPlease pair a Bluetooth device in system settings first.";
                if (getActivity() != null && !com.example.integraa_android_junaid.util.PermissionHelper.hasBluetoothPermissions(getActivity())) {
                    message = "Bluetooth permissions required.\nPlease grant permissions to view devices.";
                } else if (viewModel != null && viewModel.getIsBluetoothEnabled().getValue() != null && !viewModel.getIsBluetoothEnabled().getValue()) {
                    message = "Bluetooth is disabled.\nPlease enable Bluetooth in system settings.";
                }
                emptyTextView.setText(message);
            }
        });

        viewModel.getSelectedDeviceName().observe(this, name -> {
            if (selectedDeviceTextView != null && viewModel != null) {
                String address = viewModel.getSelectedDeviceAddress().getValue();
                if (name != null && address != null) {
                    selectedDeviceTextView.setText("Selected: " + name + " (" + address + ")");
                } else {
                    selectedDeviceTextView.setText("No device selected");
                }
            }
        });

        viewModel.getIsBluetoothEnabled().observe(this, enabled -> {
            if (enabled == null || !enabled) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Please enable Bluetooth in system settings", Toast.LENGTH_LONG).show();
                }
                // Update empty text if no devices
                if (viewModel != null && (viewModel.getPairedDevices().getValue() == null || viewModel.getPairedDevices().getValue().isEmpty())) {
                    if (emptyTextView != null) {
                        emptyTextView.setVisibility(View.VISIBLE);
                        emptyTextView.setText("Bluetooth is disabled. Please enable Bluetooth in system settings.");
                    }
                }
            }
            // Don't auto-load devices here to prevent loops - let user click refresh or load on init
        });
    }
}

