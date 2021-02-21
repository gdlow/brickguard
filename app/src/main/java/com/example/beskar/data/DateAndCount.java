package com.example.beskar.data;

import androidx.room.ColumnInfo;

public class DateAndCount {
    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "count")
    private Integer count;

    public DateAndCount(String date, Integer count) {
        this.date = date;
        this.count = count;
    }

    public String getDate() {
        return date;
    }

    public Integer getCount() {
        return count;
    }
}
