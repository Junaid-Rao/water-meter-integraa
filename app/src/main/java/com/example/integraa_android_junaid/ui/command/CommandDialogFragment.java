package com.example.integraa_android_junaid.ui.command;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.integraa_android_junaid.R;
import com.example.integraa_android_junaid.data.api.models.Command;
import com.example.integraa_android_junaid.data.api.models.Parameter;
import com.example.integraa_android_junaid.util.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CommandDialogFragment extends DialogFragment {
    private static final String ARG_COMMAND_KEY = "command_key";
    private static final String ARG_COMMAND = "command";

    private String commandKey;
    private Command command;
    private LinearLayout parametersContainer;
    private MaterialButton sendButton;
    private TextView commandLabelTextView;
    private TextView commandPayloadTextView;
    private Map<String, TextInputEditText> parameterInputs = new HashMap<>();
    private Map<String, com.example.integraa_android_junaid.domain.model.Parameter> parameterDefinitions = new HashMap<>();
    private CommandViewModel viewModel;

    public static CommandDialogFragment newInstance(String commandKey, Command command) {
        CommandDialogFragment fragment = new CommandDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COMMAND_KEY, commandKey);
        // Serialize command to JSON string
        Gson gson = new Gson();
        args.putString(ARG_COMMAND, gson.toJson(command));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Integraaandroidjunaid);
        if (getArguments() != null) {
            commandKey = getArguments().getString(ARG_COMMAND_KEY);
            String commandJson = getArguments().getString(ARG_COMMAND);
            Gson gson = new Gson();
            command = gson.fromJson(commandJson, Command.class);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_command, container, false);
        
        try {
            viewModel = new ViewModelProvider(this).get(CommandViewModel.class);

            commandLabelTextView = view.findViewById(R.id.commandLabelTextView);
            commandPayloadTextView = view.findViewById(R.id.commandPayloadTextView);
            parametersContainer = view.findViewById(R.id.parametersContainer);
            sendButton = view.findViewById(R.id.sendButton);

            if (command != null) {
                if (commandLabelTextView != null) {
                    commandLabelTextView.setText(command.getLabel() != null ? command.getLabel() : "Command");
                }
                if (commandPayloadTextView != null) {
                    commandPayloadTextView.setText("Payload: " + (command.getPayload() != null ? command.getPayload() : "N/A"));
                }
                setupParameters();
            } else {
                if (commandLabelTextView != null) {
                    commandLabelTextView.setText("Error: Command not found");
                }
            }

            if (sendButton != null) {
                sendButton.setOnClickListener(v -> sendCommand());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (commandLabelTextView != null) {
                commandLabelTextView.setText("Error loading dialog: " + e.getMessage());
            }
        }

        return view;
    }

    private void setupParameters() {
        if (command == null || command.getParameters() == null || command.getParameters().isEmpty()) {
            if (parametersContainer != null) {
                parametersContainer.setVisibility(View.GONE);
            }
            return;
        }

        if (parametersContainer == null || getContext() == null) {
            return;
        }

        try {
            parametersContainer.removeAllViews();
            parameterInputs.clear();
            parametersContainer.setVisibility(View.VISIBLE);

            for (Map.Entry<String, Parameter> entry : command.getParameters().entrySet()) {
                try {
                    String paramKey = entry.getKey();
                    Parameter param = entry.getValue();

                    if (paramKey == null || param == null) {
                        continue;
                    }

                    // Skip checksum parameter - it's calculated automatically
                    if ("CHK".equalsIgnoreCase(paramKey) || param.getType() == null || "checksum".equalsIgnoreCase(param.getType())) {
                        continue;
                    }

                    TextInputLayout textInputLayout = (TextInputLayout) LayoutInflater.from(getContext())
                            .inflate(R.layout.item_parameter, parametersContainer, false);

                    if (textInputLayout == null) {
                        continue;
                    }

                    TextInputEditText editText = textInputLayout.findViewById(R.id.parameterEditText);
                    if (editText == null) {
                        continue;
                    }

                    textInputLayout.setHint(param.getLabel() != null ? param.getLabel() : paramKey);

                    // Set input type based on parameter type
                    if ("int".equalsIgnoreCase(param.getType())) {
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    } else {
                        editText.setInputType(InputType.TYPE_CLASS_TEXT);
                    }

                    parameterInputs.put(paramKey, editText);
                    parametersContainer.addView(textInputLayout);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Continue with next parameter
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (parametersContainer != null) {
                parametersContainer.setVisibility(View.GONE);
            }
        }
    }

    private void sendCommand() {
        if (viewModel == null || command == null) {
            Toast.makeText(getContext(), "Error: ViewModel or Command not initialized", Toast.LENGTH_LONG).show();
            return;
        }

        if (!viewModel.isBluetoothConnected()) {
            Toast.makeText(getContext(), "Please connect to a Bluetooth device in settings", Toast.LENGTH_LONG).show();
            return;
        }

        // Validate and collect parameter values
        Map<String, String> parameterValues = new HashMap<>();
        Map<String, com.example.integraa_android_junaid.domain.model.Parameter> parameterDefinitions = new HashMap<>();

        boolean isValid = true;
        
        if (command.getParameters() == null || command.getParameters().isEmpty()) {
            // No parameters, send command directly
            viewModel.sendCommand(command.getPayload(), parameterValues, parameterDefinitions, new CommandViewModel.SendCommandCallback() {
                @Override
                public void onSuccess(String payload) {
                    Toast.makeText(getContext(), "Command sent successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        for (Map.Entry<String, Parameter> entry : command.getParameters().entrySet()) {
            try {
                String paramKey = entry.getKey();
                Parameter param = entry.getValue();

                if (paramKey == null || param == null) {
                    continue;
                }

                // Skip checksum
                if ("CHK".equalsIgnoreCase(paramKey) || "checksum".equalsIgnoreCase(param.getType())) {
                    continue;
                }

                TextInputEditText editText = parameterInputs.get(paramKey);
                if (editText == null) {
                    // Parameter might not have UI input (e.g., auto-calculated)
                    continue;
                }

                String value = editText.getText() != null ? editText.getText().toString().trim() : "";

                // Validate required
                if (param.getRequired() != null && !param.getRequired().isEmpty()) {
                    if (!ValidationUtils.validateInput(value, param.getRequired())) {
                        if (editText != null) {
                            editText.setError("Invalid format");
                        }
                        isValid = false;
                        continue;
                    }
                }

                // Validate integer min/max
                if ("int".equalsIgnoreCase(param.getType())) {
                    if (!ValidationUtils.validateInteger(value, param.getMin(), param.getMax())) {
                        if (editText != null) {
                            editText.setError("Value out of range");
                        }
                        isValid = false;
                        continue;
                    }
                }

                parameterValues.put(paramKey, value);
                
                // Convert to domain parameter
                com.example.integraa_android_junaid.domain.model.Parameter domainParam = 
                    new com.example.integraa_android_junaid.domain.model.Parameter(
                        paramKey,
                        param.getLabel(),
                        param.getType(),
                        param.getValue(),
                        param.getRequired(),
                        param.getMin(),
                        param.getMax()
                );
                parameterDefinitions.put(paramKey, domainParam);
            } catch (Exception e) {
                e.printStackTrace();
                isValid = false;
                // Continue with next parameter
            }
        }

        if (!isValid) {
            return;
        }

        // Send command
        viewModel.sendCommand(command.getPayload(), parameterValues, parameterDefinitions, new CommandViewModel.SendCommandCallback() {
            @Override
            public void onSuccess(String payload) {
                Toast.makeText(getContext(), "Command sent successfully", Toast.LENGTH_SHORT).show();
                dismiss();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}

