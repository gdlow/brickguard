package com.example.beskar.data;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class LocalResolveViewModel extends AndroidViewModel {

    private LocalResolveRepository mRepository;
    private final LiveData<List<LocalResolve>> mAllLocalResolvesWithNullRes;
    private final LiveData<List<LocalResolve>> mAllLocalResolvesFrom1dAgoWithNullRes;

    public LocalResolveViewModel(Application application) {
        super(application);
        mRepository = new LocalResolveRepository(application);
        mAllLocalResolvesWithNullRes = mRepository.getAllWithNullRes();
        mAllLocalResolvesFrom1dAgoWithNullRes = mRepository.getAllFrom1dAgoWithNullRes();
    }

    LiveData<List<LocalResolve>> getAllLocalResolvesWithNullRes() {
        return mAllLocalResolvesWithNullRes;
    }

    LiveData<List<LocalResolve>> getAllLocalResolvesFrom1dAgoWithNullRes() {
        return mAllLocalResolvesFrom1dAgoWithNullRes;
    }

    public void insert(LocalResolve localResolve) { mRepository.insert(localResolve); }
}
