package com.example.beskar.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.beskar.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);
        initComponent(root);
        return root;
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
            tv.setText(sliderTexts.get(Math.round(value)));
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

        // Set switches in step 2
        SwitchMaterial adultSwitch = view.findViewById(R.id.activity_bottom_sheet_step2_adult_switch);
        adultSwitch.setOnClickListener(v -> {});
        adultSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            // Add logic here
        });
        SwitchMaterial adsSwitch = view.findViewById(R.id.activity_bottom_sheet_step2_ads_switch);
        adsSwitch.setOnClickListener(v -> {});
        adsSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            // Add logic here
        });

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
        mainSwitch.setOnClickListener(v -> {});
        mainSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            TextView mainSwitchText =
                    view.findViewById(R.id.activity_fragment_home_main_switch_text);
            mainSwitchText.setText(isChecked ? "D E A C T I V A T E" : "A C T I V A T E");
            Toast.makeText(getActivity(), "Service " + (isChecked ? "activated" : "deactivated"),
                    Toast.LENGTH_SHORT).show();
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
}
