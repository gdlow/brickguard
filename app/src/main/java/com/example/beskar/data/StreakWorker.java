package com.example.beskar.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.beskar.Beskar;
import com.example.beskar.service.BeskarVpnService;
import com.example.beskar.util.Logger;

public class StreakWorker extends Worker {

    public static final String TAG_UPDATE_STREAK = "TAG_UPDATE_STREAK";
    private static final long ONE_DAY_IN_MILLIS = (long) 8.64e+7;

    public StreakWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Logger.debug("Running update streak worker task...");
            updateStreak();
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("Error updating streak. Error: " + e.getMessage());
            return Result.failure();
        }
    }

    @Override
    public void onStopped() {
        super.onStopped();
        Logger.debug("onStopped called for StreakWorker");
    }

    private void updateStreak() {
        long startTimeMarker = Beskar.getPrefs().getLong("beskar_start_time_marker", 0);
        if (!BeskarVpnService.isActivated() || startTimeMarker == 0) {
            return;
        }
        long timeDelta = System.currentTimeMillis() - startTimeMarker;
        long timeDeltaInDays =  timeDelta / ONE_DAY_IN_MILLIS;
        long currentMaxTimeDelta = Beskar.getPrefs().getLong("beskar_longest_time_delta", 0);
        // Update current time
        Beskar.getPrefs().edit().putLong("beskar_current_time_delta", timeDeltaInDays).apply();
        // Update maximum time
        Beskar.getPrefs().edit().putLong("beskar_longest_time_delta", Math.max(timeDeltaInDays,
                currentMaxTimeDelta)).apply();
    }
}