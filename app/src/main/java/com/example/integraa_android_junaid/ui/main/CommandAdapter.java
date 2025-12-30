package com.example.integraa_android_junaid.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.integraa_android_junaid.R;
import com.example.integraa_android_junaid.data.api.models.Command;

import java.util.ArrayList;
import java.util.List;

public class CommandAdapter extends RecyclerView.Adapter<CommandAdapter.CommandViewHolder> {
    private List<CommandItem> commands = new ArrayList<>();
    private OnCommandClickListener listener;

    public interface OnCommandClickListener {
        void onCommandClick(CommandItem commandItem);
    }

    public static class CommandItem {
        private String key;
        private Command command;

        public CommandItem(String key, Command command) {
            this.key = key;
            this.command = command;
        }

        public String getKey() {
            return key;
        }

        public Command getCommand() {
            return command;
        }
    }

    public void setCommands(List<Command> commands) {
        this.commands.clear();
        if (commands != null) {
            for (int i = 0; i < commands.size(); i++) {
                Command command = commands.get(i);
                // Generate key from label or use index
                String key = command != null && command.getLabel() != null 
                    ? command.getLabel().toLowerCase().replace(" ", "_").replace("/", "_")
                    : "command_" + i;
                this.commands.add(new CommandItem(key, command));
            }
        }
        notifyDataSetChanged();
    }

    public void setOnCommandClickListener(OnCommandClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_command, parent, false);
        return new CommandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommandViewHolder holder, int position) {
        CommandItem item = commands.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return commands.size();
    }

    class CommandViewHolder extends RecyclerView.ViewHolder {
        private TextView commandLabelTextView;
        private TextView commandPayloadTextView;

        public CommandViewHolder(@NonNull View itemView) {
            super(itemView);
            commandLabelTextView = itemView.findViewById(R.id.commandLabelTextView);
            commandPayloadTextView = itemView.findViewById(R.id.commandPayloadTextView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCommandClick(commands.get(position));
                }
            });
        }

        public void bind(CommandItem item) {
            Command command = item.getCommand();
            commandLabelTextView.setText(command.getLabel());
            commandPayloadTextView.setText("Payload: " + command.getPayload());
        }
    }
}

