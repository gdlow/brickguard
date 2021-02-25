package com.example.beskar.ui.home;

import android.content.Intent;
import android.os.Bundle;
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

import com.example.beskar.Beskar;
import com.example.beskar.LockActivity;
import com.example.beskar.MainActivity;
import com.example.beskar.R;
import com.example.beskar.service.BeskarVpnService;
import com.example.beskar.util.PreferencesModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private View root;
    private List<StepState> stepStates;
    private TextView updateText;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);
        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initComponent(root);
    }

    private void initComponent(View view) {
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
        updateText = view.findViewById(R.id.activity_fragment_home_update_text);
        PreferencesModel.refreshModelCache();

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
            PreferencesModel.setCurrSliderIndex(index);
            setUpdateTextVisibility(updateText);
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
        slider.setValue((float) PreferencesModel.getCurrSliderIndex());

        // Set switches in step 2
        SwitchMaterial adultSwitch = view.findViewById(R.id.activity_bottom_sheet_step2_adult_switch);
        adultSwitch.setOnClickListener(v -> {});
        adultSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            PreferencesModel.setCurrAdultSwitchChecked(isChecked);
            setUpdateTextVisibility(updateText);
        });
        adultSwitch.setChecked(PreferencesModel.getCurrAdultSwitchChecked());

        SwitchMaterial adsSwitch = view.findViewById(R.id.activity_bottom_sheet_step2_ads_switch);
        adsSwitch.setOnClickListener(v -> {});
        adsSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            PreferencesModel.setCurrAdsSwitchChecked(isChecked);
            setUpdateTextVisibility(updateText);
        });
        adsSwitch.setChecked(PreferencesModel.getCurrAdsSwitchChecked());

        // Set text input in step 3
        TextView editText = view.findViewById(R.id.activity_bottom_sheet_step3_edit_text);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            // Hide keyboard
            hideKeyboard();
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
                addChip(cg, text);

                // Update preferences model
                PreferencesModel.addToCurrChipMap(textStr);

                // Update updateText visibility
                setUpdateTextVisibility(updateText);

                // Clear text
                editText.setText("");
                return true;
            }
            return false;
        });

        // Set chip group state from preferences model
        Map<String, Boolean> chipMap = PreferencesModel.getCurrChipMap();
        ChipGroup cg = view.findViewById(R.id.activity_bottom_sheet_step3_chip_group);
        Set<String> chips = new HashSet<>();
        for (int i = 0; i < cg.getChildCount(); i++) {
            chips.add(((Chip) cg.getChildAt(i)).getText().toString());
        }
        for (String key : chipMap.keySet()) {
            if (!chips.contains(key)) {
                addChip(cg, key);
            }
        }

        // Set main switch state from BeskarVpnService
        boolean isMainSwitchOn = BeskarVpnService.isActivated();
        SwitchMaterial mainSwitch = view.findViewById(R.id.activity_fragment_home_main_switch);
        TextView mainSwitchText = view.findViewById(R.id.activity_fragment_home_main_switch_text);
        mainSwitch.setChecked(isMainSwitchOn);
        mainSwitchText.setText(isMainSwitchOn ? "DEACTIVATE" : "ACTIVATE");

        mainSwitch.setOnClickListener(v -> {});
        mainSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked) {
                if (!BeskarVpnService.isActivated()) {
                    startActivity(new Intent(getActivity(), LockActivity.class)
                            .putExtra(LockActivity.LOCK_SCREEN_ACTION,
                                    LockActivity.LOCK_SCREEN_ACTION_AUTHENTICATE)
                            .putExtra(MainActivity.LAUNCH_ACTION,
                                    MainActivity.LAUNCH_ACTION_ACTIVATE));
                }
            } else {
                if (BeskarVpnService.isActivated()) {
                    startActivity(new Intent(getActivity(), LockActivity.class)
                            .putExtra(LockActivity.LOCK_SCREEN_ACTION,
                                    LockActivity.LOCK_SCREEN_ACTION_AUTHENTICATE)
                            .putExtra(MainActivity.LAUNCH_ACTION,
                                    MainActivity.LAUNCH_ACTION_DEACTIVATE));
                }
            }
        });

        // Get and show streaks in UI
        TextView currentStreak = view.findViewById(R.id.activity_fragment_home_current_streak);
        long currentStreakDays = Beskar.getPrefs().getLong("beskar_current_time_delta", 0);
        setStreak(currentStreak, currentStreakDays);

        TextView longestStreak = view.findViewById(R.id.activity_fragment_home_longest_streak);
        long longestStreakDays = Beskar.getPrefs().getLong("beskar_longest_time_delta", 0);
        setStreak(longestStreak, longestStreakDays);

        // Hide keyboard
        hideKeyboard();
    }

    private void setUpdateTextVisibility(TextView updateText) {
        if (PreferencesModel.hasChanged()) {
            updateText.setVisibility(View.VISIBLE);
        } else {
            updateText.setVisibility(View.GONE);
        }
    }

    private void setStreak(TextView streak, long days) {
        String text = days + "D";
        streak.setText(text);
    }

    private Chip addChip(ChipGroup cg, CharSequence text) {
        Chip chip = new Chip(getActivity());
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setCheckable(false);
        chip.setOnCloseIconClickListener(chipView -> {
            cg.removeView(chip);
            String textStr = chip.getText().toString();
            PreferencesModel.removeFromCurrChipMap(textStr);
            setUpdateTextVisibility(updateText);
        });
        cg.addView(chip);
        return chip;
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
}
