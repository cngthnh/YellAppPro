package com.triplet.yellapp.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.repository.DashboardRepository;

import java.util.ArrayList;
import java.util.List;

public class DashboardViewModel extends AndroidViewModel {
    DashboardRepository repository;
    LiveData<DashboardCard> dashboardCardLiveData;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        repository = new DashboardRepository(getApplication());
        dashboardCardLiveData = repository.getDashboardCardMutableLiveData();
    }

    public boolean getDashboard(String dashboardId) {
        return repository.getDashboard(dashboardId);
    }

    public void editDashboard(DashboardCard dashboardCard) {
        repository.editDashboard(dashboardCard);
    }

    public void deleteDashboard(DashboardCard dashboardCard) {
        repository.deleteDashboard(dashboardCard);
    }

    public LiveData<DashboardCard> getDashboardCardLiveData() {
        return dashboardCardLiveData;
    }
}
