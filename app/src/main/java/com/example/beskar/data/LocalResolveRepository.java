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
    private LiveData<Count> mAllLocalResolvesCountFrom7dAgoWithNullRes;
    private LiveData<Count> mAllLocalResolvesCountFrom7dAgoWithOneRes;
    private LiveData<List<DateAndCount>> mDateAndCountFrom7dAgoWithNullRes;
    private LiveData<List<DateAndCount>> mDateAndCountFrom7dAgoWithOneRes;

    LocalResolveRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mLocalResolveDao = db.localResolveDao();
        mAllLocalResolvesCountFrom7dAgoWithNullRes =
                mLocalResolveDao.getAllCountWithResolutionFrom7dAgo("0.0.0.0");
        mAllLocalResolvesCountFrom7dAgoWithOneRes =
                mLocalResolveDao.getAllCountWithResolutionFrom7dAgo("0.0.0.1");
        mDateAndCountFrom7dAgoWithNullRes = mLocalResolveDao
                .getDateAndCountWithResolutionFrom7dAgo("0.0.0.0");
        mDateAndCountFrom7dAgoWithOneRes = mLocalResolveDao
                .getDateAndCountWithResolutionFrom7dAgo("0.0.0.1");
    }

    LiveData<Count> getAllLocalResolvesCountFrom7dAgoWithNullRes() {
        return mAllLocalResolvesCountFrom7dAgoWithNullRes;
    }

    LiveData<Count> getAllLocalResolvesCountFrom7dAgoWithOneRes() {
        return mAllLocalResolvesCountFrom7dAgoWithOneRes;
    }

    LiveData<List<DateAndCount>> getDateAndCountFrom7dAgoWithNullRes() {
        return mDateAndCountFrom7dAgoWithNullRes;
    }

    LiveData<List<DateAndCount>> getDateAndCountFrom7dAgoWithOneRes() {
        return mDateAndCountFrom7dAgoWithOneRes;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(LocalResolve localResolve) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mLocalResolveDao.insert(localResolve);
        });
    }
}
