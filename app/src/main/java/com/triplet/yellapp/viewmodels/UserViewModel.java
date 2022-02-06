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
import androidx.lifecycle.Observer;

import com.triplet.yellapp.R;
import com.triplet.yellapp.models.BudgetCard;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.Notification;
import com.triplet.yellapp.models.TransactionCard;
import com.triplet.yellapp.models.UserAccountFull;
import com.triplet.yellapp.models.YellTask;
import com.triplet.yellapp.repository.BudgetRepository;
import com.triplet.yellapp.repository.DashboardRepository;
import com.triplet.yellapp.repository.YellTaskRepository;
import com.triplet.yellapp.repository.YellUserRepository;
import com.triplet.yellapp.utils.GlobalStatus;

import java.util.List;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class UserViewModel extends AndroidViewModel {
    private final int TRUE_UUID_LEN = 36;
    private final int TEMP_UUID_LEN = 40;
    private final int DELETED_UUID_LEN = 43;
    private YellUserRepository repository;
    private DashboardRepository dashboardRepository;
    private BudgetRepository budgetRepository;
    private YellTaskRepository taskRepository;
    private MutableLiveData<UserAccountFull> yellUserLiveData;
    private LiveData<List<Notification>> listNotificationLivaData;
    private LiveData<Notification> notificationLiveData;
    private LiveData<DashboardCard> syncDashboardCardLiveData;
    private LiveData<YellTask> syncYellTaskLiveData;
    private GlobalStatus globalStatus = GlobalStatus.getInstance();
    private SharedPreferences sharedPreferences;
    Application application;
    String uid;
    Realm realm;

    public MutableLiveData<UserAccountFull> getYellUserLiveData() {
        return yellUserLiveData;
    }
    public LiveData<YellTask> getSyncYellTaskLiveData() {
        return syncYellTaskLiveData;
    }

    public UserViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    public void init() {
        repository = new YellUserRepository(getApplication());
        dashboardRepository = new DashboardRepository(getApplication());
        taskRepository = new YellTaskRepository(getApplication());
        budgetRepository = new BudgetRepository(getApplication());
        yellUserLiveData = repository.getYellUserLiveData();
        listNotificationLivaData = repository.getListNotificationLiveData();
        notificationLiveData = repository.getNotificationMutableLiveData();
        syncDashboardCardLiveData = repository.getSyncDashBoardLiveData();
        syncYellTaskLiveData = dashboardRepository.getSyncYellTaskLiveData();
        realm = Realm.getDefaultInstance();
        sharedPreferences = application.getSharedPreferences(application.getResources().getString(R.string.yell_sp), MODE_PRIVATE);
        uid = sharedPreferences.getString("uid", "");
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
        if (globalStatus.isOfflineMode())
            repository.addBudgetToLocalDb(budgetCard);
        else
            repository.addBudgetToServer(budgetCard);
    }

    public void addNotification(Notification notification, String id){
        repository.addNotificationToLocalDb(notification, id);
    }

    public void getNotification() {
        repository.getNotificationFromServer();
        repository.getNotification();
    }

    public LiveData<List<Notification>> getListNotificationLivaData() {
        return listNotificationLivaData;
    }

    public LiveData<Notification> getNotificationLiveData() {
        return notificationLiveData;
    }
    public LiveData<DashboardCard> getSyncDashboardCardLiveData() {
        return syncDashboardCardLiveData;
    }

    public void acceptNotify(Notification notification) {
        repository.confirmInvited(notification);
    }

    public void readNotify(Notification notification){
        repository.readNotification(notification);
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

    public void syncAddTasks(List<YellTask> tasks) {
        for (YellTask task : tasks) {
            if (task.parent_id == null)
                dashboardRepository.addTaskToServer(task);
        }
    }
    public void syncTasks() {
        List<YellTask> tasks = realm.copyFromRealm(realm.where(YellTask.class).isNotNull("local_edited_at").findAll());
        for (YellTask task : tasks) {
            if (task.getDashboard_id().length() == TRUE_UUID_LEN) {
                switch (task.getTask_id().length())
                {
                    case TEMP_UUID_LEN:
                        if (task.parent_id == null) {
                            dashboardRepository.addTaskToServer(task);
                        }
                        else if (task.parent_id.length() == TRUE_UUID_LEN) {
                            dashboardRepository.addTaskToServer(task, realm.copyFromRealm(realm.where(YellTask.class)
                                    .equalTo("task_id", task.parent_id).findFirst()));
                        }
                        break;
                    case DELETED_UUID_LEN:
                        taskRepository.syncDeletedTaskWithServer(task);
                        break;
                    case TRUE_UUID_LEN:
                        taskRepository.patchTaskToServer(task);
                        break;
                }
            }
        }
    }

    public void syncAddSubTasks(List<YellTask> tasks, YellTask parentTask) {
        for (YellTask task : tasks) {
            dashboardRepository.addTaskToServer(task,parentTask);
        }
    }

    private void syncBudgets() {
        List<BudgetCard> budgets = realm.copyFromRealm(realm.where(BudgetCard.class).isNotNull("local_edited_at").findAll());
        for (BudgetCard budget : budgets) {
            switch (budget.getId().length())
            {
                case TEMP_UUID_LEN:
                    repository.addBudgetToServer(budget);
                    break;
                case DELETED_UUID_LEN:
                    budgetRepository.syncDeletedBudgetWithServer(budget);
                    break;
                case TRUE_UUID_LEN:
                    // không có phương thức chỉnh sửa budget
                    break;
            }
        }
    }

    private void syncTransactions() {
        List<TransactionCard> transactions = realm.copyFromRealm(realm.where(TransactionCard.class).isNotNull("local_edited_at").findAll());
        for (TransactionCard transaction : transactions) {
            switch (transaction.getTran_id().length())
            {
                case TEMP_UUID_LEN:
                    budgetRepository.getBudget(transaction.getBudget_id());
                    budgetRepository.addTransactionToServer(transaction);
                    break;
                case DELETED_UUID_LEN:
                    budgetRepository.syncDeletedTransactionWithServer(transaction);
                    break;
                case TRUE_UUID_LEN:
                    // không có phương thức chỉnh sửa
                    break;
            }
        }
    }

    public MutableLiveData<UserAccountFull> sync() {
        syncTasks();
        syncDashboards();
        syncBudgets();
        syncTransactions();
        globalStatus.setEditedOffline(false);
        sharedPreferences.edit().putString(application.getResources().getString(R.string.edited_offline),
                application.getResources().getString(R.string.bool_no)).apply();
        RealmResults<UserAccountFull> updatedUser = realm.where(UserAccountFull.class).equalTo("uid", uid).limit(1).findAllAsync();
        updatedUser.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<UserAccountFull>>() {
            @Override
            public void onChange(RealmResults<UserAccountFull> userAccountFulls, OrderedCollectionChangeSet changeSet) {
                try {
                    yellUserLiveData.postValue(userAccountFulls.get(0));
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        });
        return yellUserLiveData;
    }
}
