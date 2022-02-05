package com.triplet.yellapp.repository;

import static android.content.Context.MODE_PRIVATE;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.squareup.moshi.Moshi;
import com.triplet.yellapp.R;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.DashboardPermission;
import com.triplet.yellapp.models.ErrorMessage;
import com.triplet.yellapp.models.InfoMessage;
import com.triplet.yellapp.models.YellTask;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.GlobalStatus;
import com.triplet.yellapp.utils.RealmListJsonAdapterFactory;
import com.triplet.yellapp.utils.SessionManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class YellTaskRepository {
    private final int TRUE_UUID_LEN = 36;
    private final int TEMP_UUID_LEN = 40;
    private final int DELETED_UUID_LEN = 43;
    SessionManager sessionManager;
    ApiService service;
    SharedPreferences sharedPreferences;
    Application application;
    Moshi moshi;
    DateFormat df;
    private MutableLiveData<DashboardCard> dashboardCardMutableLiveData;
    private MutableLiveData<YellTask> YellTaskResponseLiveData;
    private MutableLiveData<YellTask> addYellTaskMutableLiveData;
    private Realm realm;
    private GlobalStatus globalStatus = GlobalStatus.getInstance();

    public YellTaskRepository(Application application) {
        this.application = application;
        sharedPreferences = application.getSharedPreferences(application.getResources().getString(R.string.yell_sp), MODE_PRIVATE);
        sessionManager = SessionManager.getInstance(sharedPreferences);
        service = Client.createService(ApiService.class);
        YellTaskResponseLiveData = new MutableLiveData<>();
        dashboardCardMutableLiveData = new MutableLiveData<>();
        addYellTaskMutableLiveData = new MutableLiveData<>();
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        moshi = new Moshi.Builder()
                .add(new RealmListJsonAdapterFactory())
                .build();
        realm = Realm.getDefaultInstance();
    }

    public MutableLiveData<YellTask> getAddYellTaskMutableLiveData() {
        return addYellTaskMutableLiveData;
    }

    public void getTaskFromServer(String taskId) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<YellTask> call;
        call = service.getTask(taskId);
        call.enqueue(new Callback<YellTask>() {
            @Override
            public void onResponse(Call<YellTask> call, Response<YellTask> response) {
                if (response.isSuccessful()) {
                    YellTask yellTask = response.body();
                    yellTask.last_sync = df.format(new Date());
                    YellTaskResponseLiveData.postValue(yellTask);
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealmOrUpdate(yellTask);
                        }
                    });
                } else {
                    ErrorMessage apiError = ErrorMessage.convertErrors(response.errorBody());
                    Toast.makeText(application.getApplicationContext(), apiError.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<YellTask> call, Throwable t) {
                YellTaskResponseLiveData.postValue(null);
                Log.w("YellTaskFragment", "onFailure: " + t.getMessage() );
            }
        });
    }

    public boolean getTask(String taskId) {
        YellTask object = realm.where(YellTask.class).equalTo("task_id", taskId).findFirst();
        if (object == null) {
            getTaskFromServer(taskId);
            return false;
        }
        else {
            if (!globalStatus.isOfflineMode()) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                long diff = 0;
                try {
                    Date dt_sync = df.parse(object.last_sync);
                    Date dt_now = df.parse(df.format(new Date()));
                    diff = TimeUnit.MINUTES.convert(dt_now.getTime() - dt_sync.getTime(), TimeUnit.MILLISECONDS);
                } catch (ParseException e) {
                    e.printStackTrace();
                    getTaskFromServer(taskId);
                    return false;
                } catch (NullPointerException e) {
                    return true;
                }

                if (diff > 5) {
                    getTaskFromServer(taskId);
                    return false;
                }
            }
            YellTask result = realm.copyFromRealm(object);

            // những subtask nào đã được đánh dấu xoá thì không hiển thị
            for (YellTask subtask : result.subtasks) {
                if (subtask.getTask_id().length() == DELETED_UUID_LEN) result.removeSubtask(subtask);
            }

            YellTaskResponseLiveData.postValue(result);
            return true;
        }
    }

    public MutableLiveData<YellTask> getYellTaskResponseLiveData() {
        return YellTaskResponseLiveData;
    }

    public void addTaskToLocalDb(YellTask task, YellTask parentTask) {
        task.setTask_id("TEMP" + UUID.randomUUID().toString());
        task.local_edited_at = df.format(new Date());
        task.dashboard_id = parentTask.getDashboard_id();
        task.parent_id = parentTask.task_id;
        parentTask.addSubtask(task);
        addYellTaskMutableLiveData.postValue(parentTask);
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(parentTask);
            }
        });

        sharedPreferences.edit().putString(application.getResources().getString(R.string.edited_offline),
                application.getResources().getString(R.string.bool_yes)).apply();
        GlobalStatus globalStatus = GlobalStatus.getInstance();
        globalStatus.setEditedOffline(true);
    }

    public void addTask(YellTask yellTask, YellTask parentTask) {
        if (globalStatus.isOfflineMode())
            addTaskToLocalDb(yellTask, parentTask);
        else
            addTaskToServer(yellTask, parentTask);
    }

    public void addTaskToServer(YellTask yellTask, YellTask parentTask) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<YellTask> call;
        RequestBody requestBody = taskToJson(yellTask);
        call = service.addTask(null, requestBody);
        call.enqueue(new Callback<YellTask>() {
            @Override
            public void onResponse(Call<YellTask> call, Response<YellTask> response) {
                Log.w("YellCreateDashboard", "onResponse: " + response);
                if (response.isSuccessful()) {
                    YellTask needToDelete = null;
                    if (yellTask.task_id != null) {
                        needToDelete = realm.where(YellTask.class).equalTo("task_id", yellTask.task_id).findFirst();
                        parentTask.removeSubtask(yellTask);
                    }
                    yellTask.setTask_id(response.body().getTask_id());
                    yellTask.last_sync = df.format(new Date());
                    yellTask.parent_id = parentTask.task_id;
                    parentTask.addSubtask(yellTask);
                    addYellTaskMutableLiveData.postValue(parentTask);
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealmOrUpdate(parentTask);
                        }
                    });
                    if (needToDelete != null)
                        needToDelete.deleteFromRealm();
                } else {
                    if (response.code() == 401) {
                        ErrorMessage apiError = ErrorMessage.convertErrors(response.errorBody());
                        Toast.makeText(application.getApplicationContext(), apiError.getMessage(), Toast.LENGTH_LONG).show();
                    }

                }
            }
            @Override
            public void onFailure(Call<YellTask> call, Throwable t) {
                Toast.makeText(application.getApplicationContext(), "Lỗi khi kết nối với server", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getDashboardFromServer(String dashboardId) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<DashboardCard> call;
        call = service.getDashboard(dashboardId,"full");
        call.enqueue(new Callback<DashboardCard>() {
            @Override
            public void onResponse(Call<DashboardCard> call, Response<DashboardCard> response) {
                if (response.isSuccessful()) {
                    DashboardCard dashboard = response.body();
                    dashboard.last_sync = df.format(new Date());
                    dashboardCardMutableLiveData.postValue(dashboard);
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmList<DashboardPermission> users = new RealmList<>();
                            DashboardPermission user;
                            for (int i = 0;i<dashboard.getUsers().size();i++) {
                                user = dashboard.getUsers().get(i);
                                user.setId_uid(dashboard.getDashboard_id());
                                users.add(user);
                            }
                            dashboard.setUsers(users);
                            realm.copyToRealmOrUpdate(dashboard);
                        }
                    });
                } else {
                    ErrorMessage apiError = ErrorMessage.convertErrors(response.errorBody());
                    Toast.makeText(application.getApplicationContext(), apiError.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<DashboardCard> call, Throwable t) {
                dashboardCardMutableLiveData.postValue(null);
                Log.w("DashboardCardFragment", "onFailure: " + t.getMessage() );
            }
        });
    }

    public boolean getDashboard(String dashboardId) {
        DashboardCard object = realm.where(DashboardCard.class).equalTo("dashboard_id", dashboardId).findFirst();
        if (object == null) {
            getDashboardFromServer(dashboardId);
            return false;
        }
        else {
            if (!globalStatus.isOfflineMode()) {
                long diff = 0;
                try {
                    Date dt_sync = df.parse(object.last_sync);
                    Date dt_now = df.parse(df.format(new Date()));
                    diff = TimeUnit.MINUTES.convert(dt_now.getTime() - dt_sync.getTime(), TimeUnit.MILLISECONDS);
                } catch (ParseException e) {
                    e.printStackTrace();
                    getDashboardFromServer(dashboardId);
                    return false;
                } catch (NullPointerException e) {
                    return true;
                }

                if (diff > 5) {
                    getDashboardFromServer(dashboardId);
                    return false;
                }
            }
            DashboardCard result = realm.copyFromRealm(object);

            // những task nào đã được đánh dấu xoá thì không hiển thị
            for (YellTask task : result.tasks) {
                if (task.getTask_id().length() == DELETED_UUID_LEN) result.removeTask(task);
            }

            dashboardCardMutableLiveData.postValue(realm.copyFromRealm(object));
            return true;
        }
    }

    public MutableLiveData<DashboardCard> getDashboardCardMutableLiveData() {
        return dashboardCardMutableLiveData;
    }

    private RequestBody taskToJson(YellTask currentYellTask) {
        String jsonYellTask = moshi.adapter(YellTask.class).toJson(currentYellTask);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), jsonYellTask);
        return requestBody;
    }

    public void patchTaskToServer(YellTask yellTask) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<InfoMessage> call;
        yellTask.local_edited_at = null;
        RequestBody requestBody = taskToJson(yellTask);
        call = service.editTask(null,requestBody);
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                Log.w("YellTaskCreate", "onResponse: " + response.body());
                if (response.isSuccessful()) {
                }
                else {
                    if (response.code() == 401) {
                        ErrorMessage apiError = ErrorMessage.convertErrors(response.errorBody());
                        Toast.makeText(application.getApplicationContext(), apiError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    // TODO
                }

            }

            @Override
            public void onFailure(Call<InfoMessage> call, Throwable t) {
                Toast.makeText(application.getApplicationContext(), "Lỗi khi kết nối với server", Toast.LENGTH_LONG).show();
                // TODO:
            }
        });
    }

    private void patchTaskInLocalDb(YellTask yellTask) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(yellTask);
            }
        });
    }

    public void patchTask(YellTask yellTask) {
        if (!globalStatus.isOfflineMode()) {
            patchTaskToServer(yellTask);
            yellTask.local_edited_at = null;
        }
        else {
            yellTask.local_edited_at = df.format(new Date());
            globalStatus.setEditedOffline(true);
            sharedPreferences.edit().putString(application.getResources().getString(R.string.edited_offline),
                    application.getResources().getString(R.string.bool_yes)).apply();
        }
        patchTaskInLocalDb(yellTask);
        YellTaskResponseLiveData.postValue(yellTask);
    }

    private void deleteTaskOnServer(YellTask yellTask) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<InfoMessage> call;
        RequestBody requestBody = taskToJson(yellTask);
        call = service.deleteTask(requestBody);
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                if (response.isSuccessful()) {
                    Log.w("YellTaskDeleted", "onResponse: " + response.body().getMessage());
                }
                else {
                    if (response.code() == 401) {
                        ErrorMessage apiError = ErrorMessage.convertErrors(response.errorBody());
                        Toast.makeText(application.getApplicationContext(), apiError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    Log.w("YellTaskDeleted", "Delete Failed " + String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(Call<InfoMessage> call, Throwable t) {
                Toast.makeText(application.getApplicationContext(), "Lỗi khi kết nối với server", Toast.LENGTH_LONG).show();
                // TODO:
            }
        });
    }

    public void syncDeletedTaskWithServer(YellTask task) {
        deleteTaskInLocalDb(new YellTask(task.getTask_id()));
        if (task.getTask_id().length() == DELETED_UUID_LEN)
            task.task_id = task.getTask_id().replace("DELETED", "");
        deleteTaskOnServer(task);
    }

    private void deleteTaskInLocalDb(YellTask task) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                YellTask object = realm.where(YellTask.class).equalTo("task_id", task.getTask_id()).findFirst();
                if (object == null)
                    return;
                object.deleteFromRealm();
            }
        });
    }

    public void deleteYellTask(YellTask yellTask) {
        if (!globalStatus.isOfflineMode()) {
            syncDeletedTaskWithServer(yellTask);
        } else {
            if (yellTask.getTask_id().length() == TRUE_UUID_LEN) {
                deleteTaskInLocalDb(yellTask);
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        yellTask.task_id = "DELETED" + yellTask.getTask_id();
                        yellTask.local_edited_at = df.format(new Date());
                        realm.copyToRealmOrUpdate(yellTask);
                    }
                });
                globalStatus.setEditedOffline(true);
                sharedPreferences.edit().putString(application.getResources().getString(R.string.edited_offline),
                        application.getResources().getString(R.string.bool_yes)).apply();
            } else if (yellTask.getTask_id().length() == TEMP_UUID_LEN) {
                deleteTaskInLocalDb(yellTask);
            }
        }
    }
}
