package com.example.beskar.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "interactions")
public class Interactions {
    @PrimaryKey(autoGenerate = true)
    private Integer id;

    @ColumnInfo(name = "timestamp")
    private Long timestamp;

    @ColumnInfo(name = "interaction")
    private String interaction;

    public Interactions(Long timestamp, String interaction) {
        this.timestamp = timestamp;
        this.interaction = interaction;
    }

    public Integer getId() { return id; }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getInteraction() {
        return interaction;
    }

    public void setId(Integer id) { this.id = id; }
}

