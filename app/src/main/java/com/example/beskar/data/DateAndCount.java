package com.example.beskar.data;

import androidx.room.ColumnInfo;

public class DateAndCount {
    @ColumnInfo(name = "date")
    private Long date;

    @ColumnInfo(name = "count")
    private Integer count;

    public DateAndCount(Long date, Integer count) {
        this.date = date;
        this.count = count;
    }

    public Long getDate() {
        return date;
    }

    public Integer getCount() {
        return count;
    }
}
