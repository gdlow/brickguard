package com.example.beskar;

import android.content.Intent;
import android.os.Bundle;
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
    private final static String TRUE_CODE = "1234";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lock);

        mPinLockView = (PinLockView) findViewById(R.id.pin_lock_view);
        mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);

        //attach lock view with dot indicator
        mPinLockView.attachIndicatorDots(mIndicatorDots);

        //set lock code length
        mPinLockView.setPinLength(TRUE_CODE.length());

        //set listener for lock code change
        mPinLockView.setPinLockListener(new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                Logger.debug("Lock code: " + pin);

                //User input true code
                if (pin.equals(TRUE_CODE)) {
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
            public void onEmpty() {
                Logger.debug("Lock code is empty!");
            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {
                Logger.debug("Pin changed, new length " + pinLength + " with intermediate pin " + intermediatePin);
            }
        });
    }
}