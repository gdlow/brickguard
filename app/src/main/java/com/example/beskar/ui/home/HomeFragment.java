package com.example.beskar.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.beskar.Beskar;
import com.example.beskar.MainActivity;
import com.example.beskar.R;
import com.example.beskar.service.BeskarVpnService;
import com.example.beskar.util.Logger;
import com.example.beskar.util.Rule;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private class StepState {
        public View container;
        public TextView label;
        public ImageView labelArrow;
        public Button nextButton;
        public BottomSheetBehavior bottomSheetBehavior;
        public TextView showMoreText;
        public Button showMoreButton;
        public boolean on;
        public StepState(View container, TextView label, ImageView labelArrow, Button nextButton,
                         BottomSheetBehavior bottomSheetBehavior, TextView showMoreText,
                         Button showMoreButton) {
            this.container = container;
            this.label = label;
            this.labelArrow = labelArrow;
            this.nextButton = nextButton;
            this.bottomSheetBehavior = bottomSheetBehavior;
            this.showMoreText = showMoreText;
            this.showMoreButton = showMoreButton;
            this.on = false;
        }
    }

    private HomeViewModel homeViewModel;
    private View root;
    private List<StepState> stepStates;
    private Thread mThread = null;
    private RuleConfigHandler mHandler = null;
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);
        mHandler = new RuleConfigHandler().setView(root);
        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initComponent(root);
    }

    private void initComponent(View view) {
        // Initialise home view model
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        // Build list of stepStates
        stepStates = new ArrayList<>(Arrays.asList(
                new StepState(
                        view.findViewById(R.id.activity_steppers_container_step1),
                        (TextView) view.findViewById(R.id.activity_steppers_txt_label_step1),
                        (ImageView) view.findViewById(R.id.activity_steppers_expand_button_step1),
                        (Button) view.findViewById(R.id.activity_bottom_sheet_step1_next_button),
                        BottomSheetBehavior.from(
                                view.findViewById(R.id.bottom_sheet_step1)),
                        (TextView) view.findViewById(R.id.activity_bottom_sheet_step1_more_info_text),
                        (Button) view.findViewById(R.id.activity_bottom_sheet_step1_more_info_button)
                ),
                new StepState(
                        view.findViewById(R.id.activity_steppers_container_step2),
                        (TextView) view.findViewById(R.id.activity_steppers_txt_label_step2),
                        (ImageView) view.findViewById(R.id.activity_steppers_expand_button_step2),
                        (Button) view.findViewById(R.id.activity_bottom_sheet_step2_next_button),
                        BottomSheetBehavior.from(
                                view.findViewById(R.id.bottom_sheet_step2)),
                        (TextView) view.findViewById(R.id.activity_bottom_sheet_step2_more_info_text),
                        (Button) view.findViewById(R.id.activity_bottom_sheet_step2_more_info_button)
                ),
                new StepState(
                        view.findViewById(R.id.activity_steppers_container_step3),
                        (TextView) view.findViewById(R.id.activity_steppers_txt_label_step3),
                        (ImageView) view.findViewById(R.id.activity_steppers_expand_button_step3),
                        (Button) view.findViewById(R.id.activity_bottom_sheet_step3_next_button),
                        BottomSheetBehavior.from(
                                view.findViewById(R.id.bottom_sheet_step3)),
                        (TextView) view.findViewById(R.id.activity_bottom_sheet_step3_more_info_text),
                        (Button) view.findViewById(R.id.activity_bottom_sheet_step3_more_info_button)
                )
        ));

        // Set initial state
        for (StepState stepState : stepStates) {
            stepState.container.setOnClickListener(this);
            stepState.nextButton.setOnClickListener(this);
            stepState.showMoreButton.setOnClickListener(this);
            stepState.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            stepState.bottomSheetBehavior.addBottomSheetCallback(
                    new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    switch (newState) {
                        case BottomSheetBehavior.STATE_HIDDEN:
                            toggle(stepState, false);

                            // Clear error message
                            TextView tv =
                                    (TextView) root.findViewById(R.id.activity_bottom_sheet_step3_edit_text);
                            tv.setError(null);

                            // Toggle show more info
                            showMoreToggle(stepState, false);
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
            });
        }

        // Set individual bottom sheet components

        // Set slider state in step 1
        List<String> sliderTexts = new ArrayList<>(Arrays.asList(
                "No filter applied. System default settings used.",
                "Malicious domains (e.g. phishing, malware) blocked.",
                "Adult domains blocked. Search engines set to safe mode. Malicious domains blocked.",
                "Proxies, VPNs & Mixed adult content blocked. Youtube to safe mode. Adult domains" +
                        " blocked. Search engines to safe mode. Malicious domains blocked."
        ));
        Slider slider = view.findViewById(R.id.activity_bottom_sheet_step1_slider);
        TextView tv = view.findViewById(R.id.activity_bottom_sheet_step1_slider_txt_label);
        slider.addOnChangeListener((s, value, fromUser) -> {
            int index = Math.round(value);
            tv.setText(sliderTexts.get(index));
            homeViewModel.setPrimaryDNSIndex(index);

            if (index > 0) {
                Beskar.getPrefs().edit().putBoolean("settings_use_system_dns", false).apply();
                Beskar.getPrefs().edit().putString("primary_server", String.valueOf(index)).apply();
                Beskar.updateUpstreamServers();
            } else {
                Beskar.getPrefs().edit().putBoolean("settings_use_system_dns", true).apply();
                BeskarVpnService.updateUpstreamToSystemDNS(getContext());
            }
        });
        slider.setLabelFormatter((float value) -> {
            switch (Math.round(value)) {
                case 0:
                    return "None";
                case 1:
                    return "Low";
                case 2:
                    return "Medium";
                case 3:
                    return "High";
                default:
                    return "Value: " + value;
            }
        });
        homeViewModel.getPrimaryDNSIndex().observe(getViewLifecycleOwner(), index -> {
            slider.setValue((float) index);
        });

        // Set switches in step 2
        SwitchMaterial adultSwitch = view.findViewById(R.id.activity_bottom_sheet_step2_adult_switch);
        adultSwitch.setOnClickListener(v -> {});
        adultSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            selectRule(Beskar.RULES.get(0), isChecked);
            homeViewModel.setIsAdultSwitchChecked(isChecked);
        });
        homeViewModel.getIsAdultSwitchChecked().observe(getViewLifecycleOwner(), adultSwitch::setChecked);

        SwitchMaterial adsSwitch = view.findViewById(R.id.activity_bottom_sheet_step2_ads_switch);
        adsSwitch.setOnClickListener(v -> {});
        adsSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            selectRule(Beskar.RULES.get(1), isChecked);
            homeViewModel.setIsAdsSwitchChecked(isChecked);
        });
        homeViewModel.getIsAdsSwitchChecked().observe(getViewLifecycleOwner(), adsSwitch::setChecked);

        // Set text input in step 3
        TextView editText = view.findViewById(R.id.activity_bottom_sheet_step3_edit_text);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Validate text
                CharSequence text = editText.getText();
                String textStr = text.toString();

                // Invalid URI
                if (!textStr.contains(".") || textStr.startsWith(".") || textStr.endsWith(".") ||
                        textStr.contains(" ") || textStr.length() < 4) {
                    editText.setError("Invalid domain");
                    return false;
                }

                // Add new chip to chip group
                ChipGroup cg = view.findViewById(R.id.activity_bottom_sheet_step3_chip_group);
                Chip chip = new Chip(getActivity());
                chip.setText(text);
                chip.setCloseIconVisible(true);
                chip.setCheckable(false);
                chip.setOnCloseIconClickListener(chipView -> {
                    cg.removeView(chip);
                });
                cg.addView(chip);

                // Clear text
                editText.setText("");
                return true;
            }
            return false;
        });

        // Set main switch
        SwitchMaterial mainSwitch = view.findViewById(R.id.activity_fragment_home_main_switch);
        homeViewModel.getIsMainButtonChecked().observe(getViewLifecycleOwner(), isChecked -> {
            TextView mainSwitchText =
                    view.findViewById(R.id.activity_fragment_home_main_switch_text);
            mainSwitchText.setText(isChecked ? "DEACTIVATE" : "ACTIVATE");
            mainSwitch.setChecked(isChecked);
        });

        mainSwitch.setOnClickListener(v -> {});
        mainSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked) {
                if (!BeskarVpnService.isActivated()) {
                    startActivity(new Intent(getActivity(), MainActivity.class)
                            .putExtra(MainActivity.LAUNCH_ACTION,
                                    MainActivity.LAUNCH_ACTION_ACTIVATE));
                }
            } else {
                if (BeskarVpnService.isActivated()) {
                    Beskar.deactivateService(getActivity().getApplicationContext());
                }
            }
            homeViewModel.setIsMainButtonChecked(isChecked);
        });

        // Hide keyboard
        hideKeyboard();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.activity_steppers_container_step1:
                onClickHelper(0);
                break;
            case R.id.activity_steppers_container_step2:
                onClickHelper(1);
                break;
            case R.id.activity_steppers_container_step3:
                onClickHelper(2);
                break;
            case R.id.activity_bottom_sheet_step1_next_button:
                onClickHelper(1);
                break;
            case R.id.activity_bottom_sheet_step2_next_button:
                onClickHelper(2);
                break;
            case R.id.activity_bottom_sheet_step3_next_button:
                // Close all bottom sheets
                onClickHelper(-1);
                break;
            case R.id.activity_bottom_sheet_step1_more_info_button:
                showMoreTextHelper(0);
                break;
            case R.id.activity_bottom_sheet_step2_more_info_button:
                showMoreTextHelper(1);
                break;
            case R.id.activity_bottom_sheet_step3_more_info_button:
                showMoreTextHelper(2);
                break;
        }
    }

    private void showMoreTextHelper(int chosen) {
        StepState chosenStepState = stepStates.get(chosen);
        boolean turn_on = chosenStepState.showMoreText.getVisibility() != View.VISIBLE;
        showMoreToggle(chosenStepState, turn_on);
    }

    private void showMoreToggle(StepState stepState, boolean turn_on) {
        stepState.showMoreText.setVisibility(turn_on ? View.VISIBLE : View.GONE);
        stepState.showMoreButton.setText(turn_on ? "LESS INFO" : "MORE INFO");
    }

    private void onClickHelper(int chosen) {
        for (int i = 0; i < stepStates.size(); i++) {
            if (i == chosen) {
                // Toggle the chosen step
                StepState chosenStepState = stepStates.get(chosen);
                toggle(chosenStepState, !chosenStepState.on);
            } else {
                // Turn off all other steps
                toggle(stepStates.get(i), false);
            }
        }
    }

    private void toggle(StepState stepState, boolean turn_on) {
        TextViewCompat.setTextAppearance(
                stepState.label,
                turn_on ? R.style.StepTextSelectedAppearance : R.style.StepTextUnselectedAppearance
        );
        stepState.labelArrow
                .setBackgroundResource(
                        turn_on ? R.drawable.baseline_expand_less_24 :
                                R.drawable.baseline_expand_more_24
                );
        stepState.bottomSheetBehavior.setState(turn_on ? BottomSheetBehavior.STATE_EXPANDED :
                BottomSheetBehavior.STATE_HIDDEN);

        stepState.on = turn_on;
    }

    public void hideKeyboard() {
        getActivity().getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void selectRule(Rule rule, boolean isUsing) {
        if (isUsing && !rule.getDownloaded()) {
            ruleSync(rule);
            rule.setDownloaded(true);
        }
        rule.setUsing(isUsing);
        Beskar.setRulesChanged();
    }

    private boolean ruleSync(Rule rule) {
        String ruleFilename = rule.getFileName();
        String ruleDownloadUrl = rule.getDownloadUrl();

        if (mThread == null) {
            Snackbar.make(getView(), R.string.notice_start_download, Snackbar.LENGTH_SHORT).show();
            if (ruleDownloadUrl.startsWith("content:/")) {
                mThread = new Thread(() -> {
                    try {
                        InputStream inputStream = getActivity().getContentResolver().openInputStream(Uri.parse(ruleDownloadUrl));
                        int readLen;
                        byte[] data = new byte[1024];
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        while ((readLen = inputStream.read(data)) != -1) {
                            buffer.write(data, 0, readLen);
                        }
                        inputStream.close();
                        buffer.flush();
                        mHandler.obtainMessage(RuleConfigHandler.MSG_RULE_DOWNLOADED,
                                new RuleData(ruleFilename, buffer.toByteArray())).sendToTarget();
                    } catch (Exception e) {
                        Logger.logException(e);
                    } finally {
                        stopThread();
                    }
                });
            } else {
                mThread = new Thread(() -> {
                    try {
                        Request request = new Request.Builder()
                                .url(ruleDownloadUrl).get().build();
                        Response response = HTTP_CLIENT.newCall(request).execute();
                        Logger.info("Downloaded " + ruleDownloadUrl);
                        if (response.isSuccessful() && mHandler != null) {
                            mHandler.obtainMessage(RuleConfigHandler.MSG_RULE_DOWNLOADED,
                                    new RuleData(ruleFilename, response.body().bytes())).sendToTarget();
                        }
                    } catch (Exception e) {
                        Logger.logException(e);
                        if (mHandler != null) {
                            mHandler.obtainMessage(RuleConfigHandler.MSG_RULE_DOWNLOADED,
                                    new RuleData(ruleFilename, new byte[0])).sendToTarget();
                        }
                    } finally {
                        stopThread();
                    }
                });
            }
            mThread.start();
        } else {
            Snackbar.make(getView(), R.string.notice_now_downloading, Snackbar.LENGTH_LONG).show();
        }
        return false;
    }

    private void stopThread() {
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
    }

    private class RuleData {
        private byte[] data;
        private String filename;

        RuleData(String filename, byte[] data) {
            this.data = data;
            this.filename = filename;
        }

        byte[] getData() {
            return data;
        }

        String getFilename() {
            return filename;
        }
    }

    private static class RuleConfigHandler extends Handler {
        static final int MSG_RULE_DOWNLOADED = 0;

        private View view = null;

        RuleConfigHandler setView(View view) {
            this.view = view;
            return this;
        }

        void shutdown() {
            view = null;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_RULE_DOWNLOADED:
                    RuleData ruleData = (RuleData) msg.obj;
                    if (ruleData.data.length == 0) {
                        if (view != null) {
                            Snackbar.make(view, R.string.notice_download_failed, Snackbar.LENGTH_SHORT).show();
                        }
                        break;
                    }
                    try {
                        File file = new File(Beskar.rulePath + ruleData.getFilename());
                        FileOutputStream stream = new FileOutputStream(file);
                        stream.write(ruleData.getData());
                        stream.close();
                    } catch (Exception e) {
                        Logger.logException(e);
                    }

                    if (view != null) {
                        Snackbar.make(view, R.string.notice_downloaded, Snackbar.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }
}
