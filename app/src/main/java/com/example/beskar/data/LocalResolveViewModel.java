package com.example.beskar.data;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class LocalResolveViewModel extends AndroidViewModel {

    private LocalResolveRepository mRepository;
    private final LiveData<Count> mAllLocalResolvesCountFrom7dAgoWithNullRes;
    private final LiveData<Count> mAllLocalResolvesCountFrom7dAgoWithOneRes;
    private final LiveData<List<DateAndCount>> mDateAndCountFrom7dAgoWithNullRes;
    private final LiveData<List<DateAndCount>> mDateAndCountFrom7dAgoWithOneRes;

    public LocalResolveViewModel(Application application) {
        super(application);
        mRepository = new LocalResolveRepository(application);
        mAllLocalResolvesCountFrom7dAgoWithNullRes =
                mRepository.getAllLocalResolvesCountFrom7dAgoWithNullRes();
        mAllLocalResolvesCountFrom7dAgoWithOneRes =
                mRepository.getAllLocalResolvesCountFrom7dAgoWithOneRes();
        mDateAndCountFrom7dAgoWithNullRes = mRepository.getDateAndCountFrom7dAgoWithNullRes();
        mDateAndCountFrom7dAgoWithOneRes = mRepository.getDateAndCountFrom7dAgoWithOneRes();
    }

    public LiveData<Count> getAllLocalResolvesCountFrom7dAgoWithNullRes() {
        return mAllLocalResolvesCountFrom7dAgoWithNullRes;
    }

    public LiveData<Count> getAllLocalResolvesCountFrom7dAgoWithOneRes() {
        return mAllLocalResolvesCountFrom7dAgoWithOneRes;
    }

    public LiveData<List<DateAndCount>> getDateAndCountFrom7dAgoWithNullRes() {
        return mDateAndCountFrom7dAgoWithNullRes;
    }

    public LiveData<List<DateAndCount>> getDateAndCountFrom7dAgoWithOneRes() {
        return mDateAndCountFrom7dAgoWithOneRes;
    }

    public void insert(LocalResolve localResolve) { mRepository.insert(localResolve); }
}
