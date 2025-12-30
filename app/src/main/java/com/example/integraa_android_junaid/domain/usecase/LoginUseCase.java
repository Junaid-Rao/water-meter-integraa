package com.example.integraa_android_junaid.domain.usecase;

import com.example.integraa_android_junaid.data.repository.AuthRepository;

public class LoginUseCase {
    private final AuthRepository authRepository;

    public LoginUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void execute(String username, String password, double latitude, double longitude, AuthRepository.LoginCallback callback) {
        authRepository.login(username, password, latitude, longitude, callback);
    }
}

