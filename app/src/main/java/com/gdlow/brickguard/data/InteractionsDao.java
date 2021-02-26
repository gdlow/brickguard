package com.gdlow.brickguard.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface InteractionsDao {

    @Query("SELECT datetime(timestamp, 'unixepoch') as datetime, interaction, description FROM " +
            "interactions WHERE interaction = :interaction AND timestamp >= strftime('%s', 'now'," +
            " '-6 day')")
    List<DateTimeInteractions> getAllWithInteractionFrom7dAgoSynchronous(String interaction);

    @Query("SELECT count(interaction) as count FROM interactions WHERE interaction = :interaction" +
            " AND timestamp >= strftime('%s', 'now', '-6 day')")
    LiveData<Count> getCountWithInteractionFrom7dAgo(String interaction);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Interactions interaction);

    @Delete
    void delete(Interactions interaction);
}