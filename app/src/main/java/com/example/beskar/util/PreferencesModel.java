package com.example.beskar.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.beskar.Beskar;
import com.example.beskar.data.Interactions;
import com.example.beskar.service.BeskarVpnService;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PreferencesModel {
    private int initSliderIndex;
    private boolean initAdultSwitchChecked;
    private boolean initAdsSwitchChecked;
    private Map<String, Boolean> initChipMap;

    private int currSliderIndex;
    private boolean currAdultSwitchChecked;
    private boolean currAdsSwitchChecked;
    private Map<String, Boolean> currChipMap;

    private static PreferencesModel instance;

    private PreferencesModel() {
        // Get initial state from shared preferences
        initSliderIndex = Beskar.getPrefs().getInt("home_slider_index", 3);
        initAdultSwitchChecked = Beskar.getPrefs().getBoolean("home_adult_switch_checked",
                false);
        initAdsSwitchChecked = Beskar.getPrefs().getBoolean("home_ads_switch_checked",
                false);
        initChipMap = loadChipMapFromSharedPreferences();

        // Set current state to initial state
        currSliderIndex = initSliderIndex;
        currAdultSwitchChecked = initAdultSwitchChecked;
        currAdsSwitchChecked = initAdsSwitchChecked;
        currChipMap = new HashMap<>(initChipMap);
    }

    public static void refreshModelCache() {
        instance = new PreferencesModel();
    }

    public static int getCurrSliderIndex() {
        return instance.currSliderIndex;
    }

    public static boolean getCurrAdultSwitchChecked() {
        return instance.currAdultSwitchChecked;
    }

    public static boolean getCurrAdsSwitchChecked() {
        return instance.currAdsSwitchChecked;
    }

    public static Map<String, Boolean> getCurrChipMap() {
        return instance.currChipMap;
    }

    public static void addToCurrChipMap(String customDomain) {
        instance.currChipMap.put(customDomain, true);
    }

    public static void removeFromCurrChipMap(String customDomain) {
        instance.currChipMap.remove(customDomain);
    }

    public static void setCurrSliderIndex(int index) {
        instance.currSliderIndex = index;
    }

    public static void setCurrAdultSwitchChecked(boolean checked) {
        instance.currAdultSwitchChecked = checked;
    }

    public static void setCurrAdsSwitchChecked(boolean checked) {
        instance.currAdsSwitchChecked = checked;
    }

    public static boolean hasChanged() {
        if (instance == null) return false;

        return (instance.initSliderIndex != instance.currSliderIndex) ||
                (instance.initAdultSwitchChecked != instance.currAdultSwitchChecked) ||
                (instance.initAdsSwitchChecked != instance.currAdsSwitchChecked) ||
                (!instance.initChipMap.equals(instance.currChipMap));
    }

    public static void applyChanges(Context context) {
        if (instance == null) return;

        if (instance.initSliderIndex != instance.currSliderIndex) {
            instance.applyChangesToSliderIndex(context);
        }

        if (instance.initAdultSwitchChecked != instance.currAdultSwitchChecked) {
            instance.applyChangesToAdultSwitch();
        }

        if (instance.initAdsSwitchChecked != instance.currAdsSwitchChecked) {
            instance.applyChangesToAdsSwitch();
        }

        if (!instance.initChipMap.equals(instance.currChipMap)) {
            instance.applyChangesToChipMap();
        }

        refreshModelCache();
    }

    private void applyChangesToSliderIndex(Context context) {
        Beskar.getPrefs().edit().putInt("home_slider_index", currSliderIndex).apply();

        if (currSliderIndex > 0) {
            Beskar.getPrefs().edit().putBoolean("settings_use_system_dns", false).apply();
            Beskar.getPrefs().edit().putString("primary_server", String.valueOf(currSliderIndex)).apply();
            Beskar.updateUpstreamServers();
        } else {
            Beskar.getPrefs().edit().putBoolean("settings_use_system_dns", true).apply();
            BeskarVpnService.updateUpstreamToSystemDNS(context);
        }

        Beskar.insertInteraction(Interactions.CONFIG_CHANGE,
                "Filter level set to: " + currSliderIndex);
    }

    private void applyChangesToAdultSwitch() {
        Beskar.getPrefs().edit().putBoolean("home_adult_switch_checked", currAdultSwitchChecked).apply();
        Beskar.selectRule(Beskar.RULES.get(0), currAdultSwitchChecked);
        Beskar.insertInteraction(Interactions.CONFIG_CHANGE,
                "Adult sites turned" + (currAdultSwitchChecked ? "on" : "off"));
    }

    private void applyChangesToAdsSwitch() {
        Beskar.getPrefs().edit().putBoolean("home_ads_switch_checked", currAdsSwitchChecked).apply();
        Beskar.selectRule(Beskar.RULES.get(1), currAdsSwitchChecked);
        Beskar.insertInteraction(Interactions.CONFIG_CHANGE,
                "Ads turned" + (currAdsSwitchChecked ? "on" : "off"));
    }

    private void applyChangesToChipMap() {
        // Add new custom domains
        for (String customDomain : currChipMap.keySet()) {
            if (!initChipMap.containsKey(customDomain)) {
                Beskar.addCustomDomain(customDomain);
                Beskar.insertInteraction(Interactions.CONFIG_CHANGE,
                        "Added custom domain: " + customDomain);
            }
        }

        // Remove old custom domains
        for (String customDomain : initChipMap.keySet()) {
            if (!currChipMap.containsKey(customDomain)) {
                Beskar.removeCustomDomain(customDomain);
                Beskar.insertInteraction(Interactions.CONFIG_CHANGE,
                        "Removed custom domain: " + customDomain);
            }
        }

        saveChipMapInSharedPreferences(currChipMap);
    }

    private void saveChipMapInSharedPreferences(Map<String,Boolean> inputMap){
        JSONObject jsonObject = new JSONObject(inputMap);
        String jsonString = jsonObject.toString();
        SharedPreferences.Editor editor = Beskar.getPrefs().edit();
        editor.remove("home_chip_map").putString("home_chip_map", jsonString).apply();
    }

    private Map<String,Boolean> loadChipMapFromSharedPreferences(){
        Map<String, Boolean> outputMap = new HashMap<>();
        try {
            String jsonString = Beskar.getPrefs().getString("home_chip_map",
                    (new JSONObject()).toString());
            JSONObject jsonObject = new JSONObject(jsonString);
            Iterator<String> keysItr = jsonObject.keys();
            while (keysItr.hasNext()) {
                String key = keysItr.next();
                Boolean value = (Boolean) jsonObject.get(key);
                outputMap.put(key, value);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return outputMap;
    }
}
