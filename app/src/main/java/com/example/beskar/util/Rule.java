package com.example.beskar.util;

import com.example.beskar.Beskar;

import java.io.File;

public class Rule {
    private String name;
    private String fileName;
    private String downloadUrl;
    private boolean using;

    public Rule(String name, String fileName, String downloadUrl) {
        this.name = name;
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
        this.using = false;
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

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public boolean getDownloaded() {
        File f = new File(Beskar.rulePath + fileName);
        return f.exists();
    }

    public void setUsing(boolean using) { this.using = using; }

    public void setName(String name) {
        this.name = name;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
