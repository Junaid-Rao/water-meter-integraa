package com.example.integraa_android_junaid.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.integraa_android_junaid.R;
import com.example.integraa_android_junaid.data.api.models.Action;
import com.example.integraa_android_junaid.data.api.models.Command;
import com.example.integraa_android_junaid.ui.command.CommandDialogFragment;

public class CommandListFragment extends Fragment {
    private static final String ARG_ACTION = "action";

    private Action action;
    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private CommandAdapter adapter;

    public static CommandListFragment newInstance(Action action) {
        CommandListFragment fragment = new CommandListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ACTION, action);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (getArguments() != null) {
                action = (Action) getArguments().getSerializable(ARG_ACTION);
            }
        } catch (Exception e) {
            e.printStackTrace();
            action = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_command_list, container, false);
        recyclerView = view.findViewById(R.id.commandRecyclerView);
        emptyTextView = view.findViewById(R.id.emptyTextView);

        try {
            adapter = new CommandAdapter();
            adapter.setOnCommandClickListener(commandItem -> {
                try {
                    // Comprehensive null checks
                    if (commandItem == null) {
                        showErrorToast("Command item is null");
                        return;
                    }
                    
                    if (commandItem.getCommand() == null) {
                        showErrorToast("Command data is invalid");
                        return;
                    }
                    
                    Command command = commandItem.getCommand();
                    if (command.getLabel() == null && command.getPayload() == null) {
                        showErrorToast("Command has no data");
                        return;
                    }

                    if (!isAdded() || getActivity() == null || getContext() == null) {
                        showErrorToast("Fragment not ready. Please try again.");
                        return;
                    }

                    // Validate fragment manager is available
                    if (getActivity().getSupportFragmentManager() == null) {
                        showErrorToast("Unable to open dialog. Please try again.");
                        return;
                    }

                    // Create dialog with proper error handling
                    String commandKey = commandItem.getKey();
                    if (commandKey == null || commandKey.isEmpty()) {
                        commandKey = command.getLabel() != null 
                            ? command.getLabel().toLowerCase().replace(" ", "_").replace("/", "_")
                            : "command";
                    }

                    CommandDialogFragment dialog = CommandDialogFragment.newInstance(commandKey, command);
                    
                    // Check if another dialog is already showing
                    if (getActivity().getSupportFragmentManager().findFragmentByTag("CommandDialog") != null) {
                        showErrorToast("Command dialog is already open");
                        return;
                    }

                    // Use activity's fragment manager for ViewPager2 fragments
                    dialog.show(getActivity().getSupportFragmentManager(), "CommandDialog");
                } catch (IllegalStateException e) {
                    // Fragment not attached or activity destroyed
                    e.printStackTrace();
                    showErrorToast("Unable to open command dialog. Please try again.");
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    showErrorToast("Command data is incomplete. Please try again.");
                } catch (Exception e) {
                    e.printStackTrace();
                    String errorMsg = e.getMessage();
                    if (errorMsg == null || errorMsg.isEmpty()) {
                        errorMsg = "Unknown error occurred";
                    }
                    showErrorToast("Error opening command dialog: " + errorMsg);
                }
            });

            if (recyclerView != null && getContext() != null) {
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(adapter);
            }

            if (action != null && action.getItems() != null) {
                adapter.setCommands(action.getItems());
                if (emptyTextView != null) {
                    emptyTextView.setVisibility(action.getItems().isEmpty() ? View.VISIBLE : View.GONE);
                }
            } else {
                if (emptyTextView != null) {
                    emptyTextView.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (emptyTextView != null) {
                emptyTextView.setVisibility(View.VISIBLE);
                emptyTextView.setText("Error loading commands");
            }
        }

        return view;
    }
    
    private void showErrorToast(String message) {
        try {
            if (getContext() != null && isAdded()) {
                android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // Ignore if context is not available
            e.printStackTrace();
        }
    }
}

