package com.example.integraa_android_junaid.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.integraa_android_junaid.data.bluetooth.BluetoothDeviceModel;
import com.example.integraa_android_junaid.data.bluetooth.BluetoothManager;
import com.example.integraa_android_junaid.data.local.SharedPreferencesManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SettingsViewModel extends ViewModel {
    private final BluetoothManager bluetoothManager;
    private final SharedPreferencesManager preferencesManager;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean isLoadingDevices = false;

    private final MutableLiveData<List<BluetoothDeviceModel>> pairedDevices = new MutableLiveData<>();
    private final MutableLiveData<List<BluetoothDeviceModel>> scannedDevices = new MutableLiveData<>();
    private final MutableLiveData<String> selectedDeviceName = new MutableLiveData<>();
    private final MutableLiveData<String> selectedDeviceAddress = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isBluetoothEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isScanning = new MutableLiveData<>(false);

    @Inject
    public SettingsViewModel(BluetoothManager bluetoothManager, SharedPreferencesManager preferencesManager) {
        this.bluetoothManager = bluetoothManager;
        this.preferencesManager = preferencesManager;
        loadSelectedDevice();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

    public LiveData<List<BluetoothDeviceModel>> getPairedDevices() {
        return pairedDevices;
    }

    public LiveData<String> getSelectedDeviceName() {
        return selectedDeviceName;
    }

    public LiveData<String> getSelectedDeviceAddress() {
        return selectedDeviceAddress;
    }

    public LiveData<Boolean> getIsBluetoothEnabled() {
        return isBluetoothEnabled;
    }

    public LiveData<List<BluetoothDeviceModel>> getScannedDevices() {
        return scannedDevices;
    }

    public LiveData<Boolean> getIsScanning() {
        return isScanning;
    }

    public void loadPairedDevices() {
        // Prevent multiple simultaneous calls - but allow if previous call completed
        if (isLoadingDevices) {
            android.util.Log.d("SettingsViewModel", "Already loading devices, skipping...");
            return;
        }

        isLoadingDevices = true;
        android.util.Log.d("SettingsViewModel", "Loading paired devices...");
        
        // Check Bluetooth status on main thread (quick operation)
        boolean enabled = false;
        try {
            enabled = bluetoothManager.isBluetoothEnabled();
        } catch (Exception e) {
            android.util.Log.e("SettingsViewModel", "Error checking Bluetooth status", e);
        }
        isBluetoothEnabled.postValue(enabled);

        if (!enabled) {
            android.util.Log.w("SettingsViewModel", "Bluetooth is not enabled");
            pairedDevices.postValue(new java.util.ArrayList<>());
            isLoadingDevices = false;
            return;
        }

        // Load devices on background thread to prevent ANR
        executorService.execute(() -> {
            try {
                android.util.Log.d("SettingsViewModel", "Fetching paired devices from BluetoothManager...");
                List<BluetoothDeviceModel> devices = bluetoothManager.getPairedDevices();
                android.util.Log.d("SettingsViewModel", "Found " + (devices != null ? devices.size() : 0) + " devices");
                pairedDevices.postValue(devices != null ? devices : new java.util.ArrayList<>());
            } catch (Exception e) {
                android.util.Log.e("SettingsViewModel", "Error loading paired devices", e);
                e.printStackTrace();
                pairedDevices.postValue(new java.util.ArrayList<>());
            } finally {
                isLoadingDevices = false;
                android.util.Log.d("SettingsViewModel", "Device loading completed");
            }
        });
    }

    public void startScanning() {
        if (isScanning.getValue() != null && isScanning.getValue()) {
            android.util.Log.d("SettingsViewModel", "Already scanning, skipping...");
            return;
        }

        isScanning.setValue(true);
        scannedDevices.setValue(new java.util.ArrayList<>());

        bluetoothManager.startScanning(new BluetoothManager.BluetoothScanCallback() {
            @Override
            public void onDeviceFound(BluetoothDeviceModel device) {
                List<BluetoothDeviceModel> currentDevices = scannedDevices.getValue();
                if (currentDevices == null) {
                    currentDevices = new java.util.ArrayList<>();
                }
                
                // Check if device already exists
                boolean exists = false;
                for (BluetoothDeviceModel existing : currentDevices) {
                    if (existing.getAddress().equals(device.getAddress())) {
                        exists = true;
                        break;
                    }
                }
                
                if (!exists) {
                    currentDevices.add(device);
                    scannedDevices.postValue(new java.util.ArrayList<>(currentDevices));
                }
            }

            @Override
            public void onScanFinished() {
                isScanning.postValue(false);
                android.util.Log.d("SettingsViewModel", "Scan finished");
            }

            @Override
            public void onScanError(String error) {
                isScanning.postValue(false);
                android.util.Log.e("SettingsViewModel", "Scan error: " + error);
            }
        });
    }

    public void stopScanning() {
        bluetoothManager.stopScanning();
        isScanning.setValue(false);
    }

    public void selectDevice(BluetoothDeviceModel device) {
        if (device != null) {
            preferencesManager.saveBluetoothDevice(device.getName(), device.getAddress());
            selectedDeviceName.setValue(device.getName());
            selectedDeviceAddress.setValue(device.getAddress());
            
            // Stop scanning when device is selected
            stopScanning();
        }
    }

    private void loadSelectedDevice() {
        String name = preferencesManager.getBluetoothDeviceName();
        String address = preferencesManager.getBluetoothDeviceAddress();
        selectedDeviceName.setValue(name);
        selectedDeviceAddress.setValue(address);
    }
}

