package com.example.integraa_android_junaid.data.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.integraa_android_junaid.util.PermissionHelper;

import java.util.ArrayList;
import java.util.HashSet;
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
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothScanCallback scanCallback;
    private Set<String> scannedDeviceAddresses = new HashSet<>();
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final long SCAN_DURATION_MS = 10000; // 10 seconds
    private static final long CONNECTION_TIMEOUT_MS = 15000; // 15 seconds
    private Handler connectionTimeoutHandler;
    private BluetoothGattCallback currentConnectionCallback;

    public BluetoothManager(Context context) {
        this.context = context;
        try {
            android.bluetooth.BluetoothManager bluetoothManager = (android.bluetooth.BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null) {
                bluetoothAdapter = bluetoothManager.getAdapter();
                if (bluetoothAdapter != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                }
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
                        String name = null;
                        String address = null;
                        
                        try {
                            name = device.getName();
                            address = device.getAddress();
                        } catch (SecurityException e) {
                            Log.w(TAG, "Permission denied accessing device info", e);
                        }
                        
                        if (address != null && !address.isEmpty()) {
                            // If name is null or empty, use address as fallback
                            if (name == null || name.isEmpty()) {
                                name = "Device (" + address + ")";
                            }
                            devices.add(new BluetoothDeviceModel(name, address, device));
                        }
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

        // Disconnect existing connection if any
        if (bluetoothGatt != null) {
            disconnect();
        }

        // Cancel any existing connection timeout
        cancelConnectionTimeout();

        // Store callback for timeout handling
        currentConnectionCallback = callback;

        // Set BluetoothManager reference in callback
        if (callback != null) {
            callback.setBluetoothManager(this);
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                bluetoothGatt = device.connectGatt(context, false, callback);
            } else {
                bluetoothGatt = device.connectGatt(context, false, callback);
            }
            connectedDevice = device;
            Log.d(TAG, "Connecting to device: " + device.getAddress());

            // Start connection timeout
            startConnectionTimeout(callback);
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied connecting to device", e);
            cancelConnectionTimeout();
            if (callback != null) {
                callback.onConnectionFailed("Permission denied");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error connecting to device", e);
            cancelConnectionTimeout();
            if (callback != null) {
                callback.onConnectionFailed("Connection error: " + e.getMessage());
            }
        }
    }

    private void startConnectionTimeout(BluetoothGattCallback callback) {
        cancelConnectionTimeout();
        connectionTimeoutHandler = new Handler(Looper.getMainLooper());
        connectionTimeoutHandler.postDelayed(() -> {
            if (bluetoothGatt != null && !isConnected()) {
                Log.w(TAG, "Connection timeout - disconnecting");
                disconnect();
                if (callback != null) {
                    callback.onConnectionFailed("Connection timeout. Device may be out of range or not responding.");
                }
                currentConnectionCallback = null;
            }
        }, CONNECTION_TIMEOUT_MS);
    }

    private void cancelConnectionTimeout() {
        if (connectionTimeoutHandler != null) {
            connectionTimeoutHandler.removeCallbacksAndMessages(null);
            connectionTimeoutHandler = null;
        }
    }

    public void connectToDeviceByAddress(String address, BluetoothGattCallback callback) {
        if (bluetoothAdapter == null || address == null || address.isEmpty()) {
            if (callback != null) {
                callback.onConnectionFailed("Invalid device address");
            }
            return;
        }

        try {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            connectToDevice(device, callback);
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied getting remote device", e);
            if (callback != null) {
                callback.onConnectionFailed("Permission denied");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting remote device", e);
            if (callback != null) {
                callback.onConnectionFailed("Error: " + e.getMessage());
            }
        }
    }

    public void disconnect() {
        cancelConnectionTimeout();
        if (bluetoothGatt != null) {
            try {
                bluetoothGatt.disconnect();
                bluetoothGatt.close();
            } catch (Exception e) {
                Log.e(TAG, "Error disconnecting", e);
            }
            bluetoothGatt = null;
        }
        connectedDevice = null;
        currentConnectionCallback = null;
    }

    /**
     * Send hex payload to connected Bluetooth device
     * @param hexPayload Hex string payload to send
     * @return Result object with success status and error message
     */
    public SendResult sendHexPayload(String hexPayload) {
        if (hexPayload == null || hexPayload.isEmpty()) {
            return new SendResult(false, "Payload is empty");
        }

        // Validate hex payload format
        if (!isValidHexString(hexPayload)) {
            return new SendResult(false, "Invalid hex payload format");
        }

        if (bluetoothGatt == null) {
            Log.e(TAG, "Bluetooth GATT not connected");
            return new SendResult(false, "Bluetooth device not connected");
        }

        try {
            // Convert hex string to byte array
            byte[] bytes = hexStringToByteArray(hexPayload);
            if (bytes == null || bytes.length == 0) {
                return new SendResult(false, "Failed to convert hex payload to bytes");
            }

            BluetoothGattService service = bluetoothGatt.getService(SERVICE_UUID);
            if (service == null) {
                Log.e(TAG, "Service not found: " + SERVICE_UUID);
                return new SendResult(false, "Bluetooth service not found. Device may not support required service.");
            }

            BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
            if (characteristic == null) {
                Log.e(TAG, "Characteristic not found: " + CHARACTERISTIC_UUID);
                return new SendResult(false, "Bluetooth characteristic not found. Device may not support required characteristic.");
            }

            // Check if characteristic supports write
            int properties = characteristic.getProperties();
            if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) == 0 &&
                (properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == 0) {
                return new SendResult(false, "Characteristic does not support write operations");
            }

            characteristic.setValue(bytes);
            boolean success = bluetoothGatt.writeCharacteristic(characteristic);
            if (success) {
                Log.d(TAG, "Payload sent successfully: " + hexPayload);
                return new SendResult(true, null);
            } else {
                Log.e(TAG, "Failed to write characteristic");
                return new SendResult(false, "Failed to write to Bluetooth device. Device may be out of range or disconnected.");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied sending payload", e);
            return new SendResult(false, "Bluetooth permission denied");
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid hex payload", e);
            return new SendResult(false, "Invalid hex payload format: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error sending payload", e);
            return new SendResult(false, "Error sending payload: " + e.getMessage());
        }
    }

    /**
     * Validate hex string format
     */
    private boolean isValidHexString(String hex) {
        if (hex == null || hex.isEmpty()) {
            return false;
        }
        // Remove whitespace and check if all characters are valid hex
        hex = hex.replaceAll("\\s", "");
        return hex.matches("^[0-9A-Fa-f]+$") && hex.length() % 2 == 0;
    }

    /**
     * Result class for sendHexPayload operation
     */
    public static class SendResult {
        private final boolean success;
        private final String errorMessage;

        public SendResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
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

    public void startScanning(BluetoothScanCallback callback) {
        if (!hasBluetoothPermissions()) {
            Log.w(TAG, "Bluetooth permissions not granted for scanning");
            if (callback != null) {
                callback.onScanError("Bluetooth permissions not granted");
            }
            return;
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.w(TAG, "Bluetooth not enabled");
            if (callback != null) {
                callback.onScanError("Bluetooth not enabled");
            }
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.w(TAG, "BLE scanning requires Android 5.0+");
            if (callback != null) {
                callback.onScanError("BLE scanning not supported on this device");
            }
            return;
        }

        if (bluetoothLeScanner == null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }

        if (bluetoothLeScanner == null) {
            Log.e(TAG, "BluetoothLeScanner not available");
            if (callback != null) {
                callback.onScanError("Bluetooth scanner not available");
            }
            return;
        }

        // Stop any existing scan
        stopScanning();

        scannedDeviceAddresses.clear();
        this.scanCallback = callback;

        try {
            ScanSettings scanSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();

            List<ScanFilter> scanFilters = new ArrayList<>(); // No filters - scan all devices

            bluetoothLeScanner.startScan(scanFilters, scanSettings, new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    if (result != null && result.getDevice() != null) {
                        BluetoothDevice device = result.getDevice();
                        String address = device.getAddress();
                        
                        // Avoid duplicates
                        if (!scannedDeviceAddresses.contains(address)) {
                            scannedDeviceAddresses.add(address);
                            try {
                                String name = null;
                                
                                // Try to get name from device first
                                try {
                                    name = device.getName();
                                } catch (SecurityException e) {
                                    Log.w(TAG, "Permission denied getting device name", e);
                                }
                                
                                // If name is null, try to get it from ScanRecord
                                if ((name == null || name.isEmpty()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    try {
                                        android.bluetooth.le.ScanRecord scanRecord = result.getScanRecord();
                                        if (scanRecord != null) {
                                            String deviceName = scanRecord.getDeviceName();
                                            if (deviceName != null && !deviceName.isEmpty()) {
                                                name = deviceName;
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.w(TAG, "Error getting name from scan record", e);
                                    }
                                }
                                
                                // If still no name, use a descriptive fallback with MAC address
                                if (name == null || name.isEmpty()) {
                                    // Format MAC address for display (e.g., "Device (AA:BB:CC:DD:EE:FF)")
                                    name = "Device (" + address + ")";
                                }
                                
                                BluetoothDeviceModel deviceModel = new BluetoothDeviceModel(name, address, device);
                                if (scanCallback != null) {
                                    scanCallback.onDeviceFound(deviceModel);
                                }
                            } catch (SecurityException e) {
                                Log.w(TAG, "Permission denied accessing device info", e);
                                // Still create device model with address as name
                                String fallbackName = "Device (" + address + ")";
                                BluetoothDeviceModel deviceModel = new BluetoothDeviceModel(fallbackName, address, device);
                                if (scanCallback != null) {
                                    scanCallback.onDeviceFound(deviceModel);
                                }
                            } catch (Exception e) {
                                Log.w(TAG, "Error processing scanned device", e);
                            }
                        }
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    String error = "Scan failed with error code: " + errorCode;
                    Log.e(TAG, error);
                    if (scanCallback != null) {
                        scanCallback.onScanError(error);
                    }
                }
            });

            // Auto-stop after scan duration
            mainHandler.postDelayed(() -> {
                stopScanning();
                if (scanCallback != null) {
                    scanCallback.onScanFinished();
                }
            }, SCAN_DURATION_MS);

            Log.d(TAG, "BLE scanning started");
        } catch (SecurityException e) {
            Log.e(TAG, "Bluetooth permission not granted for scanning", e);
            if (callback != null) {
                callback.onScanError("Bluetooth permission not granted");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting scan", e);
            if (callback != null) {
                callback.onScanError("Error starting scan: " + e.getMessage());
            }
        }
    }

    public void stopScanning() {
        if (bluetoothLeScanner != null && scanCallback != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    bluetoothLeScanner.stopScan(new ScanCallback() {
                        @Override
                        public void onScanResult(int callbackType, ScanResult result) {
                            // Empty implementation
                        }

                        @Override
                        public void onScanFailed(int errorCode) {
                            // Empty implementation
                        }
                    });
                }
                Log.d(TAG, "BLE scanning stopped");
            } catch (Exception e) {
                Log.e(TAG, "Error stopping scan", e);
            }
        }
        scanCallback = null;
        scannedDeviceAddresses.clear();
    }

    public interface BluetoothScanCallback {
        void onDeviceFound(BluetoothDeviceModel device);
        void onScanFinished();
        void onScanError(String error);
    }

    public abstract static class BluetoothGattCallback extends android.bluetooth.BluetoothGattCallback {
        private BluetoothManager bluetoothManager;

        public void setBluetoothManager(BluetoothManager manager) {
            this.bluetoothManager = manager;
        }

        @Override
        public void onConnectionStateChange(android.bluetooth.BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // Cancel timeout on successful connection
                    if (bluetoothManager != null) {
                        bluetoothManager.cancelConnectionTimeout();
                    }
                    gatt.discoverServices();
                    onConnected();
                } else {
                    // Connection failed
                    if (bluetoothManager != null) {
                        bluetoothManager.cancelConnectionTimeout();
                    }
                    String errorMsg = "Connection failed";
                    if (status == 8) { // GATT_INTERNAL_ERROR
                        errorMsg = "Connection failed: Internal error. Device may be out of range.";
                    } else if (status == 19) { // GATT_CONN_TERMINATE_PEER_USER
                        errorMsg = "Connection terminated by device";
                    } else if (status == 22) { // GATT_CONN_TIMEOUT
                        errorMsg = "Connection timeout. Device not responding.";
                    } else if (status == 133) { // GATT_ERROR
                        errorMsg = "Connection error. Please ensure device is powered on and in range.";
                    }
                    onConnectionFailed(errorMsg);
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (bluetoothManager != null) {
                    bluetoothManager.cancelConnectionTimeout();
                }
                onDisconnected();
            }
        }

        @Override
        public void onServicesDiscovered(android.bluetooth.BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                onServicesDiscovered();
            } else {
                if (bluetoothManager != null) {
                    bluetoothManager.cancelConnectionTimeout();
                }
                String errorMsg = "Service discovery failed";
                if (status == 8) {
                    errorMsg = "Service discovery failed: Internal error";
                } else if (status == 133) {
                    errorMsg = "Service discovery failed: GATT error. Device may not support required services.";
                }
                onConnectionFailed(errorMsg);
            }
        }

        public abstract void onConnected();
        public abstract void onDisconnected();
        public abstract void onServicesDiscovered();
        public abstract void onConnectionFailed(String error);
    }
}

