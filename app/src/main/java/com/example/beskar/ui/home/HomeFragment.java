package com.example.beskar.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

    private HomeViewModel homeViewModel;
    private View root;
    private BottomSheetBehavior bottomSheetBehavior;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);
        initComponent(root);
        return root;
    }

    private void initComponent(View view) {
        // Register buttons
        List<View> buttons = new ArrayList<>(Arrays.asList(
                view.findViewById(R.id.activity_steppers_container_step1),
                view.findViewById(R.id.activity_steppers_container_step2),
                view.findViewById(R.id.activity_steppers_container_step3)
        ));
        for (View b : buttons) {
            b.setOnClickListener(this);
        }

        // Set slider state
        List<String> sliderTexts = new ArrayList<>(Arrays.asList(
                "No filter applied. System default settings used.",
                "Malicious domains (e.g. phishing, malware) blocked.",
                "Adult domains blocked. Search engines set to safe mode. Malicious domains blocked.",
                "Proxies, VPNs & Mixed adult content blocked. Youtube to safe mode. Adult domains" +
                        " blocked. Search engines to safe mode. Malicious domains blocked."
        ));
        Slider slider = view.findViewById(R.id.activity_bottom_sheet_slider);
        TextView tv = view.findViewById(R.id.activity_bottom_sheet_slider_txt_label);
        slider.addOnChangeListener((s, value, fromUser) -> {
            tv.setText(sliderTexts.get(Math.round(value)));
        });

        // Set bottom sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(
                view.findViewById(R.id.bottom_sheet)
        );
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // Hide keyboard
        hideKeyboard();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.activity_steppers_container_step1:
                // Select this step
                toggle(R.id.activity_steppers_txt_label_step1,
                        R.id.activity_steppers_expand_button_step1, true);

                // Unselect other steps
                toggle(R.id.activity_steppers_txt_label_step2,
                        R.id.activity_steppers_expand_button_step2, false);
                toggle(R.id.activity_steppers_txt_label_step3,
                        R.id.activity_steppers_expand_button_step3, false);
                break;
            case R.id.activity_steppers_container_step2:
                // Select this step
                toggle(R.id.activity_steppers_txt_label_step2,
                        R.id.activity_steppers_expand_button_step2, true);

                // Unselect other steps
                toggle(R.id.activity_steppers_txt_label_step1,
                        R.id.activity_steppers_expand_button_step1, false);
                toggle(R.id.activity_steppers_txt_label_step3,
                        R.id.activity_steppers_expand_button_step3, false);
                break;
            case R.id.activity_steppers_container_step3:
                // Select this step
                toggle(R.id.activity_steppers_txt_label_step3,
                        R.id.activity_steppers_expand_button_step3, true);

                // Unselect other steps
                toggle(R.id.activity_steppers_txt_label_step1,
                        R.id.activity_steppers_expand_button_step1, false);
                toggle(R.id.activity_steppers_txt_label_step2,
                        R.id.activity_steppers_expand_button_step2, false);
                break;
        }
    }

    private void toggle(int label, int button, boolean on) {
        TextViewCompat.setTextAppearance(
                root.findViewById(label),
                on ? R.style.StepTextSelectedAppearance : R.style.StepTextUnselectedAppearance
        );
        root.findViewById(button)
                .setBackgroundResource(
                        on ? R.drawable.baseline_expand_less_24 : R.drawable.baseline_expand_more_24
                );
        bottomSheetBehavior.setState(on ? BottomSheetBehavior.STATE_EXPANDED :
                BottomSheetBehavior.STATE_HIDDEN);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                int bottomSheetArrow = R.id.activity_bottom_sheet_expand_button;
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        root.findViewById(bottomSheetArrow)
                                .setBackgroundResource(R.drawable.baseline_expand_less_24);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        root.findViewById(bottomSheetArrow)
                                .setBackgroundResource(R.drawable.baseline_expand_more_24);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });
    }

    public void hideKeyboard() {
        getActivity().getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
}
