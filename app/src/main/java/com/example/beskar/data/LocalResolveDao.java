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
    @Query("SELECT * FROM local_resolves")
    LiveData<List<LocalResolve>> getAll();

    @Query("SELECT * FROM local_resolves WHERE timestamp >= strftime('%s', 'now', '-1 day')")
    LiveData<List<LocalResolve>> getAllFrom1dAgo();

    @Query("SELECT * FROM local_resolves WHERE timestamp >= strftime('%s', 'now', '-7 day')")
    LiveData<List<LocalResolve>> getAllFrom7dAgo();

    @Query("SELECT * FROM local_resolves WHERE resolution = :resolution")
    LiveData<List<LocalResolve>> getAllWithResolution(String resolution);

    // Deduplicate on timestamp, resolution on db read
    @Query("SELECT count(resolution) as count FROM (SELECT DISTINCT timestamp, resolution FROM " +
            "local_resolves WHERE resolution = :resolution " +
            "AND timestamp >= strftime('%s', 'now', '-7 day'))")
    LiveData<Count> getAllCountWithResolutionFrom7dAgo(String resolution);

    @Query("SELECT * FROM local_resolves WHERE resolution = :resolution AND timestamp >= strftime" +
            "('%s', 'now', '-1 day')")
    LiveData<List<LocalResolve>> getAllWithResolutionFrom1dAgo(String resolution);

    @Query("SELECT * FROM local_resolves WHERE resolution = :resolution AND timestamp >= strftime" +
            "('%s', 'now', '-7 day')")
    LiveData<List<LocalResolve>> getAllWithResolutionFrom7dAgo(String resolution);

    // Deduplicate on timestamp, resolution on db read
    @Query("SELECT date(timestamp, 'unixepoch') as date, count(resolution) as count FROM " +
            "(SELECT DISTINCT timestamp, resolution FROM local_resolves WHERE resolution = " +
            ":resolution AND timestamp >= strftime('%s', 'now'," +
            " '-7 day')) GROUP BY date")
    LiveData<List<DateAndCount>> getDateAndCountWithResolutionFrom7dAgo(String resolution);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(LocalResolve localResolve);

    @Delete
    void delete(LocalResolve resolve);
}