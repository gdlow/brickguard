package com.example.beskar.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.work.BackoffPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.beskar.data.StreakWorker;

import java.util.concurrent.TimeUnit;

public class HomeViewModel extends AndroidViewModel {
    private WorkManager mWorkManager;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        mWorkManager = WorkManager.getInstance(application);
    }

    public void schedulePeriodicUpdateStreak() {
        // Define periodic sync work
        PeriodicWorkRequest periodicSyncDataWork =
                new PeriodicWorkRequest.Builder(StreakWorker.class, 1, TimeUnit.DAYS)
                        .addTag(StreakWorker.TAG_UPDATE_STREAK)
                        .setBackoffCriteria(BackoffPolicy.LINEAR,
                                PeriodicWorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS,
                                TimeUnit.MILLISECONDS)
                        .build();

        // Enqueue periodic work
        mWorkManager.enqueue(periodicSyncDataWork);
    }
}