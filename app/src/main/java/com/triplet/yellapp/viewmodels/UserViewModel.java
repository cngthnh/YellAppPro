package com.triplet.yellapp.viewmodels;

import static android.content.Context.MODE_PRIVATE;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.triplet.yellapp.R;
import com.triplet.yellapp.models.BudgetCard;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.Notification;
import com.triplet.yellapp.models.UserAccountFull;
import com.triplet.yellapp.repository.DashboardRepository;
import com.triplet.yellapp.repository.YellUserRepository;
import com.triplet.yellapp.utils.GlobalStatus;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class UserViewModel extends AndroidViewModel {
    private final int TRUE_UUID_LEN = 36;
    private final int TEMP_UUID_LEN = 40;
    private final int DELETED_UUID_LEN = 43;
    private YellUserRepository repository;
    private DashboardRepository dashboardRepository;
    private LiveData<UserAccountFull> yellUserLiveData;
    private LiveData<List<Notification>> listNotificationLivaData;
    private LiveData<Notification> notificationLiveData;
    private GlobalStatus globalStatus = GlobalStatus.getInstance();
    private SharedPreferences sharedPreferences;
    Application application;
    Realm realm;

    public LiveData<UserAccountFull> getYellUserLiveData() {
        return yellUserLiveData;
    }

    public UserViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    public void init() {
        repository = new YellUserRepository(getApplication());
        dashboardRepository = new DashboardRepository(getApplication());
        yellUserLiveData = repository.getYellUserLiveData();
        listNotificationLivaData = repository.getListNotificationLiveData();
        notificationLiveData = repository.getNotificationMutableLiveData();
        realm = Realm.getDefaultInstance();
        sharedPreferences = application.getSharedPreferences(application.getResources().getString(R.string.yell_sp), MODE_PRIVATE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean getUser() {
        return repository.getUser();
    }

    public void addDashboard(DashboardCard dashboardCard) {
        if (globalStatus.isOfflineMode())
            repository.addDashboardToLocalDb(dashboardCard);
        else
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

    private void syncDashboards() {
        List<DashboardCard> dashboards = realm.copyFromRealm(realm.where(DashboardCard.class).isNotNull("local_edited_at").findAll());
        for (DashboardCard dashboard : dashboards) {
            switch (dashboard.getDashboard_id().length())
            {
                case TEMP_UUID_LEN:
                    repository.addDashboardToServer(dashboard);
                    break;
                case DELETED_UUID_LEN:
                    dashboardRepository.syncDeletedDashboardWithServer(dashboard);
                    break;
                case TRUE_UUID_LEN:
                    dashboardRepository.editDashboardOnServer(dashboard);
                    break;
            }
        }
    }

    public void sync() {
        syncDashboards();
        globalStatus.setEditedOffline(false);
        sharedPreferences.edit().putString(application.getResources().getString(R.string.edited_offline),
                application.getResources().getString(R.string.bool_no)).apply();
    }
}
