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

    @Query("SELECT * FROM local_resolves WHERE resolution = :resolution")
    LiveData<List<LocalResolve>> getAllWithResolution(String resolution);

    @Query("SELECT * FROM local_resolves WHERE resolution = :resolution AND timestamp >= strftime('%s', 'now', '-1 day')")
    LiveData<List<LocalResolve>> getAllWithResolutionFrom1dAgo(String resolution);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(LocalResolve... resolves);

    @Delete
    void delete(LocalResolve resolve);
}