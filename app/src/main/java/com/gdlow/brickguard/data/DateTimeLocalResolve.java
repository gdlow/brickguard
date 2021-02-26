package com.gdlow.brickguard.data;

import androidx.room.ColumnInfo;

public class DateTimeLocalResolve {

    @ColumnInfo(name = "datetime")
    private String datetime;

    @ColumnInfo(name = "domain")
    private String domain;

    @ColumnInfo(name = "resolution")
    private String resolution;

    public DateTimeLocalResolve(String datetime, String domain, String resolution) {
        this.datetime = datetime;
        this.domain = domain;
        this.resolution = resolution;
    }

    public String getDatetime() {
        return datetime;
    }

    public String getDomain() {
        return domain;
    }

    public String getResolution() {
        return resolution;
    }
}
