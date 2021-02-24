package com.example.beskar.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "local_resolves")
public class LocalResolve {

    public static final String NULL_RES = "0.0.0.0";
    public static final String ONE_RES = "0.0.0.1";

    @PrimaryKey(autoGenerate = true)
    private Integer id;

    @ColumnInfo(name = "timestamp")
    private Long timestamp;

    @ColumnInfo(name = "domain")
    private String domain;

    @ColumnInfo(name = "resolution")
    private String resolution;

    public LocalResolve(Long timestamp, String domain, String resolution) {
        this.timestamp = timestamp;
        this.domain = domain;
        this.resolution = resolution;
    }

    public Integer getId() { return id; }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getDomain() {
        return domain;
    }

    public String getResolution() {
        return resolution;
    }

    public void setId(Integer id) { this.id = id; }
}

