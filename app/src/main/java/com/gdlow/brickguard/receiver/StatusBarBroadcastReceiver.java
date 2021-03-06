package com.gdlow.brickguard.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gdlow.brickguard.LockActivity;
import com.gdlow.brickguard.MainActivity;
import com.gdlow.brickguard.util.Logger;

import java.lang.reflect.Method;

public class StatusBarBroadcastReceiver extends BroadcastReceiver {
    public static String STATUS_BAR_BTN_DEACTIVATE_CLICK_ACTION = "com.gdlow.brickguard.receiver.StatusBarBroadcastReceiver.STATUS_BAR_BTN_DEACTIVATE_CLICK_ACTION";
    public static String STATUS_BAR_BTN_SETTINGS_CLICK_ACTION = "com.gdlow.brickguard.receiver.StatusBarBroadcastReceiver.STATUS_BAR_BTN_SETTINGS_CLICK_ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(STATUS_BAR_BTN_DEACTIVATE_CLICK_ACTION)) {
            context.startActivity(new Intent(context, LockActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(LockActivity.LOCK_SCREEN_ACTION,
                            LockActivity.LOCK_SCREEN_ACTION_AUTHENTICATE)
                    .putExtra(MainActivity.LAUNCH_ACTION,
                            MainActivity.LAUNCH_ACTION_DEACTIVATE));
        }
        if (intent.getAction().equals(STATUS_BAR_BTN_SETTINGS_CLICK_ACTION)) {
            Intent settingsIntent = new Intent(context, MainActivity.class).putExtra(MainActivity.LAUNCH_FRAGMENT, MainActivity.FRAGMENT_SETTINGS);
            settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(settingsIntent);
            try {
                Object statusBarManager = context.getSystemService("statusbar");
                Method collapse = statusBarManager.getClass().getMethod("collapsePanels");
                collapse.invoke(statusBarManager);
            } catch (Exception e) {
                Logger.logException(e);
            }
        }
    }
}
