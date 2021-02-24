package com.example.beskar.data;

import androidx.room.ColumnInfo;

public class DateTimeInteractions {
    @ColumnInfo(name = "datetime")
    private String datetime;

    @ColumnInfo(name = "interaction")
    private String interaction;

    @ColumnInfo(name = "description")
    private String description;

    public DateTimeInteractions(String datetime, String interaction, String description) {
        this.datetime = datetime;
        this.interaction = interaction;
        this.description = description;
    }

    public String getDateTime() {
        return datetime;
    }

    public String getInteraction() {
        return interaction;
    }

    public String getDescription() {
        return description;
    }
}