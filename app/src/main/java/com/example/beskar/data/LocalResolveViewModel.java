package com.example.beskar.data;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class LocalResolveViewModel extends AndroidViewModel {

    private LocalResolveRepository mRepository;
    private final LiveData<List<LocalResolve>> mAllLocalResolvesWithNullRes;
    private final LiveData<List<LocalResolve>> mAllLocalResolvesFrom1dAgoWithNullRes;
    private final LiveData<List<DateAndCount>> mDateAndCountFrom7dAgoWithNullRes;

    public LocalResolveViewModel(Application application) {
        super(application);
        mRepository = new LocalResolveRepository(application);
        mAllLocalResolvesWithNullRes = mRepository.getAllWithNullRes();
        mAllLocalResolvesFrom1dAgoWithNullRes = mRepository.getAllFrom1dAgoWithNullRes();
        mDateAndCountFrom7dAgoWithNullRes = mRepository.getDateAndCountFrom7dAgoWithNullRes();
    }

    public LiveData<List<LocalResolve>> getAllLocalResolvesWithNullRes() {
        return mAllLocalResolvesWithNullRes;
    }

    public LiveData<List<LocalResolve>> getAllLocalResolvesFrom1dAgoWithNullRes() {
        return mAllLocalResolvesFrom1dAgoWithNullRes;
    }

    public LiveData<List<DateAndCount>> getDateAndCountFrom7dAgoWithNullRes() {
        return mDateAndCountFrom7dAgoWithNullRes;
    }

    public void insert(LocalResolve localResolve) { mRepository.insert(localResolve); }
}
