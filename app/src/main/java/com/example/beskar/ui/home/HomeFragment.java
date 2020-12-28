package com.example.beskar.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.beskar.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private HomeViewModel homeViewModel;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);
        initComponent(root);
        return root;
    }

    private void initComponent(View view) {
        List<View> buttons = new ArrayList<>(Arrays.asList(
                view.findViewById(R.id.activity_steppers_container_step1),
                view.findViewById(R.id.activity_steppers_container_step2),
                view.findViewById(R.id.activity_steppers_container_step3)
        ));
        for (View b : buttons) {
            b.setOnClickListener(this);
        }

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
    }

    public void hideKeyboard() {
        getActivity().getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
}
