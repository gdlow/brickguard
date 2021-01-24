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

    LocalResolveRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mLocalResolveDao = db.localResolveDao();
        mAllLocalResolves = mLocalResolveDao.getAll();
        mAllLocalResolvesWithNullRes = mLocalResolveDao.getAllWithResolution("0.0.0.0");
        mLocalResolvesFrom1dAgo = mLocalResolveDao.getAllFrom1dAgo();
        mLocalResolvesFrom1dAgoWithNullRes = mLocalResolveDao
                .getAllWithResolutionFrom1dAgo("0.0.0.0");
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

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(LocalResolve LocalResolve) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mLocalResolveDao.insertAll(LocalResolve);
        });
    }
}
