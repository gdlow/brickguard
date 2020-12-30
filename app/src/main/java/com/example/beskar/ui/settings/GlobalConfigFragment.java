package com.example.beskar.ui.settings;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

import com.example.beskar.R;

public class GlobalConfigFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_settings);
    }
}