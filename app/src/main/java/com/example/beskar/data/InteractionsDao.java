package com.example.beskar.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface InteractionsDao {
    @Query("SELECT * FROM interactions")
    LiveData<List<Interactions>> getAll();

    @Query("SELECT * FROM interactions WHERE timestamp >= strftime('%s', 'now', '-7 day')")
    LiveData<List<Interactions>> getAllFrom7dAgo();

    // Deduplicate on timestamp, interaction on db read
    @Query("SELECT count(interaction) as count FROM (SELECT DISTINCT timestamp, interaction FROM " +
            "interactions WHERE interaction = :interaction" +
            " AND timestamp >= strftime('%s', 'now', '-7 day'))")
    LiveData<Count> getCountWithInteractionFrom7dAgo(String interaction);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Interactions interaction);

    @Delete
    void delete(Interactions interaction);
}