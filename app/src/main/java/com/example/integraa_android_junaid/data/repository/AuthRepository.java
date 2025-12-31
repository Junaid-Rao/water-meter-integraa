package com.example.integraa_android_junaid.data.repository;

import com.example.integraa_android_junaid.data.api.ApiService;
import com.example.integraa_android_junaid.data.api.models.LoginResponse;
import com.example.integraa_android_junaid.data.local.SharedPreferencesManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private final ApiService apiService;
    private final SharedPreferencesManager preferencesManager;

    public AuthRepository(ApiService apiService, SharedPreferencesManager preferencesManager) {
        this.apiService = apiService;
        this.preferencesManager = preferencesManager;
    }

    public void login(String username, String password, double latitude, double longitude, LoginCallback callback) {
        Call<LoginResponse> call = apiService.login(username, password, latitude, longitude);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        LoginResponse loginResponse = response.body();
                        String token = loginResponse.getToken();
                        if (token != null && !token.isEmpty()) {
                            preferencesManager.saveToken(token);
                            if (loginResponse.getUserId() != null) {
                                preferencesManager.saveUserId(loginResponse.getUserId());
                            }
                            callback.onSuccess(loginResponse);
                        } else {
                            callback.onError("Token not found in response. Please try again.");
                        }
                    } else {
                        String errorMsg;
                        if (response.code() == 401 || response.code() == 403) {
                            errorMsg = "Invalid username or password. Please check your credentials and try again.";
                        } else if (response.code() >= 500) {
                            errorMsg = "Server error. Please try again later.";
                        } else if (response.code() >= 400) {
                            errorMsg = "Login failed. Please check your connection and try again.";
                        } else {
                            errorMsg = "Login failed. Please try again.";
                        }
                        callback.onError(errorMsg);
                    }
                } catch (Exception e) {
                    callback.onError("Error processing login response. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                String errorMsg;
                if (t instanceof java.net.UnknownHostException || t instanceof java.net.ConnectException) {
                    errorMsg = "Unable to connect to server. Please check your internet connection.";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    errorMsg = "Connection timeout. Please check your internet connection and try again.";
                } else {
                    errorMsg = "Network error. Please check your connection and try again.";
                }
                callback.onError(errorMsg);
            }
        });
    }

    public void logout() {
        preferencesManager.clearToken();
        preferencesManager.clearAll();
    }

    public boolean isLoggedIn() {
        return preferencesManager.getToken() != null;
    }

    public String getToken() {
        return preferencesManager.getToken();
    }

    public interface LoginCallback {
        void onSuccess(LoginResponse response);
        void onError(String error);
    }
}

