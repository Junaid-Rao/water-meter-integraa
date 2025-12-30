package com.example.integraa_android_junaid.di;

import android.content.Context;

import com.example.integraa_android_junaid.data.api.ApiService;
import com.example.integraa_android_junaid.data.bluetooth.BluetoothManager;
import com.example.integraa_android_junaid.data.local.SharedPreferencesManager;
import com.example.integraa_android_junaid.data.repository.AuthRepository;
import com.example.integraa_android_junaid.data.repository.PermissionRepository;
import com.example.integraa_android_junaid.domain.model.PayloadBuilder;
import com.example.integraa_android_junaid.domain.usecase.CalculateChecksumUseCase;
import com.example.integraa_android_junaid.domain.usecase.GetPermissionsUseCase;
import com.example.integraa_android_junaid.domain.usecase.LoginUseCase;
import com.example.integraa_android_junaid.domain.usecase.SendCommandUseCase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class RepositoryModule {

    @Provides
    @Singleton
    AuthRepository provideAuthRepository(ApiService apiService, SharedPreferencesManager preferencesManager) {
        return new AuthRepository(apiService, preferencesManager);
    }

    @Provides
    @Singleton
    PermissionRepository providePermissionRepository(ApiService apiService, SharedPreferencesManager preferencesManager) {
        return new PermissionRepository(apiService, preferencesManager);
    }

    @Provides
    @Singleton
    BluetoothManager provideBluetoothManager(@ApplicationContext Context context) {
        return new BluetoothManager(context);
    }

    @Provides
    @Singleton
    CalculateChecksumUseCase provideCalculateChecksumUseCase() {
        return new CalculateChecksumUseCase();
    }

    @Provides
    @Singleton
    PayloadBuilder providePayloadBuilder(CalculateChecksumUseCase calculateChecksumUseCase) {
        return new PayloadBuilder(calculateChecksumUseCase);
    }

    @Provides
    @Singleton
    LoginUseCase provideLoginUseCase(AuthRepository authRepository) {
        return new LoginUseCase(authRepository);
    }

    @Provides
    @Singleton
    GetPermissionsUseCase provideGetPermissionsUseCase(PermissionRepository permissionRepository) {
        return new GetPermissionsUseCase(permissionRepository);
    }

    @Provides
    @Singleton
    SendCommandUseCase provideSendCommandUseCase(BluetoothManager bluetoothManager, PayloadBuilder payloadBuilder) {
        return new SendCommandUseCase(bluetoothManager, payloadBuilder);
    }
}

