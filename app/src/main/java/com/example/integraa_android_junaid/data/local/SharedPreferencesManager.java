package com.example.integraa_android_junaid.data.local;

import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_BLUETOOTH_DEVICE_NAME = "bluetooth_device_name";
    private static final String KEY_BLUETOOTH_DEVICE_ADDRESS = "bluetooth_device_address";
    private static final String KEY_LAST_PERMISSIONS_FETCH = "last_permissions_fetch";

    private final SharedPreferences sharedPreferences;

    public SharedPreferencesManager(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void saveToken(String token) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public void clearToken() {
        sharedPreferences.edit().remove(KEY_TOKEN).apply();
    }

    public void saveUserId(String userId) {
        sharedPreferences.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public void saveBluetoothDevice(String name, String address) {
        sharedPreferences.edit()
                .putString(KEY_BLUETOOTH_DEVICE_NAME, name)
                .putString(KEY_BLUETOOTH_DEVICE_ADDRESS, address)
                .apply();
    }

    public String getBluetoothDeviceName() {
        return sharedPreferences.getString(KEY_BLUETOOTH_DEVICE_NAME, null);
    }

    public String getBluetoothDeviceAddress() {
        return sharedPreferences.getString(KEY_BLUETOOTH_DEVICE_ADDRESS, null);
    }

    public void clearBluetoothDevice() {
        sharedPreferences.edit()
                .remove(KEY_BLUETOOTH_DEVICE_NAME)
                .remove(KEY_BLUETOOTH_DEVICE_ADDRESS)
                .apply();
    }

    public void saveLastPermissionsFetchTime(long timestamp) {
        sharedPreferences.edit().putLong(KEY_LAST_PERMISSIONS_FETCH, timestamp).apply();
    }

    public long getLastPermissionsFetchTime() {
        return sharedPreferences.getLong(KEY_LAST_PERMISSIONS_FETCH, 0);
    }

    public boolean shouldRefreshPermissions() {
        long lastFetch = getLastPermissionsFetchTime();
        if (lastFetch == 0) {
            return true;
        }
        long threeHoursInMillis = 3 * 60 * 60 * 1000;
        return (System.currentTimeMillis() - lastFetch) >= threeHoursInMillis;
    }

    public void clearAll() {
        sharedPreferences.edit().clear().apply();
    }
}

