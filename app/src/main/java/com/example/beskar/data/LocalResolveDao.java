package com.example.beskar.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LocalResolveDao {

    String DISTINCT_7D_TABLE = "(SELECT * FROM " +
            "local_resolves WHERE resolution = :resolution " +
            "AND timestamp >= strftime('%s', 'now', '-6 day') " +
            "GROUP BY timestamp)";

    @Query("SELECT datetime(timestamp, 'unixepoch') as datetime, domain, resolution FROM " + DISTINCT_7D_TABLE)
    LiveData<List<DateTimeLocalResolve>> getAllDateTimeLocalResolveWithResolutionFrom7dAgo(String resolution);

    @Query("SELECT count(resolution) as count FROM " + DISTINCT_7D_TABLE)
    LiveData<Count> getAllCountWithResolutionFrom7dAgo(String resolution);

    @Query("SELECT date(timestamp, 'unixepoch') as date, count(resolution) as count FROM " +
            DISTINCT_7D_TABLE + " GROUP BY date")
    LiveData<List<DateAndCount>> getDateAndCountWithResolutionFrom7dAgo(String resolution);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(LocalResolve localResolve);

    @Delete
    void delete(LocalResolve resolve);
}