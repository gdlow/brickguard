package com.example.beskar.data;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class InteractionsViewModel extends AndroidViewModel {
    private InteractionsRepository mRepository;
    private final LiveData<List<DateAndCount>> mDateAndCountFrom7dAgoWithConfigChanges;
    private final LiveData<List<DateAndCount>> mDateAndCountFrom7dAgoWithSwitchedOff;

    public InteractionsViewModel(Application application) {
        super(application);
        mRepository = new InteractionsRepository(application);
        mDateAndCountFrom7dAgoWithConfigChanges =
                mRepository.getDateAndCountFrom7dAgoWithConfigChanges();
        mDateAndCountFrom7dAgoWithSwitchedOff =
                mRepository.getDateAndCountFrom7dAgoWithSwitchedOff();
    }

    public LiveData<List<DateAndCount>> getDateAndCountFrom7dAgoWithConfigChanges() {
        return mDateAndCountFrom7dAgoWithConfigChanges;
    }

    public LiveData<List<DateAndCount>> getDateAndCountFrom7dAgoSwitchedOff() {
        return mDateAndCountFrom7dAgoWithSwitchedOff;
    }

    public void insert(Interactions interaction) { mRepository.insert(interaction); }
}
