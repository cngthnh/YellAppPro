package com.triplet.yellapp.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.triplet.yellapp.models.DashboardCard;

import java.util.List;

public class DashboardViewModelFactory implements ViewModelProvider.Factory {

    List<DashboardCard> mListDashboard;

    public DashboardViewModelFactory(List<DashboardCard> mListDashboard){
        this.mListDashboard = mListDashboard;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        DashboardViewModel dashboardViewModel = new DashboardViewModel(mListDashboard);
        return (T)dashboardViewModel;
    }
}
