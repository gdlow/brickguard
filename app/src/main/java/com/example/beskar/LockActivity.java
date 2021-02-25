package com.example.beskar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.example.beskar.util.Logger;

public class LockActivity extends AppCompatActivity {

    private PinLockView mPinLockView;
    private IndicatorDots mIndicatorDots;
    private TextView mLockScreenDescription;

    private static final int PIN_LENGTH = 4;

    public static final String PIN_UNSET = "PIN_UNSET";
    public static final String LOCK_SCREEN_ACTION = "com.example.Beskar.LockActivity.LOCK_SCREEN_ACTION";
    public static final int LOCK_SCREEN_ACTION_SET_UP = 0;
    public static final int LOCK_SCREEN_ACTION_AUTHENTICATE = 1;
    public static final int LOCK_SCREEN_ACTION_RESET = 2;
    public static final int LOCK_SCREEN_ACTION_NONE = 3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lock);

        mPinLockView = (PinLockView) findViewById(R.id.pin_lock_view);
        mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);
        mLockScreenDescription = (TextView) findViewById(R.id.lock_screen_description);

        //attach lock view with dot indicator
        mPinLockView.attachIndicatorDots(mIndicatorDots);

        //set lock code length
        mPinLockView.setPinLength(PIN_LENGTH);

        // Change behaviour based on intent
        updateOnNewIntent(getIntent());
    }

    private void updateOnNewIntent(Intent intent) {
        if (!Beskar.getPrefs().contains("beskar_pin")) {
            handleSetUpIntent();
        }

        int lockScreenAction = intent.getIntExtra(LOCK_SCREEN_ACTION, LOCK_SCREEN_ACTION_NONE);
        switch (lockScreenAction) {
            case LOCK_SCREEN_ACTION_SET_UP:
                handleSetUpIntent();
                break;
            case LOCK_SCREEN_ACTION_AUTHENTICATE:
                handleAuthenticateIntent();
                break;
            case LOCK_SCREEN_ACTION_RESET:
                handleResetIntent();
                break;
        }
    }

    private void handleSetUpIntent() {
        mLockScreenDescription.setText("Enter a new PIN");
        mPinLockView.setPinLockListener(new PinLockListener() {

            private int count = 0;
            private final int verified = 2;
            private String enteredPin = PIN_UNSET;

            @Override
            public void onComplete(String pin) {
                Logger.debug("Lock code: " + pin);

                if (++count < verified) {
                    enteredPin = pin;
                    mPinLockView.resetPinLockView();
                    mLockScreenDescription.setText("Re-enter PIN");
                } else if (!enteredPin.equals(pin)) {
                    count = 0;
                    enteredPin = PIN_UNSET;
                    mPinLockView.resetPinLockView();
                    mLockScreenDescription.setText("Enter a new PIN");
                    Toast.makeText(LockActivity.this, "Pin does not match, try again!",
                            Toast.LENGTH_SHORT).show();
                    Logger.debug("Entered pin is wrong. Entered: " + pin + " , previous: " + enteredPin);
                } else {
                    Beskar.getPrefs().edit().putString("beskar_pin", pin).apply();
                    startActivity(new Intent(LockActivity.this, MainActivity.class)
                            .putExtra(MainActivity.LAUNCH_ACTION, MainActivity.LAUNCH_ACTION_NONE));
                    finish();
                }
            }

            @Override
            public void onEmpty() {}

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {}
        });
    }

    private void handleAuthenticateIntent() {
        mLockScreenDescription.setText("Enter PIN");
        mPinLockView.setPinLockListener(new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                Logger.debug("Lock code: " + pin);

                String correct = Beskar.getPrefs().getString("beskar_pin", PIN_UNSET);
                if (pin.equals(correct)) {
                    int launchAction = getIntent().getIntExtra(MainActivity.LAUNCH_ACTION,
                            MainActivity.LAUNCH_ACTION_NONE);
                    startActivity(new Intent(LockActivity.this, MainActivity.class)
                            .putExtra(MainActivity.LAUNCH_ACTION, launchAction));
                    finish();
                } else {
                    mPinLockView.resetPinLockView();
                    Toast.makeText(LockActivity.this, "Failed code, try again!",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onEmpty() {}

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {}
        });
    }

    private void handleResetIntent() {
        mLockScreenDescription.setText("Enter PIN");
        mPinLockView.setPinLockListener(new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                Logger.debug("Lock code: " + pin);

                String correct = Beskar.getPrefs().getString("beskar_pin", PIN_UNSET);
                if (pin.equals(correct)) {
                    startActivity(new Intent(LockActivity.this, LockActivity.class)
                            .putExtra(LOCK_SCREEN_ACTION, LOCK_SCREEN_ACTION_SET_UP));
                    finish();
                } else {
                    mPinLockView.resetPinLockView();
                    Toast.makeText(LockActivity.this, "Failed code, try again!",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onEmpty() {}

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {}
        });
    }
}