package com.example.integraa_android_junaid.ui.command;

import androidx.lifecycle.ViewModel;

import com.example.integraa_android_junaid.data.bluetooth.BluetoothManager;
import com.example.integraa_android_junaid.domain.model.Parameter;
import com.example.integraa_android_junaid.domain.usecase.SendCommandUseCase;

import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CommandViewModel extends ViewModel {
    private final SendCommandUseCase sendCommandUseCase;
    private final BluetoothManager bluetoothManager;

    @Inject
    public CommandViewModel(SendCommandUseCase sendCommandUseCase, BluetoothManager bluetoothManager) {
        this.sendCommandUseCase = sendCommandUseCase;
        this.bluetoothManager = bluetoothManager;
    }

    public void sendCommand(String payload, Map<String, String> parameterValues, Map<String, Parameter> parameterDefinitions, SendCommandCallback callback) {
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

