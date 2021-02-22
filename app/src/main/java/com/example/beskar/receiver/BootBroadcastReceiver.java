package com.example.beskar.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.beskar.Beskar;
import com.example.beskar.util.Logger;


public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Beskar.getPrefs().getBoolean("settings_boot", false)) {
            Beskar.activateService(context, false);
            Logger.info("Triggered boot receiver");
        }
    }
}
