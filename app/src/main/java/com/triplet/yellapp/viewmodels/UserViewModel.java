package com.triplet.yellapp.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.triplet.yellapp.models.BudgetCard;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.Notification;
import com.triplet.yellapp.models.UserAccountFull;
import com.triplet.yellapp.repository.YellUserRepository;

import java.util.List;

public class UserViewModel extends AndroidViewModel {
    private YellUserRepository repository;
    private LiveData<UserAccountFull> yellUserLiveData;
    private LiveData<List<Notification>> listNotificationLivaData;
    private LiveData<Notification> notificationLiveData;

    public LiveData<UserAccountFull> getYellUserLiveData() {
        return yellUserLiveData;
    }

    public UserViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        repository = new YellUserRepository(getApplication());
        yellUserLiveData = repository.getYellUserLiveData();
        listNotificationLivaData = repository.getListNotificationLiveData();
        notificationLiveData = repository.getNotificationMutableLiveData();
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

    public void getNotification() {
        repository.getNotificationFromServer();
    }

    public LiveData<List<Notification>> getListNotificationLivaData() {
        return listNotificationLivaData;
    }

    public LiveData<Notification> getNotificationLiveData() {
        return notificationLiveData;
    }

    public void acceptNotify(Notification notification) {
        repository.confirmInvited(notification);
    }

    public void reject(Notification notification) {
        repository.rejectInvited(notification);
    }
}
