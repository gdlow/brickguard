package com.example.beskar.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.preference.PreferenceFragmentCompat;

import com.example.beskar.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class GlobalConfigFragment extends PreferenceFragmentCompat {

    private MaterialAlertDialogBuilder materialAlertDialogBuilder;
    private View emailDialog;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_settings);
        materialAlertDialogBuilder = new MaterialAlertDialogBuilder(getContext());
        findPreference("settings_email").setOnPreferenceClickListener(preference -> {
            emailDialog = LayoutInflater.from(getContext()).inflate(R.layout.email_dialog, null,
                    false);
            launchEmailDialog();
            return true;
        });
    }

    private void launchEmailDialog() {
        // Build the alert dialog
        materialAlertDialogBuilder.setView(emailDialog)
                .setPositiveButton("Set", (dialog, which) -> {
                    // Do whatever you want here
                })
                .setNegativeButton("Cancel", ((dialog, which) -> {
                    Toast.makeText(getContext(), "Operation cancelled!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }))
                .show();
    }
}