package com.example.integraa_android_junaid.ui.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.integraa_android_junaid.data.api.models.Action;

import java.util.ArrayList;
import java.util.List;

public class MainPagerAdapter extends FragmentStateAdapter {
    private List<MainViewModel.ActionGroup> actionGroups = new ArrayList<>();

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public void setActionGroups(List<MainViewModel.ActionGroup> actionGroups) {
        this.actionGroups = actionGroups != null ? actionGroups : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position >= 0 && position < actionGroups.size()) {
            MainViewModel.ActionGroup group = actionGroups.get(position);
            if (group != null) {
                Action action = group.getAction();
                if (action != null) {
                    return CommandListFragment.newInstance(action);
                }
            }
        }
        // Return empty fragment as fallback
        return new CommandListFragment();
    }

    @Override
    public int getItemCount() {
        return actionGroups.size();
    }
}

