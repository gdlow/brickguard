package com.example.beskar.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.beskar.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private HomeViewModel homeViewModel;

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
                // do something here for step 1
                break;
            case R.id.activity_steppers_container_step2:
                // do something here for step 2
                break;
            case R.id.activity_steppers_container_step3:
                // do something here for step 3
                break;
        }
    }

    public void hideKeyboard() {
        getActivity().getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
}
