package com.example.beskar.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;

import com.danimahardhika.android.helpers.animation.AnimationHelper;
import com.example.beskar.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private HomeViewModel homeViewModel;
    private List<View> layoutList = new ArrayList<>();
    private int currentStep = 0;
    private int completeStep = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        initComponent(root);
        return root;
    }

    //initialize component
    private void initComponent(View view) {
        // populate layout field
        layoutList.add(view.findViewById(R.id.activity_steppers_layout_step1));
        layoutList.add(view.findViewById(R.id.activity_steppers_layout_step2));
        layoutList.add(view.findViewById(R.id.activity_steppers_layout_step3));

        //set visibility of view
        for (View v : layoutList) {
            v.setVisibility(View.GONE);
        }

        //display visible step
        layoutList.get(0).setVisibility(View.VISIBLE);

        //register buttons
        List<View> buttons = new ArrayList<>(Arrays.asList(
                view.findViewById(R.id.activity_steppers_txt_label_step1),
                view.findViewById(R.id.activity_steppers_txt_label_step2),
                view.findViewById(R.id.activity_steppers_txt_label_step3)
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
            case R.id.activity_steppers_txt_label_step1:
                if (completeStep >= 0 && currentStep != 0) {
                    currentStep = 0;
                    closeStep();
                    AnimationHelper.slideDownIn(layoutList.get(0))
                            .interpolator(new LinearOutSlowInInterpolator())
                            .duration(50)
                            .start();
                }
                break;
            case R.id.activity_steppers_txt_label_step2:
                if (completeStep >= 1 && currentStep != 1) {
                    currentStep = 1;
                    closeStep();
                    AnimationHelper.slideDownIn(layoutList.get(1))
                            .interpolator(new LinearOutSlowInInterpolator())
                            .duration(50)
                            .start();
                }
                break;
            case R.id.activity_steppers_txt_label_step3:
                if (completeStep >= 2 && currentStep != 2) {
                    currentStep = 2;
                    closeStep();
                    AnimationHelper.slideDownIn(layoutList.get(2))
                            .interpolator(new LinearOutSlowInInterpolator())
                            .duration(50)
                            .start();
                }
                break;
        }
    }

    private void closeStep() {
        for (View v : layoutList) {
            AnimationHelper.slideUpOut(v)
                    .interpolator(new LinearOutSlowInInterpolator())
                    .duration(50)
                    .start();
        }
    }

    public void hideKeyboard() {
        getActivity().getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
}
