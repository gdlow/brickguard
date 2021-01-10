package com.example.beskar.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {
    private MutableLiveData<Boolean> isMainButtonChecked;
    private MutableLiveData<Boolean> isAdultSwitchChecked;
    private MutableLiveData<Boolean> isAdsSwitchChecked;

    public HomeViewModel() {
        isMainButtonChecked = new MutableLiveData<>();
        isMainButtonChecked.setValue(false);

        isAdultSwitchChecked = new MutableLiveData<>();
        isAdultSwitchChecked.setValue(false);

        isAdsSwitchChecked = new MutableLiveData<>();
        isAdsSwitchChecked.setValue(false);
    }

    public LiveData<Boolean> getIsMainButtonChecked() {
        return isMainButtonChecked;
    }

    public LiveData<Boolean> getIsAdultSwitchChecked() {
        return isAdultSwitchChecked;
    }

    public LiveData<Boolean> getIsAdsSwitchChecked() {
        return isAdsSwitchChecked;
    }

    public void setIsMainButtonChecked(boolean isChecked) {
        isMainButtonChecked.setValue(isChecked);
    }

    public void setIsAdultSwitchChecked(boolean isChecked) {
        isAdultSwitchChecked.setValue(isChecked);
    }

    public void setIsAdsSwitchChecked(boolean isChecked) {
        isAdsSwitchChecked.setValue(isChecked);
    }
}