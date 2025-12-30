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
                    if (commandItem == null || commandItem.getCommand() == null) {
                        if (getContext() != null) {
                            android.widget.Toast.makeText(getContext(), "Command data is invalid", android.widget.Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    if (!isAdded() || getActivity() == null) {
                        if (getContext() != null) {
                            android.widget.Toast.makeText(getContext(), "Fragment not ready. Please try again.", android.widget.Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    CommandDialogFragment dialog = CommandDialogFragment.newInstance(
                            commandItem.getKey() != null ? commandItem.getKey() : "command",
                            commandItem.getCommand()
                    );

                    // Use activity's fragment manager for ViewPager2 fragments
                    dialog.show(getActivity().getSupportFragmentManager(), "CommandDialog");
                } catch (IllegalStateException e) {
                    // Fragment not attached or activity destroyed
                    e.printStackTrace();
                    if (getContext() != null) {
                        android.widget.Toast.makeText(getContext(), "Unable to open command dialog. Please try again.", android.widget.Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getContext() != null) {
                        android.widget.Toast.makeText(getContext(), "Error opening command dialog: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"), android.widget.Toast.LENGTH_SHORT).show();
                    }
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
}

