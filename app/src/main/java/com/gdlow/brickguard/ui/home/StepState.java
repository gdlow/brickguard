package com.gdlow.brickguard.ui.home;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class StepState {
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
