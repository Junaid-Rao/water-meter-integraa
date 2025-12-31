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
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
        try {
            if (getArguments() != null) {
                commandKey = getArguments().getString(ARG_COMMAND_KEY);
                String commandJson = getArguments().getString(ARG_COMMAND);
                if (commandJson != null && !commandJson.isEmpty()) {
                    Gson gson = new Gson();
                    command = gson.fromJson(commandJson, Command.class);
                } else {
                    android.util.Log.e("CommandDialogFragment", "Command JSON is null or empty");
                }
            } else {
                android.util.Log.e("CommandDialogFragment", "Arguments bundle is null");
            }
        } catch (Exception e) {
            android.util.Log.e("CommandDialogFragment", "Error parsing command from arguments", e);
            command = null;
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

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Set dialog window to respect safe areas
            android.view.Window window = getDialog().getWindow();
            window.setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
            
            // Enable edge-to-edge and handle system bars
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
                window.getDecorView().setSystemUiVisibility(
                        android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                );
            }
            
            // Apply window insets for safe area handling
            if (getView() != null) {
                android.view.View rootView = getView();
                ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                    int systemBars = WindowInsetsCompat.Type.systemBars();
                    androidx.core.graphics.Insets windowInsets = insets.getInsets(systemBars);
                    int topInset = windowInsets.top;
                    int bottomInset = windowInsets.bottom;
                    int leftInset = windowInsets.left;
                    int rightInset = windowInsets.right;
                    
                    // Apply safe area padding
                    v.setPadding(
                            Math.max(leftInset, v.getPaddingLeft()),
                            Math.max(topInset, v.getPaddingTop()),
                            Math.max(rightInset, v.getPaddingRight()),
                            Math.max(bottomInset, v.getPaddingBottom())
                    );
                    
                    return insets;
                });
            }
        }
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
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: Unable to process command. Please try again.", Toast.LENGTH_LONG).show();
            }
            return;
        }

        // The ViewModel will automatically connect to the selected device if needed
        // No need to check connection status here - let ViewModel handle it

        // Validate and collect parameter values
        Map<String, String> parameterValues = new HashMap<>();
        Map<String, com.example.integraa_android_junaid.domain.model.Parameter> parameterDefinitions = new HashMap<>();

        boolean isValid = true;
        
        if (command.getParameters() == null || command.getParameters().isEmpty()) {
            // No parameters, send command directly
            // Show loading feedback
            if (sendButton != null) {
                sendButton.setEnabled(false);
                sendButton.setText("Connecting...");
            }
            
            // Send command - ViewModel will handle connection automatically
            viewModel.sendCommand(command.getPayload(), parameterValues, parameterDefinitions, new CommandViewModel.SendCommandCallback() {
                @Override
                public void onSuccess(String payload) {
                    if (sendButton != null) {
                        sendButton.setEnabled(true);
                        sendButton.setText("Send Command");
                    }
                    if (getContext() != null) {
                        Toast.makeText(getContext(), getString(R.string.success_command_sent), Toast.LENGTH_SHORT).show();
                    }
                    dismiss();
                }

                @Override
                public void onError(String error) {
                    if (sendButton != null) {
                        sendButton.setEnabled(true);
                        sendButton.setText("Send Command");
                    }
                    if (getContext() != null) {
                        String errorMsg = error != null ? error : getString(R.string.error_payload_send_failed);
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
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
                        if (editText != null && getContext() != null) {
                            editText.setError(getString(R.string.error_parameter_invalid_format));
                        }
                        isValid = false;
                        continue;
                    }
                }

                // Validate integer min/max
                if ("int".equalsIgnoreCase(param.getType())) {
                    if (!ValidationUtils.validateInteger(value, param.getMin(), param.getMax())) {
                        if (editText != null && getContext() != null) {
                            editText.setError(getString(R.string.error_parameter_out_of_range));
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

        // Show loading feedback
        if (sendButton != null) {
            sendButton.setEnabled(false);
            sendButton.setText("Connecting...");
        }

        // Send command - ViewModel will handle connection automatically
        viewModel.sendCommand(command.getPayload(), parameterValues, parameterDefinitions, new CommandViewModel.SendCommandCallback() {
            @Override
            public void onSuccess(String payload) {
                if (sendButton != null) {
                    sendButton.setEnabled(true);
                    sendButton.setText("Send Command");
                }
                Toast.makeText(getContext(), "Command sent successfully", Toast.LENGTH_SHORT).show();
                dismiss();
            }

            @Override
            public void onError(String error) {
                if (sendButton != null) {
                    sendButton.setEnabled(true);
                    sendButton.setText("Send Command");
                }
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}

