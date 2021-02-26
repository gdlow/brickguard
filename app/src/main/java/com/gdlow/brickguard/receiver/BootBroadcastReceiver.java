package com.gdlow.brickguard.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gdlow.brickguard.BrickGuard;
import com.gdlow.brickguard.util.Logger;


public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (BrickGuard.getPrefs().getBoolean("settings_boot", false)) {
            BrickGuard.prepareAndActivateService(context);
            Logger.info("Triggered boot receiver");
        }
    }
}
