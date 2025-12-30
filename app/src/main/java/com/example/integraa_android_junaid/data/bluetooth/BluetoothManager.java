package com.example.integraa_android_junaid.data.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.integraa_android_junaid.util.PermissionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothManager {
    private static final String TAG = "BluetoothManager";
    private static final UUID SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    private final Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothDevice connectedDevice;

    public BluetoothManager(Context context) {
        this.context = context;
        try {
            android.bluetooth.BluetoothManager bluetoothManager = (android.bluetooth.BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null) {
                bluetoothAdapter = bluetoothManager.getAdapter();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Bluetooth permission not granted", e);
            bluetoothAdapter = null;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing BluetoothManager", e);
            bluetoothAdapter = null;
        }
    }

    public boolean isBluetoothEnabled() {
        if (!hasBluetoothPermissions()) {
            return false;
        }
        try {
            return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
        } catch (SecurityException e) {
            Log.e(TAG, "Bluetooth permission not granted", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking Bluetooth status", e);
            return false;
        }
    }

    public List<BluetoothDeviceModel> getPairedDevices() {
        List<BluetoothDeviceModel> devices = new ArrayList<>();
        
        if (!hasBluetoothPermissions()) {
            Log.w(TAG, "Bluetooth permissions not granted");
            return devices;
        }
        
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return devices;
        }

        try {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices != null) {
                for (BluetoothDevice device : pairedDevices) {
                    try {
                        String name = device.getName();
                        String address = device.getAddress();
                        if (name != null && address != null) {
                            devices.add(new BluetoothDeviceModel(name, address, device));
                        }
                    } catch (SecurityException e) {
                        Log.w(TAG, "Permission denied accessing device info", e);
                    } catch (Exception e) {
                        Log.w(TAG, "Error processing device", e);
                    }
                }
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Bluetooth permission not granted for getBondedDevices", e);
        } catch (Exception e) {
            Log.e(TAG, "Error getting paired devices", e);
        }
        
        return devices;
    }

    private boolean hasBluetoothPermissions() {
        return PermissionHelper.hasBluetoothPermissions(context);
    }

    public void connectToDevice(BluetoothDevice device, BluetoothGattCallback callback) {
        if (bluetoothAdapter == null || device == null) {
            if (callback != null) {
                callback.onConnectionFailed("Bluetooth adapter not available");
            }
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothGatt = device.connectGatt(context, false, callback);
        } else {
            bluetoothGatt = device.connectGatt(context, false, callback);
        }
        connectedDevice = device;
    }

    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
        connectedDevice = null;
    }

    public boolean sendHexPayload(String hexPayload) {
        if (bluetoothGatt == null) {
            Log.e(TAG, "Bluetooth GATT not connected");
            return false;
        }

        try {
            // Convert hex string to byte array
            byte[] bytes = hexStringToByteArray(hexPayload);

            BluetoothGattService service = bluetoothGatt.getService(SERVICE_UUID);
            if (service == null) {
                Log.e(TAG, "Service not found");
                return false;
            }

            BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
            if (characteristic == null) {
                Log.e(TAG, "Characteristic not found");
                return false;
            }

            characteristic.setValue(bytes);
            boolean success = bluetoothGatt.writeCharacteristic(characteristic);
            if (success) {
                Log.d(TAG, "Payload sent: " + hexPayload);
            } else {
                Log.e(TAG, "Failed to write characteristic");
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error sending payload", e);
            return false;
        }
    }

    private byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    public boolean isConnected() {
        return bluetoothGatt != null && connectedDevice != null;
    }

    public BluetoothDevice getConnectedDevice() {
        return connectedDevice;
    }

    public abstract static class BluetoothGattCallback extends android.bluetooth.BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(android.bluetooth.BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
                onConnected();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                onDisconnected();
            }
        }

        @Override
        public void onServicesDiscovered(android.bluetooth.BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                onServicesDiscovered();
            } else {
                onConnectionFailed("Service discovery failed");
            }
        }

        public abstract void onConnected();
        public abstract void onDisconnected();
        public abstract void onServicesDiscovered();
        public abstract void onConnectionFailed(String error);
    }
}

