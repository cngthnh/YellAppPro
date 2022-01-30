package com.triplet.yellapp.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.YellTask;
import com.triplet.yellapp.repository.YellTaskRepository;

import io.realm.Realm;

public class YellTaskViewModel extends AndroidViewModel {
    private YellTaskRepository yellTaskRepository;
    private LiveData<YellTask> yellTaskLiveData;
    private LiveData<DashboardCard> dashboardCardLiveData;
    private LiveData<YellTask> addYellTaskLiveData;

    public YellTaskViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        yellTaskRepository = new YellTaskRepository(getApplication());
        yellTaskLiveData = yellTaskRepository.getYellTaskResponseLiveData();
        dashboardCardLiveData = yellTaskRepository.getDashboardCardMutableLiveData();
        addYellTaskLiveData = yellTaskRepository.getAddYellTaskMutableLiveData();
    }

    public boolean getTask(String taskId) {
        return yellTaskRepository.getTask(taskId);
    }

    public void addTask(YellTask yellTask, YellTask parentTask) {
       yellTaskRepository.addTaskToServer(yellTask, parentTask);
    }

    public void editTask(YellTask yellTask) {
        yellTaskRepository.patchTask(yellTask);
    }

    public void deleteTask(YellTask yellTask) {yellTaskRepository.deleteYellTask(yellTask);}

    public void getDashboard(String dashboardId) {
        yellTaskRepository.getDashboard(dashboardId);
    }

    public LiveData<YellTask> getYellTaskLiveData() {
        return yellTaskLiveData;
    }

    public LiveData<DashboardCard> getDashboardCardLiveData () {
        return dashboardCardLiveData;
    }

    public LiveData<YellTask> getAddYellTaskLiveData() {
        return addYellTaskLiveData;
    }
}
