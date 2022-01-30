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
import com.triplet.yellapp.models.ErrorMessage;
import com.triplet.yellapp.models.InfoMessage;
import com.triplet.yellapp.models.YellTask;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.RealmListJsonAdapterFactory;
import com.triplet.yellapp.utils.SessionManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class YellTaskRepository {
    SessionManager sessionManager;
    ApiService service;
    SharedPreferences sharedPreferences;
    Application application;
    Moshi moshi;
    DateFormat df;
    private MutableLiveData<DashboardCard> dashboardCardMutableLiveData;
    private MutableLiveData<YellTask> YellTaskResponseLiveData;
    private MutableLiveData<String> taskId;
    private Realm realm;

    public YellTaskRepository(Application application) {
        this.application = application;
        sharedPreferences = application.getSharedPreferences(application.getResources().getString(R.string.yell_sp), MODE_PRIVATE);
        sessionManager = SessionManager.getInstance(sharedPreferences);
        service = Client.createService(ApiService.class);
        YellTaskResponseLiveData = new MutableLiveData<>();
        dashboardCardMutableLiveData = new MutableLiveData<>();
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        moshi = new Moshi.Builder()
                .add(new RealmListJsonAdapterFactory())
                .build();
        taskId = new MutableLiveData<>();
        realm = Realm.getDefaultInstance();
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
            }
            catch (NullPointerException e) {
                return true;
            }

            if (diff > 5) {
                getTaskFromServer(taskId);
                return false;
            }

            YellTaskResponseLiveData.postValue(realm.copyFromRealm(object));
            return true;
        }
    }

    public MutableLiveData<YellTask> getYellTaskResponseLiveData() {
        return YellTaskResponseLiveData;
    }

    public MutableLiveData<String> getTaskIdLiveData() {
        return taskId;
    }

    public void addTaskToServer(YellTask yellTask) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<YellTask> call;
        RequestBody requestBody = taskToJson(yellTask);
        call = service.addTask(null, requestBody);
        call.enqueue(new Callback<YellTask>() {
            @Override
            public void onResponse(Call<YellTask> call, Response<YellTask> response) {
                Log.w("YellCreateDashboard", "onResponse: " + response);
                if (response.isSuccessful()) {
                    yellTask.setTask_id(response.body().getTask_id());
                    yellTask.last_sync = df.format(new Date());
                    YellTask parentTask = YellTaskResponseLiveData.getValue();
                    while (parentTask == null) {
                        getTaskFromServer(yellTask.getTask_id());
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    parentTask.addSubtask(yellTask);
                    YellTaskResponseLiveData.postValue(parentTask);
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealmOrUpdate(parentTask);
                        }
                    });
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
        DashboardCard object = realm.where(DashboardCard.class).equalTo("id", dashboardId).findFirst();
        if (object == null) {
            getDashboardFromServer(dashboardId);
            return false;
        }
        else {
            long diff = 0;
            try {
                Date dt_sync = df.parse(object.last_sync);
                Date dt_now = df.parse(df.format(new Date()));
                diff = TimeUnit.MINUTES.convert(dt_now.getTime() - dt_sync.getTime(), TimeUnit.MILLISECONDS);
            } catch (ParseException e) {
                e.printStackTrace();
                getDashboardFromServer(dashboardId);
                return false;
            }
            catch (NullPointerException e) {
                return true;
            }

            if (diff > 5) {
                getDashboardFromServer(dashboardId);
                return false;
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

    public void patchTask(YellTask yellTask) {
        YellTaskResponseLiveData.postValue(yellTask);
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(yellTask);
            }
        });
        patchTaskToServer(yellTask);
    }

    private void deleteTaskOnServer(YellTask yellTask) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<InfoMessage> call;
        RequestBody requestBody = taskToJson(yellTask);
        call = service.deleteTask(requestBody);
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                Log.w("YellTaskDeleted", "onResponse: " + response.body().getMessage());
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

    public void deleteYellTask(YellTask yellTask) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                YellTask object = realm.where(YellTask.class).equalTo("task_id",yellTask.getTask_id()).findFirst();
                if (object == null)
                    return;
                object.deleteFromRealm();
                Log.w("YellTaskDeletedOnServer", "Deleted " + yellTask.getName()+" on DB");
            }
        });
        deleteTaskOnServer(yellTask);
    }
}
