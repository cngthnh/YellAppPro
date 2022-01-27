package com.triplet.yellapp.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.repository.YellUserRepository;

import java.util.List;

public class UserViewModel extends AndroidViewModel {
    private YellUserRepository repository;

    public LiveData<List<DashboardCard>> getDashboardsLiveData() {
        return dashboardsLiveData;
    }

    private LiveData<List<DashboardCard>> dashboardsLiveData;

    public UserViewModel(@NonNull Application application) {
        super(application);
    }
    public void init() {
        repository = new YellUserRepository(getApplication());
        dashboardsLiveData = repository.getDashboardsLiveData();
    }
    public void getUser() {
        repository.getUserFromServer();
    }
}
