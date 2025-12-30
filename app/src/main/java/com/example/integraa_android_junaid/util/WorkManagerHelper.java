package com.example.integraa_android_junaid.util;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.integraa_android_junaid.worker.PermissionRefreshWorker;

import java.util.concurrent.TimeUnit;

public class WorkManagerHelper {
    private static final String PERMISSION_REFRESH_WORK_NAME = "permission_refresh_work";

    public static void schedulePermissionRefresh(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // Periodic work request - runs every 3 hours
        PeriodicWorkRequest refreshWork = new PeriodicWorkRequest.Builder(
                PermissionRefreshWorker.class,
                3,
                TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERMISSION_REFRESH_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                refreshWork
        );
    }

    public static void cancelPermissionRefresh(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(PERMISSION_REFRESH_WORK_NAME);
    }
}

