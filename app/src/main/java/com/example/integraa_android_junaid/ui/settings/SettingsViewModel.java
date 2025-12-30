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
    private final MutableLiveData<String> selectedDeviceName = new MutableLiveData<>();
    private final MutableLiveData<String> selectedDeviceAddress = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isBluetoothEnabled = new MutableLiveData<>();

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

    public void loadPairedDevices() {
        // Prevent multiple simultaneous calls
        if (isLoadingDevices) {
            return;
        }

        isLoadingDevices = true;
        
        // Check Bluetooth status on main thread (quick operation)
        boolean enabled = bluetoothManager.isBluetoothEnabled();
        isBluetoothEnabled.postValue(enabled);

        if (!enabled) {
            pairedDevices.postValue(null);
            isLoadingDevices = false;
            return;
        }

        // Load devices on background thread to prevent ANR
        executorService.execute(() -> {
            try {
                List<BluetoothDeviceModel> devices = bluetoothManager.getPairedDevices();
                pairedDevices.postValue(devices);
            } catch (Exception e) {
                e.printStackTrace();
                pairedDevices.postValue(null);
            } finally {
                isLoadingDevices = false;
            }
        });
    }

    public void selectDevice(BluetoothDeviceModel device) {
        if (device != null) {
            preferencesManager.saveBluetoothDevice(device.getName(), device.getAddress());
            selectedDeviceName.setValue(device.getName());
            selectedDeviceAddress.setValue(device.getAddress());
        }
    }

    private void loadSelectedDevice() {
        String name = preferencesManager.getBluetoothDeviceName();
        String address = preferencesManager.getBluetoothDeviceAddress();
        selectedDeviceName.setValue(name);
        selectedDeviceAddress.setValue(address);
    }
}

