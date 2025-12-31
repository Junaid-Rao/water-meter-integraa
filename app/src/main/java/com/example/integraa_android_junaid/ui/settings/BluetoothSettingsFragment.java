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
    private MaterialButton scanButton;
    private MaterialButton closeButton;
    private BluetoothDeviceAdapter adapter;
    private List<BluetoothDeviceModel> allDevices = new java.util.ArrayList<>();

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
            scanButton = view.findViewById(R.id.scanButton);
            closeButton = view.findViewById(R.id.closeButton);

            if (devicesRecyclerView == null || getContext() == null) {
                return view;
            }

            adapter = new BluetoothDeviceAdapter();
            adapter.setOnDeviceClickListener(device -> {
                if (device != null && viewModel != null) {
                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress();
                    
                    // Select the device
                    viewModel.selectDevice(device);
                    
                    // Show clear success message with device details
                    String message = "Device selected successfully!\n" + deviceName;
                    if (deviceAddress != null && !deviceAddress.isEmpty()) {
                        message += "\n(" + deviceAddress + ")";
                    }
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    
                    // Auto-close dialog after device selection
                    if (isAdded()) {
                        // Small delay to show the toast message
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            if (isAdded()) {
                                dismiss();
                            }
                        }, 500);
                    }
                }
            });

            // Use LinearLayoutManager with proper scrolling
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            devicesRecyclerView.setLayoutManager(layoutManager);
            devicesRecyclerView.setAdapter(adapter);
            // Disable nested scrolling so NestedScrollView handles it
            devicesRecyclerView.setNestedScrollingEnabled(false);
            // Add item decoration for spacing
            devicesRecyclerView.addItemDecoration(new androidx.recyclerview.widget.DividerItemDecoration(getContext(), layoutManager.getOrientation()));

            if (closeButton != null) {
                closeButton.setOnClickListener(v -> {
                    if (isAdded()) {
                        dismiss();
                    }
                });
            }

            if (scanButton != null) {
                scanButton.setOnClickListener(v -> {
                    try {
                        if (getActivity() == null || !isAdded()) {
                            return;
                        }

                        if (!com.example.integraa_android_junaid.util.PermissionHelper.hasBluetoothPermissions(getActivity())) {
                            com.example.integraa_android_junaid.util.PermissionHelper.requestBluetoothPermissions(getActivity());
                            Toast.makeText(getContext(), "Please grant Bluetooth permissions", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (viewModel == null) {
                            return;
                        }

                        Boolean isScanning = viewModel.getIsScanning().getValue();
                        if (isScanning != null && isScanning) {
                            viewModel.stopScanning();
                            scanButton.setText("Scan New Devices");
                        } else {
                            viewModel.startScanning();
                            scanButton.setText("Stop Scanning");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

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

        // Observe paired devices
        viewModel.getPairedDevices().observe(this, devices -> {
            updateDeviceList();
        });

        // Observe scanned devices
        viewModel.getScannedDevices().observe(this, devices -> {
            updateDeviceList();
        });

        // Observe scanning state
        viewModel.getIsScanning().observe(this, isScanning -> {
            if (scanButton != null && isAdded()) {
                if (isScanning != null && isScanning) {
                    scanButton.setText("Stop Scanning");
                    scanButton.setEnabled(true);
                } else {
                    scanButton.setText("Scan New Devices");
                    scanButton.setEnabled(true);
                }
            }
        });

        viewModel.getSelectedDeviceName().observe(this, name -> {
            if (selectedDeviceTextView != null && viewModel != null) {
                String address = viewModel.getSelectedDeviceAddress().getValue();
                if (name != null && !name.isEmpty() && address != null && !address.isEmpty()) {
                    // Show clear status with device name and address
                    selectedDeviceTextView.setText("✓ Selected: " + name + "\n(" + address + ")");
                    try {
                        selectedDeviceTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
                    } catch (Exception e) {
                        // Fallback if color not available
                    }
                } else {
                    selectedDeviceTextView.setText("No device selected");
                    try {
                        selectedDeviceTextView.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
                    } catch (Exception e) {
                        // Fallback if color not available
                    }
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

    private void updateDeviceList() {
        if (adapter == null || devicesRecyclerView == null || emptyTextView == null || viewModel == null) {
            return;
        }

        // Re-enable refresh button when data is received
        if (refreshButton != null && isAdded()) {
            refreshButton.setEnabled(true);
            refreshButton.setText("Refresh Paired");
        }

        // Combine paired and scanned devices
        List<BluetoothDeviceModel> paired = viewModel.getPairedDevices().getValue();
        List<BluetoothDeviceModel> scanned = viewModel.getScannedDevices().getValue();
        
        allDevices.clear();
        
        // Add paired devices first
        if (paired != null && !paired.isEmpty()) {
            allDevices.addAll(paired);
        }
        
        // Add scanned devices (avoid duplicates by address)
        if (scanned != null && !scanned.isEmpty()) {
            for (BluetoothDeviceModel scannedDevice : scanned) {
                boolean exists = false;
                for (BluetoothDeviceModel existing : allDevices) {
                    if (existing.getAddress().equals(scannedDevice.getAddress())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    allDevices.add(scannedDevice);
                }
            }
        }

        if (!allDevices.isEmpty()) {
            // Show devices
            adapter.setDevices(allDevices);
            devicesRecyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
        } else {
            // Show empty state
            adapter.setDevices(new java.util.ArrayList<>());
            devicesRecyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
            
            // Set appropriate message based on state
            String message = "No devices found.\nClick 'Refresh Paired' for paired devices or 'Scan New Devices' to discover nearby devices.";
            if (getActivity() != null && !com.example.integraa_android_junaid.util.PermissionHelper.hasBluetoothPermissions(getActivity())) {
                message = "Bluetooth permissions required.\nPlease grant permissions to view devices.";
            } else if (viewModel != null && viewModel.getIsBluetoothEnabled().getValue() != null && !viewModel.getIsBluetoothEnabled().getValue()) {
                message = "Bluetooth is disabled.\nPlease enable Bluetooth in system settings.";
            }
            emptyTextView.setText(message);
        }
    }

    @Override
    public void onDestroyView() {
        // Stop scanning when fragment is destroyed
        if (viewModel != null) {
            viewModel.stopScanning();
        }
        super.onDestroyView();
    }
}

