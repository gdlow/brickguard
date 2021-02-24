package com.example.beskar.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class InteractionsRepository {
    private InteractionsDao interactionsDao;
    private LiveData<List<DateTimeInteractions>> mDateTimeInteractionsFrom7dAgoWithConfigChanges;
    private LiveData<List<DateTimeInteractions>> mDateTimeInteractionsFrom7dAgoWithSwitchedOff;
    private LiveData<Count> mCountFrom7dAgoWithConfigChanges;
    private LiveData<Count> mCountFrom7dAgoWithSwitchedOff;

    public InteractionsRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        interactionsDao = db.interactionsDao();
        mDateTimeInteractionsFrom7dAgoWithConfigChanges =
                interactionsDao.getAllDateTimeInteractionWithInteractionFrom7dAgo(Interactions.CONFIG_CHANGE);
        mDateTimeInteractionsFrom7dAgoWithSwitchedOff =
                interactionsDao.getAllDateTimeInteractionWithInteractionFrom7dAgo(Interactions.SWITCHED_OFF);
        mCountFrom7dAgoWithConfigChanges =
                interactionsDao.getCountWithInteractionFrom7dAgo(Interactions.CONFIG_CHANGE);
        mCountFrom7dAgoWithSwitchedOff = interactionsDao.getCountWithInteractionFrom7dAgo(
                Interactions.SWITCHED_OFF);
    }

    LiveData<List<DateTimeInteractions>> getDateTimeInteractionsFrom7dAgoWithConfigChanges() {
        return mDateTimeInteractionsFrom7dAgoWithConfigChanges;
    }

    LiveData<List<DateTimeInteractions>> getDateTimeInteractionsFrom7dAgoWithSwitchedOff() {
        return mDateTimeInteractionsFrom7dAgoWithSwitchedOff;
    }

    LiveData<Count> getCountFrom7dAgoWithConfigChanges() {
        return mCountFrom7dAgoWithConfigChanges;
    }

    LiveData<Count> getCountFrom7dAgoWithSwitchedOff() {
        return mCountFrom7dAgoWithSwitchedOff;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insert(Interactions interaction) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            interactionsDao.insert(interaction);
        });
    }
}
