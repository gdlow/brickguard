package com.example.beskar.data;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;


public class InteractionsViewModel extends AndroidViewModel {
    private InteractionsRepository mRepository;
    private final LiveData<Count> mCountFrom7dAgoWithConfigChanges;
    private final LiveData<Count> mCountFrom7dAgoWithSwitchedOff;

    public InteractionsViewModel(Application application) {
        super(application);
        mRepository = new InteractionsRepository(application);
        mCountFrom7dAgoWithConfigChanges =
                mRepository.getCountFrom7dAgoWithConfigChanges();
        mCountFrom7dAgoWithSwitchedOff =
                mRepository.getCountFrom7dAgoWithSwitchedOff();
    }

    public LiveData<Count> getCountFrom7dAgoWithConfigChanges() {
        return mCountFrom7dAgoWithConfigChanges;
    }

    public LiveData<Count> getCountFrom7dAgoWithSwitchedOff() {
        return mCountFrom7dAgoWithSwitchedOff;
    }

    public void insert(Interactions interaction) { mRepository.insert(interaction); }
}
