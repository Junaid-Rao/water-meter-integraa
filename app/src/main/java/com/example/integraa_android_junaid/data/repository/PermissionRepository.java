package com.example.integraa_android_junaid.data.repository;

import com.example.integraa_android_junaid.data.api.ApiService;
import com.example.integraa_android_junaid.data.api.models.PermissionResponse;
import com.example.integraa_android_junaid.data.local.SharedPreferencesManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PermissionRepository {
    private final ApiService apiService;
    private final SharedPreferencesManager preferencesManager;
    private PermissionResponse cachedPermissions;

    public PermissionRepository(ApiService apiService, SharedPreferencesManager preferencesManager) {
        this.apiService = apiService;
        this.preferencesManager = preferencesManager;
    }

    public void getPermissions(PermissionsCallback callback) {
        String token = preferencesManager.getToken();
        if (token == null || token.isEmpty()) {
            callback.onError("Not authenticated");
            return;
        }

        // Check if we need to refresh (3 hours passed or no cache)
        if (!preferencesManager.shouldRefreshPermissions() && cachedPermissions != null) {
            callback.onSuccess(cachedPermissions);
            return;
        }

        Call<PermissionResponse> call = apiService.getPermissions(token);
        call.enqueue(new Callback<PermissionResponse>() {
            @Override
            public void onResponse(Call<PermissionResponse> call, Response<PermissionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cachedPermissions = response.body();
                    preferencesManager.saveLastPermissionsFetchTime(System.currentTimeMillis());
                    callback.onSuccess(cachedPermissions);
                } else {
                    // Handle session expiry
                    if (response.code() == 401 || response.code() == 403) {
                        preferencesManager.clearToken();
                        callback.onSessionExpired();
                    } else {
                        callback.onError("Failed to fetch permissions: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<PermissionResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void forceRefresh(PermissionsCallback callback) {
        cachedPermissions = null;
        preferencesManager.saveLastPermissionsFetchTime(0);
        getPermissions(callback);
    }

    public PermissionResponse getCachedPermissions() {
        return cachedPermissions;
    }

    public interface PermissionsCallback {
        void onSuccess(PermissionResponse response);
        void onError(String error);
        void onSessionExpired();
    }
}

