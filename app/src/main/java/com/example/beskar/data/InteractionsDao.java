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

    String DISTINCT_7D_TABLE = "(SELECT * FROM " +
            "interactions WHERE interaction = :interaction " +
            "AND timestamp >= strftime('%s', 'now', '-6 day') " +
            "GROUP BY timestamp)";

    @Query("SELECT datetime(timestamp, 'unixepoch') as datetime, interaction, description FROM " +
            DISTINCT_7D_TABLE)
    LiveData<List<DateTimeInteractions>> getAllDateTimeInteractionWithInteractionFrom7dAgo(String interaction);

    @Query("SELECT count(interaction) as count FROM " + DISTINCT_7D_TABLE)
    LiveData<Count> getCountWithInteractionFrom7dAgo(String interaction);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Interactions interaction);

    @Delete
    void delete(Interactions interaction);
}