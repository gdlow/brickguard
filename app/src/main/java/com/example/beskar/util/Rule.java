package com.example.beskar.util;

import com.example.beskar.Beskar;

import java.io.File;
import java.util.ArrayList;

public class Rule {
    public static final int TYPE_HOSTS = 0;
    public static final int TYPE_DNSMASQ = 1;

    private String name;
    private String fileName;
    private int type;
    private String downloadUrl;
    private boolean using;
    private String id;

    public Rule(String name, String fileName, int type, String downloadUrl, boolean withId) {
        this.name = name;
        this.fileName = fileName;
        this.type = type;
        this.downloadUrl = downloadUrl;
        this.using = false;
        if (withId) {
            this.id = String.valueOf(Beskar.configurations.getNextRuleId());
        }
    }

    public Rule(String name, String fileName, int type, String downloadUrl) {
        this(name, fileName, type, downloadUrl, true);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setUsing(boolean using) {
        this.using = using;
        if (using) {
            if (type == TYPE_HOSTS) {
                for (Rule rule : Beskar.configurations.getDnsmasqRules()) {
                    rule.setUsing(false);
                }
            } else if (type == TYPE_DNSMASQ) {
                for (Rule rule : Beskar.configurations.getHostsRules()) {
                    rule.setUsing(false);
                }
            }
        }
    }

    public boolean isUsing() {
        return using;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public int getType() {
        return type;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setType(int type) {
        this.removeFromConfig();
        this.type = type;
        this.addToConfig();
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void addToConfig() {
        if (getType() == Rule.TYPE_HOSTS) {
            Beskar.configurations.getHostsRules().add(this);
        } else if (getType() == Rule.TYPE_DNSMASQ) {
            Beskar.configurations.getDnsmasqRules().add(this);
        }
    }

    public void removeFromConfig() {
        if (getType() == Rule.TYPE_HOSTS) {
            Beskar.configurations.getHostsRules().remove(this);
        } else if (getType() == Rule.TYPE_DNSMASQ) {
            Beskar.configurations.getDnsmasqRules().remove(this);
        }
        File file = new File(getFileName());
        Logger.info("Delete rule " + getName() + " result: " + file.delete());
    }

    public static String[] getBuildInRuleNames() {
        ArrayList<String> names = new ArrayList<>(Beskar.RULES.size());
        for (Rule rule : Beskar.RULES) {
            names.add(rule.getName() + " - " + getTypeById(rule.getType()));
        }
        String[] strings = new String[names.size()];
        return names.toArray(strings);
    }

    public static String[] getBuildInRuleEntries() {
        ArrayList<String> entries = new ArrayList<>(Beskar.RULES.size());
        for (int i = 0; i < Beskar.RULES.size(); i++) {
            entries.add(String.valueOf(i));
        }
        String[] strings = new String[entries.size()];
        return entries.toArray(strings);
    }

    public static Rule getRuleById(String id) {
        for (Rule rule : Beskar.configurations.getHostsRules()) {
            if (rule.getId().equals(id)) {
                return rule;
            }
        }
        for (Rule rule : Beskar.configurations.getDnsmasqRules()) {
            if (rule.getId().equals(id)) {
                return rule;
            }
        }
        return null;
    }

    public static String getTypeById(int id) {
        switch (id) {
            case TYPE_HOSTS:
                return "Hosts";
            case TYPE_DNSMASQ:
                return "DNSMasq";
            default:
                return "Unknown";
        }
    }
}
