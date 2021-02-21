package com.example.beskar.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class LocalResolveRepository {
    private LocalResolveDao mLocalResolveDao;
    private LiveData<List<LocalResolve>> mAllLocalResolves;
    private LiveData<List<LocalResolve>> mAllLocalResolvesWithNullRes;
    private LiveData<List<LocalResolve>> mLocalResolvesFrom1dAgo;
    private LiveData<List<LocalResolve>> mLocalResolvesFrom1dAgoWithNullRes;
    private LiveData<List<LocalResolve>> mLocalResolvesFrom7dAgoWithNullRes;
    private LiveData<List<DateAndCount>> mDateAndCountFrom7dAgoWithNullRes;

    LocalResolveRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mLocalResolveDao = db.localResolveDao();
        mAllLocalResolves = mLocalResolveDao.getAll();
        mAllLocalResolvesWithNullRes = mLocalResolveDao.getAllWithResolution("0.0.0.0");
        mLocalResolvesFrom1dAgo = mLocalResolveDao.getAllFrom1dAgo();
        mLocalResolvesFrom1dAgoWithNullRes = mLocalResolveDao
                .getAllWithResolutionFrom1dAgo("0.0.0.0");
        mLocalResolvesFrom7dAgoWithNullRes = mLocalResolveDao
                .getAllWithResolutionFrom7dAgo("0.0.0.0");
        mDateAndCountFrom7dAgoWithNullRes = mLocalResolveDao
                .getCountWithResolutionFrom7dAgo("0.0.0.0");
    }

    LiveData<List<LocalResolve>> getAll() {
        return mAllLocalResolves;
    }

    LiveData<List<LocalResolve>> getAllWithNullRes() {
        return mAllLocalResolvesWithNullRes;
    }

    LiveData<List<LocalResolve>> getAllFrom1dAgo() {
        return mLocalResolvesFrom1dAgo;
    }

    LiveData<List<LocalResolve>> getAllFrom1dAgoWithNullRes() {
        return mLocalResolvesFrom1dAgoWithNullRes;
    }

    LiveData<List<LocalResolve>> getAllFrom7dAgoWithNullRes() {
        return mLocalResolvesFrom7dAgoWithNullRes;
    }

    LiveData<List<DateAndCount>> getDateAndCountFrom7dAgoWithNullRes() {
        return mDateAndCountFrom7dAgoWithNullRes;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(LocalResolve localResolve) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mLocalResolveDao.insert(localResolve);
        });
    }
}
