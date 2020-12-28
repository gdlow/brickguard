package com.example.beskar.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.beskar.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private class StepState {
        public View container;
        public TextView label;
        public ImageView labelArrow;
        public ImageView bottomSheetArrow;
        public BottomSheetBehavior bottomSheetBehavior;
        public boolean on;
        public StepState(View container, TextView label, ImageView labelArrow,
                         ImageView bottomSheetArrow, BottomSheetBehavior bottomSheetBehavior) {
            this.container = container;
            this.label = label;
            this.labelArrow = labelArrow;
            this.bottomSheetArrow = bottomSheetArrow;
            this.bottomSheetBehavior = bottomSheetBehavior;
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
                        (ImageView) view.findViewById(R.id.activity_bottom_sheet_step1_expand_button),
                        BottomSheetBehavior.from(
                                view.findViewById(R.id.bottom_sheet_step1))
                ),
                new StepState(
                        view.findViewById(R.id.activity_steppers_container_step2),
                        (TextView) view.findViewById(R.id.activity_steppers_txt_label_step2),
                        (ImageView) view.findViewById(R.id.activity_steppers_expand_button_step2),
                        (ImageView) view.findViewById(R.id.activity_bottom_sheet_step2_expand_button),
                        BottomSheetBehavior.from(
                                view.findViewById(R.id.bottom_sheet_step2))
                ),
                new StepState(
                        view.findViewById(R.id.activity_steppers_container_step3),
                        (TextView) view.findViewById(R.id.activity_steppers_txt_label_step3),
                        (ImageView) view.findViewById(R.id.activity_steppers_expand_button_step3),
                        (ImageView) view.findViewById(R.id.activity_bottom_sheet_step3_expand_button),
                        BottomSheetBehavior.from(
                                view.findViewById(R.id.bottom_sheet_step3))
                )
        ));

        // Set initial state
        for (StepState stepState : stepStates) {
            stepState.container.setOnClickListener(this);
            stepState.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            stepState.bottomSheetBehavior.addBottomSheetCallback(
                    new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    switch (newState) {
                        case BottomSheetBehavior.STATE_COLLAPSED:
                            stepState.bottomSheetArrow
                                    .setBackgroundResource(R.drawable.baseline_expand_less_24);
                            break;
                        case BottomSheetBehavior.STATE_EXPANDED:
                            stepState.bottomSheetArrow
                                    .setBackgroundResource(R.drawable.baseline_expand_more_24);
                            break;
                        case BottomSheetBehavior.STATE_HIDDEN:
                            toggle(stepState, false);
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
            });
        }

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
        }
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
