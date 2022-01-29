package com.triplet.yellapp.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.triplet.yellapp.models.YellTask;
import com.triplet.yellapp.repository.YellTaskRepository;

import io.realm.Realm;

public class YellTaskViewModel extends AndroidViewModel {
    private YellTaskRepository yellTaskRepository;
    private LiveData<YellTask> yellTaskLiveData;
    private  LiveData<String> taskId;

    public YellTaskViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        yellTaskRepository = new YellTaskRepository(getApplication());
        yellTaskLiveData = yellTaskRepository.getYellTaskResponseLiveData();
        taskId = yellTaskRepository.getTaskIdLiveData();
    }

    public boolean getTask(String taskId) {
        return yellTaskRepository.getTask(taskId);
    }

    public void addTask(YellTask yellTask) {
       yellTaskRepository.addTaskToServer(yellTask);
    }

    public void editTask(YellTask yellTask) {
        yellTaskRepository.patchTask(yellTask);
    }

    public void deleteTask(YellTask yellTask) {yellTaskRepository.deleteTaskToServer(yellTask);}

    public LiveData<YellTask> getYellTaskLiveData() {
        return yellTaskLiveData;
    }

    public  LiveData<String> getTaskIdLiveData() {return taskId;}
}
