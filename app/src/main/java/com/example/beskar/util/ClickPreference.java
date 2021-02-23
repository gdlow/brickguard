package com.example.beskar.util;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.ListPreference;

public class ClickPreference extends ListPreference {

    public ClickPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ClickPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickPreference(Context context) {
        super(context);
    }

    @Override
    protected void onClick() {
    }
}
