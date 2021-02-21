package com.example.beskar.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class InteractionsRepository {
    private InteractionsDao interactionsDao;
    private LiveData<List<DateAndCount>> mDateAndCountFrom7dAgoWithConfigChanges;
    private LiveData<List<DateAndCount>> mDateAndCountFrom7dAgoWithSwitchedOff;

    public InteractionsRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        interactionsDao = db.interactionsDao();
        mDateAndCountFrom7dAgoWithConfigChanges =
                interactionsDao.getCountWithInteractionFrom7dAgo("config_change");
        mDateAndCountFrom7dAgoWithSwitchedOff = interactionsDao.getCountWithInteractionFrom7dAgo(
                "switched_off");
    }

    LiveData<List<DateAndCount>> getDateAndCountFrom7dAgoWithConfigChanges() {
        return mDateAndCountFrom7dAgoWithConfigChanges;
    }

    LiveData<List<DateAndCount>> getDateAndCountFrom7dAgoWithSwitchedOff() {
        return mDateAndCountFrom7dAgoWithSwitchedOff;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(Interactions interaction) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            interactionsDao.insert(interaction);
        });
    }
}
