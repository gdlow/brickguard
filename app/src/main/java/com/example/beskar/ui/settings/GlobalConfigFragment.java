package com.example.beskar.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.PreferenceFragmentCompat;

import com.example.beskar.Beskar;
import com.example.beskar.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class GlobalConfigFragment extends PreferenceFragmentCompat {

    private MaterialAlertDialogBuilder materialAlertDialogBuilder;
    private View emailDialog;

    // Email report state
    private String email;
    private boolean sendReport;

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
        // Initialize email report state
        email = Beskar.getPrefs().getString("beskar_email", "nil");
        sendReport = Beskar.getPrefs().getBoolean("beskar_send_report", false);

        // Build the alert dialog
        materialAlertDialogBuilder.setView(emailDialog)
                .setPositiveButton("Save", (dialog, which) -> {
                    // Save email in shared preferences
                    Beskar.getPrefs().edit().putString("beskar_email", email).apply();
                    // Save report state in shared preferences
                    Beskar.getPrefs().edit().putBoolean("beskar_send_report", sendReport).apply();
                    Toast.makeText(getContext(), "Email settings saved", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", ((dialog, which) -> {
                    dialog.dismiss();
                }))
                .show();

        // Handle actions
        TextView editText = emailDialog.findViewById(R.id.email_edit_text);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Validate text
                CharSequence text = editText.getText();
                String textStr = text.toString();

                // Invalid email address
                if (!textStr.contains("@") || !textStr.contains(".") || textStr.startsWith(".") ||
                        textStr.endsWith(".") || textStr.contains(" ") || textStr.length() < 4) {
                    editText.setError("Invalid email address");
                    return false;
                }

                // Save email in state
                email = textStr;

                // Clear text
                editText.setText("");

                // Change emailSetText
                TextView emailSetText = emailDialog.findViewById(R.id.email_set_text);
                emailSetText.setText("Setting email to: " + textStr);
                return true;
            }
            return false;
        });

        TextView emailSetText = emailDialog.findViewById(R.id.email_set_text);
        emailSetText.setText(email.equals("nil") ? "No email set" : "Email set to: " + email);

        SwitchMaterial emailReportSwitch = emailDialog.findViewById(R.id.email_report_switch);
        emailReportSwitch.setOnClickListener(v -> {});
        emailReportSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            sendReport = isChecked;
        });
        emailReportSwitch.setChecked(sendReport);
    }
}