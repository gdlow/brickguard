package com.example.beskar.util;

import com.example.beskar.Beskar;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Configurations {
    private static final int CUSTOM_ID_START = 32;

    private static File file;

    private ArrayList<String> appObjects;

    private ArrayList<Rule> hostsRules;
    private ArrayList<Rule> dnsmasqRules;

    private int totalDnsId;
    private int totalRuleId;

    private long activateCounter;

    public int getNextDnsId() {
        if (totalDnsId < CUSTOM_ID_START) {
            totalDnsId = CUSTOM_ID_START;
        }
        return totalDnsId++;
    }

    int getNextRuleId() {
        if (totalRuleId < 0) {
            totalRuleId = 0;
        }
        return totalRuleId++;
    }

    public long getActivateCounter() {
        return activateCounter;
    }

    public void setActivateCounter(long activateCounter) {
        this.activateCounter = activateCounter;
    }

    public ArrayList<String> getAppObjects() {
        if (appObjects == null) {
            appObjects = new ArrayList<>();
        }
        return appObjects;
    }

    public ArrayList<Rule> getHostsRules() {
        if (hostsRules == null) {
            hostsRules = new ArrayList<>();
        }
        return hostsRules;
    }

    public ArrayList<Rule> getDnsmasqRules() {
        if (dnsmasqRules == null) {
            dnsmasqRules = new ArrayList<>();
        }
        return dnsmasqRules;
    }

    public ArrayList<Rule> getUsingRules() {
        if (hostsRules != null && hostsRules.size() > 0) {
            for (Rule rule : hostsRules) {
                if (rule.isUsing()) {
                    return hostsRules;
                }
            }
        }
        if (dnsmasqRules != null && dnsmasqRules.size() > 0) {
            for (Rule rule : dnsmasqRules) {
                if (rule.isUsing()) {
                    return dnsmasqRules;
                }
            }
        }
        return hostsRules;
    }

    public int getUsingRuleType() {
        if (hostsRules != null && hostsRules.size() > 0) {
            for (Rule rule : hostsRules) {
                if (rule.isUsing()) {
                    return Rule.TYPE_HOSTS;
                }
            }
        }
        if (dnsmasqRules != null && dnsmasqRules.size() > 0) {
            for (Rule rule : dnsmasqRules) {
                if (rule.isUsing()) {
                    return Rule.TYPE_DNSMASQ;
                }
            }
        }
        return Rule.TYPE_HOSTS;
    }

    public static Configurations load(File file) {
        Configurations.file = file;
        Configurations config = null;
        if (file.exists()) {
            try {
                config = Beskar.parseJson(Configurations.class, new JsonReader(new FileReader(file)));
                Logger.info("Load configuration successfully from " + file);
            } catch (Exception e) {
                Logger.logException(e);
            }
        }

        if (config == null) {
            Logger.info("Load configuration failed. Generating default configurations.");
            config = new Configurations();
        }

        return config;
    }

    public Configurations() {
        //TODO: Initial config. Eg. Build-in rules
    }

    public void save() {
        try {
            if (file != null) {
                FileWriter writer = new FileWriter(file);
                new Gson().toJson(this, writer);
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            Logger.logException(e);
        }
    }
}
