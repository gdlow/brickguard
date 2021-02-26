package com.gdlow.brickguard.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

public class InteractionsRepository {
    private InteractionsDao interactionsDao;
    private LiveData<Count> mCountFrom7dAgoWithConfigChanges;
    private LiveData<Count> mCountFrom7dAgoWithSwitchedOff;

    public InteractionsRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        interactionsDao = db.interactionsDao();
        mCountFrom7dAgoWithConfigChanges =
                interactionsDao.getCountWithInteractionFrom7dAgo(Interactions.CONFIG_CHANGE);
        mCountFrom7dAgoWithSwitchedOff = interactionsDao.getCountWithInteractionFrom7dAgo(
                Interactions.SWITCHED_OFF);
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
