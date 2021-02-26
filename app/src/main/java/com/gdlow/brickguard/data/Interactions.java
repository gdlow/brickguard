package com.gdlow.brickguard.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "interactions")
public class Interactions {

    public static final String CONFIG_CHANGE = "config_change";
    public static final String SWITCHED_OFF = "switched_off";

    @PrimaryKey(autoGenerate = true)
    private Integer id;

    @ColumnInfo(name = "timestamp")
    private Long timestamp;

    @ColumnInfo(name = "interaction")
    private String interaction;

    @ColumnInfo(name = "description")
    private String description;

    public Interactions(Long timestamp, String interaction, String description) {
        this.timestamp = timestamp;
        this.interaction = interaction;
        this.description = description;
    }

    public Integer getId() { return id; }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getInteraction() {
        return interaction;
    }

    public String getDescription() { return description; }

    public void setId(Integer id) { this.id = id; }
}

