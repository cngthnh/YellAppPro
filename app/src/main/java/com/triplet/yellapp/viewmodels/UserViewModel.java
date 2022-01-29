package com.triplet.yellapp.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.triplet.yellapp.models.BudgetCard;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.UserAccountFull;
import com.triplet.yellapp.repository.YellUserRepository;

import java.util.List;

public class UserViewModel extends AndroidViewModel {
    private YellUserRepository repository;
    private LiveData<UserAccountFull> yellUserLiveData;

    public LiveData<UserAccountFull> getYellUserLiveData() {
        return yellUserLiveData;
    }

    public UserViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        repository = new YellUserRepository(getApplication());
        yellUserLiveData = repository.getYellUserLiveData();
    }

    public boolean getUser() {
        return repository.getUser();
    }

    public void addDashboard(DashboardCard dashboardCard) {
        repository.addDashboardToServer(dashboardCard);
    }

    public void addBudget(BudgetCard budgetCard) {
        repository.addBudgetToServer(budgetCard);
    }
}
