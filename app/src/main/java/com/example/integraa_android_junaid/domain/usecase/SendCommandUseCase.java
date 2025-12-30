package com.example.integraa_android_junaid.domain.usecase;

import com.example.integraa_android_junaid.data.bluetooth.BluetoothManager;
import com.example.integraa_android_junaid.domain.model.PayloadBuilder;

import java.util.Map;

public class SendCommandUseCase {
    private final BluetoothManager bluetoothManager;
    private final PayloadBuilder payloadBuilder;

    public SendCommandUseCase(BluetoothManager bluetoothManager, PayloadBuilder payloadBuilder) {
        this.bluetoothManager = bluetoothManager;
        this.payloadBuilder = payloadBuilder;
    }

    public void execute(String payload, Map<String, String> parameterValues, SendCommandCallback callback) {
        try {
            String finalPayload = payloadBuilder.buildPayload(payload, parameterValues);
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

    public interface SendCommandCallback {
        void onSuccess(String payload);
        void onError(String error);
    }
}

