package com.example.integraa_android_junaid.ui.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.integraa_android_junaid.R;
import com.example.integraa_android_junaid.data.bluetooth.BluetoothDeviceModel;

import java.util.ArrayList;
import java.util.List;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder> {
    private List<BluetoothDeviceModel> devices = new ArrayList<>();
    private OnDeviceClickListener listener;

    public interface OnDeviceClickListener {
        void onDeviceClick(BluetoothDeviceModel device);
    }

    public void setDevices(List<BluetoothDeviceModel> devices) {
        this.devices = devices != null ? devices : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnDeviceClickListener(OnDeviceClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bluetooth_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        holder.bind(devices.get(position));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {
        private TextView deviceNameTextView;
        private TextView deviceAddressTextView;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceNameTextView = itemView.findViewById(R.id.deviceNameTextView);
            deviceAddressTextView = itemView.findViewById(R.id.deviceAddressTextView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeviceClick(devices.get(position));
                }
            });
        }

        public void bind(BluetoothDeviceModel device) {
            deviceNameTextView.setText(device.getName());
            deviceAddressTextView.setText(device.getAddress());
        }
    }
}

