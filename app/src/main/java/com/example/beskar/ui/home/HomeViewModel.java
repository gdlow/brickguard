package com.example.beskar.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.beskar.Beskar;

public class HomeViewModel extends ViewModel {
    private MutableLiveData<Boolean> isMainButtonChecked;
    private MutableLiveData<Boolean> isAdultSwitchChecked;
    private MutableLiveData<Boolean> isAdsSwitchChecked;
    private MutableLiveData<Integer> primaryDNSIndex;

    public HomeViewModel() {
        isMainButtonChecked = new MutableLiveData<>();
        isMainButtonChecked.setValue(false);

        isAdultSwitchChecked = new MutableLiveData<>();
        isAdultSwitchChecked.setValue(false);

        isAdsSwitchChecked = new MutableLiveData<>();
        isAdsSwitchChecked.setValue(false);

        primaryDNSIndex = new MutableLiveData<>();
        primaryDNSIndex.setValue(2); // default value in slider
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

    public LiveData<Integer> getPrimaryDNSIndex() { return primaryDNSIndex; }

    public void setIsMainButtonChecked(boolean isChecked) {
        isMainButtonChecked.setValue(isChecked);
    }

    public void setIsAdultSwitchChecked(boolean isChecked) {
        isAdultSwitchChecked.setValue(isChecked);
    }

    public void setIsAdsSwitchChecked(boolean isChecked) {
        isAdsSwitchChecked.setValue(isChecked);
    }

    public void setPrimaryDNSIndex(int index) {
        primaryDNSIndex.setValue(index);
    }
}