package com.example.integraa_android_junaid.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.integraa_android_junaid.data.api.models.LoginResponse;
import com.example.integraa_android_junaid.data.repository.AuthRepository;
import com.example.integraa_android_junaid.domain.usecase.LoginUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends ViewModel {
    private final LoginUseCase loginUseCase;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<LoginResponse> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> loginError = new MutableLiveData<>();

    @Inject
    public LoginViewModel(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<LoginResponse> getLoginSuccess() {
        return loginSuccess;
    }

    public LiveData<String> getLoginError() {
        return loginError;
    }

    public void login(String username, String password, double latitude, double longitude) {
        isLoading.setValue(true);
        loginError.setValue(null);

        loginUseCase.execute(username, password, latitude, longitude, new AuthRepository.LoginCallback() {
            @Override
            public void onSuccess(LoginResponse response) {
                isLoading.postValue(false);
                loginSuccess.postValue(response);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                loginError.postValue(error);
            }
        });
    }
}

