package com.example.integraa_android_junaid.data.bluetooth;

import android.bluetooth.BluetoothDevice;

public class BluetoothDeviceModel {
    private String name;
    private String address;
    private BluetoothDevice device;

    public BluetoothDeviceModel(String name, String address, BluetoothDevice device) {
        this.name = name;
        this.address = address;
        this.device = device;
    }

    public String getName() {
        if (name != null && !name.isEmpty()) {
            return name;
        }
        // If name is not available, return a descriptive name with address
        if (address != null && !address.isEmpty()) {
            return "Device (" + address + ")";
        }
        return "Unknown Device";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }
}

