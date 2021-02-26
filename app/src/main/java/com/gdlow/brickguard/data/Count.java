package com.gdlow.brickguard.data;

import androidx.room.ColumnInfo;

public class Count {
    @ColumnInfo(name = "count")
    private Integer count;

    public Count(Integer count) {
        this.count = count;
    }

    public Integer getCount() {
        return count;
    }
}
