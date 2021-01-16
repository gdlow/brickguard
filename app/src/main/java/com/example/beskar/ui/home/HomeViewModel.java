package com.example.beskar.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.beskar.Beskar;
import com.example.beskar.service.BeskarVpnService;

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
        // Source of truth is in BeskarVpnService
        isMainButtonChecked.setValue(BeskarVpnService.isActivated());
        return isMainButtonChecked;
    }

    public LiveData<Boolean> getIsAdultSwitchChecked() {
        // Source of truth is in Beskar RULES
        isAdultSwitchChecked.setValue(Beskar.RULES.get(0).isUsing());
        return isAdultSwitchChecked;
    }

    public LiveData<Boolean> getIsAdsSwitchChecked() {
        // Source of truth is in Beskar RULES
        isAdsSwitchChecked.setValue(Beskar.RULES.get(1).isUsing());
        return isAdsSwitchChecked;
    }

    public LiveData<Integer> getPrimaryDNSIndex() {
        // Source of truth is in Beskar prefs
        Integer index = Beskar.getPrefs().getBoolean("settings_use_system_dns", false) ? 0 :
                Integer.parseInt(Beskar.getPrefs().getString("primary_server", "2"));
        primaryDNSIndex.setValue(index);
        return primaryDNSIndex;
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

    public void setPrimaryDNSIndex(int index) {
        primaryDNSIndex.setValue(index);
    }
}