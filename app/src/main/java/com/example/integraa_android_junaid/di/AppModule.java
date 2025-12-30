package com.example.integraa_android_junaid.di;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.integraa_android_junaid.data.local.SharedPreferencesManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(@ApplicationContext Context context) {
        return context.getSharedPreferences("integraa_prefs", Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    SharedPreferencesManager provideSharedPreferencesManager(SharedPreferences sharedPreferences) {
        return new SharedPreferencesManager(sharedPreferences);
    }
}

