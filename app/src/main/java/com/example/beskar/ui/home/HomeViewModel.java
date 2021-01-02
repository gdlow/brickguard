package com.example.beskar.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {
    private MutableLiveData<Boolean> isMainButtonChecked;

    public HomeViewModel() {
        isMainButtonChecked = new MutableLiveData<>();
        isMainButtonChecked.setValue(false);
    }

    public LiveData<Boolean> getIsMainButtonChecked() {
        return isMainButtonChecked;
    }

    public void setIsMainButtonChecked(boolean isChecked) {
        isMainButtonChecked.setValue(isChecked);
    }
}