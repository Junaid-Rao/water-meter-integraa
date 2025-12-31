package com.example.integraa_android_junaid.ui.command;

import androidx.lifecycle.ViewModel;

import com.example.integraa_android_junaid.data.bluetooth.BluetoothManager;
import com.example.integraa_android_junaid.data.local.SharedPreferencesManager;
import com.example.integraa_android_junaid.domain.model.Parameter;
import com.example.integraa_android_junaid.domain.usecase.SendCommandUseCase;

import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CommandViewModel extends ViewModel {
    private final SendCommandUseCase sendCommandUseCase;
    private final BluetoothManager bluetoothManager;
    private final SharedPreferencesManager preferencesManager;

    @Inject
    public CommandViewModel(SendCommandUseCase sendCommandUseCase, BluetoothManager bluetoothManager, SharedPreferencesManager preferencesManager) {
        this.sendCommandUseCase = sendCommandUseCase;
        this.bluetoothManager = bluetoothManager;
        this.preferencesManager = preferencesManager;
    }

    private SendCommandCallback pendingCallback;
    private String pendingPayload;
    private Map<String, String> pendingParameterValues;
    private Map<String, Parameter> pendingParameterDefinitions;

    public void sendCommand(String payload, Map<String, String> parameterValues, Map<String, Parameter> parameterDefinitions, SendCommandCallback callback) {
        // Check if device is connected, if not, connect to saved device
        if (!bluetoothManager.isConnected()) {
            String deviceAddress = preferencesManager.getBluetoothDeviceAddress();
            if (deviceAddress == null || deviceAddress.isEmpty()) {
                callback.onError("No Bluetooth device selected. Please select a device in settings.");
                return;
            }

            // Store command details for when connection completes
            pendingCallback = callback;
            pendingPayload = payload;
            pendingParameterValues = parameterValues;
            pendingParameterDefinitions = parameterDefinitions;

            // Connect to the saved device
            bluetoothManager.connectToDeviceByAddress(deviceAddress, new BluetoothManager.BluetoothGattCallback() {
                @Override
                public void onConnected() {
                    // Device connected, wait for services discovery
                    android.util.Log.d("CommandViewModel", "Device connected, waiting for services...");
                }

                @Override
                public void onDisconnected() {
                    if (pendingCallback != null) {
                        pendingCallback.onError("Device disconnected");
                        clearPendingCommand();
                    }
                }

                @Override
                public void onServicesDiscovered() {
                    // Services discovered, now we can send the command
                    android.util.Log.d("CommandViewModel", "Services discovered, sending command...");
                    if (pendingPayload != null && pendingCallback != null) {
                        sendCommandInternal(pendingPayload, pendingParameterValues, pendingParameterDefinitions, pendingCallback);
                        clearPendingCommand();
                    }
                }

                @Override
                public void onConnectionFailed(String error) {
                    if (pendingCallback != null) {
                        pendingCallback.onError("Failed to connect to device: " + error);
                        clearPendingCommand();
                    }
                }
            });
        } else {
            // Already connected, send command directly
            sendCommandInternal(payload, parameterValues, parameterDefinitions, callback);
        }
    }

    private void clearPendingCommand() {
        pendingCallback = null;
        pendingPayload = null;
        pendingParameterValues = null;
        pendingParameterDefinitions = null;
    }

    private void sendCommandInternal(String payload, Map<String, String> parameterValues, Map<String, Parameter> parameterDefinitions, SendCommandCallback callback) {
        // Use the PayloadBuilder with parameter definitions for proper value transformation
        com.example.integraa_android_junaid.domain.model.PayloadBuilder payloadBuilder = 
            new com.example.integraa_android_junaid.domain.model.PayloadBuilder(
                new com.example.integraa_android_junaid.domain.usecase.CalculateChecksumUseCase()
            );

        try {
            String finalPayload = payloadBuilder.buildPayload(payload, parameterValues, parameterDefinitions);
            boolean success = bluetoothManager.sendHexPayload(finalPayload);
            if (success) {
                callback.onSuccess(finalPayload);
            } else {
                callback.onError("Failed to send payload to Bluetooth device");
            }
        } catch (Exception e) {
            callback.onError("Error building payload: " + e.getMessage());
        }
    }

    public boolean isBluetoothConnected() {
        return bluetoothManager.isConnected();
    }

    public interface SendCommandCallback {
        void onSuccess(String payload);
        void onError(String error);
    }
}

