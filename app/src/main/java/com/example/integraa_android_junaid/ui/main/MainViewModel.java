package com.example.integraa_android_junaid.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.integraa_android_junaid.data.api.models.Action;
import com.example.integraa_android_junaid.data.api.models.PermissionResponse;
import com.example.integraa_android_junaid.data.repository.AuthRepository;
import com.example.integraa_android_junaid.data.repository.PermissionRepository;
import com.example.integraa_android_junaid.domain.usecase.GetPermissionsUseCase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainViewModel extends ViewModel {
    private final GetPermissionsUseCase getPermissionsUseCase;
    private final AuthRepository authRepository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<List<ActionGroup>> actionGroups = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> sessionExpired = new MutableLiveData<>(false);

    @Inject
    public MainViewModel(GetPermissionsUseCase getPermissionsUseCase, AuthRepository authRepository) {
        this.getPermissionsUseCase = getPermissionsUseCase;
        this.authRepository = authRepository;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<List<ActionGroup>> getActionGroups() {
        return actionGroups;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getSessionExpired() {
        return sessionExpired;
    }

    public void loadPermissions() {
        isLoading.setValue(true);
        error.setValue(null);

        getPermissionsUseCase.execute(new PermissionRepository.PermissionsCallback() {
            @Override
            public void onSuccess(PermissionResponse response) {
                isLoading.postValue(false);
                if (response != null && response.getActions() != null) {
                    List<ActionGroup> groups = convertToActionGroups(response.getActions());
                    actionGroups.postValue(groups);
                } else {
                    error.postValue("No permissions available");
                }
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.postValue(false);
                error.postValue(errorMessage);
            }

            @Override
            public void onSessionExpired() {
                isLoading.postValue(false);
                sessionExpired.postValue(true);
            }
        });
    }

    public void refreshPermissions() {
        isLoading.setValue(true);
        error.setValue(null);

        getPermissionsUseCase.forceRefresh(new PermissionRepository.PermissionsCallback() {
            @Override
            public void onSuccess(PermissionResponse response) {
                isLoading.postValue(false);
                if (response != null && response.getActions() != null) {
                    List<ActionGroup> groups = convertToActionGroups(response.getActions());
                    actionGroups.postValue(groups);
                } else {
                    error.postValue("No permissions available");
                }
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.postValue(false);
                error.postValue(errorMessage);
            }

            @Override
            public void onSessionExpired() {
                isLoading.postValue(false);
                sessionExpired.postValue(true);
            }
        });
    }

    public void logout() {
        authRepository.logout();
    }

    private List<ActionGroup> convertToActionGroups(List<Action> actions) {
        List<ActionGroup> groups = new ArrayList<>();
        if (actions != null) {
            for (int i = 0; i < actions.size(); i++) {
                Action action = actions.get(i);
                // Generate key from label or use index
                String key = action != null && action.getLabel() != null 
                    ? action.getLabel().toLowerCase().replace(" ", "_") 
                    : "action_" + i;
                groups.add(new ActionGroup(key, action));
            }
        }
        return groups;
    }

    public static class ActionGroup {
        private final String key;
        private final Action action;

        public ActionGroup(String key, Action action) {
            this.key = key;
            this.action = action;
        }

        public String getKey() {
            return key;
        }

        public String getLabel() {
            return action != null ? action.getLabel() : key;
        }

        public Action getAction() {
            return action;
        }
    }
}

