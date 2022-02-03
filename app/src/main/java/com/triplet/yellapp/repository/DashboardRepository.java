package com.triplet.yellapp.repository;

import static android.content.Context.MODE_PRIVATE;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

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
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmList;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardRepository {
    private final int TRUE_UUID_LEN = 36;
    private final int TEMP_UUID_LEN = 40;
    private final int DELETED_UUID_LEN = 43;
    GlobalStatus globalStatus = GlobalStatus.getInstance();
    SessionManager sessionManager;
    ApiService service;
    SharedPreferences sharedPreferences;
    Application application;
    Moshi moshi;
    DateFormat df;
    MutableLiveData<DashboardCard> dashboardCardMutableLiveData;
    private Realm realm;

    public DashboardRepository(Application application) {
        this.application = application;
        sharedPreferences = application.getSharedPreferences(application.getResources().getString(R.string.yell_sp), MODE_PRIVATE);
        sessionManager = SessionManager.getInstance(sharedPreferences);
        service = Client.createService(ApiService.class);
        dashboardCardMutableLiveData = new MutableLiveData<>();
        moshi = new Moshi.Builder()
                .add(new RealmListJsonAdapterFactory())
                .build();
        realm = Realm.getDefaultInstance();
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public MutableLiveData<DashboardCard> getDashboardCardMutableLiveData() {
        return dashboardCardMutableLiveData;
    }

    public void getDashboardFromServer(String dashboardId) {
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

            GlobalStatus globalStatus = GlobalStatus.getInstance();
            if (diff > 5 && !globalStatus.isOfflineMode()) {
                getDashboardFromServer(dashboardId);
                return false;
            }

            dashboardCardMutableLiveData.postValue(realm.copyFromRealm(object));
            return true;
        }
    }

    public void editDashboardOnServer(DashboardCard dashboardCard) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<InfoMessage> call;
        RequestBody requestBody = dashboardToJson(dashboardCard);
        call = service.editDashboard(requestBody);
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                Log.w("YellEditDashboard", "onResponse: " + response);
                if (response.isSuccessful()) {

                }
            }

            @Override
            public void onFailure(Call<InfoMessage> call, Throwable t) {
                Log.w("YellEditDashboard", "onFailure: " + t.getMessage() );
            }
        });
    }

    private void editDashboardInLocalDb(DashboardCard dashboardCard) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(dashboardCard);
            }
        });
    }

    public void editDashboard(DashboardCard dashboardCard) {
        if (!globalStatus.isOfflineMode()) {
            editDashboardOnServer(dashboardCard);
            dashboardCard.local_edited_at = null;
        }
        else {
            dashboardCard.local_edited_at = df.format(new Date());
            globalStatus.setEditedOffline(true);
            sharedPreferences.edit().putString(application.getResources().getString(R.string.edited_offline),
                    application.getResources().getString(R.string.bool_yes)).apply();
        }
        editDashboardInLocalDb(dashboardCard);
    }

    private void deleteDashboardFromServer(DashboardCard dashboardCard) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<InfoMessage> call;
        RequestBody requestBody = dashboardToJson(dashboardCard);
        call = service.deleteDashboard(requestBody);
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                Log.w("Dashboard: ", "DeleteCommandResponse");
                if (response.isSuccessful()) {
                    Log.w("Dashboard: ", "Deleted Successfully");
                }
            }

            @Override
            public void onFailure(Call<InfoMessage> call, Throwable t) {
                Log.w("YellDeleteDashboard", "onFailure: " + t.getMessage() );
            }
        });
    }

    public void syncDeletedDashboardWithServer(DashboardCard dashboardCard) {
        if (dashboardCard.getDashboard_id().length() == DELETED_UUID_LEN)
            dashboardCard.dashboard_id = dashboardCard.getDashboard_id().replace("DELETED", "");
        deleteDashboardFromServer(dashboardCard);
    }

    private void deleteDashboardInLocalDb(DashboardCard dashboardCard) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                DashboardCard object = realm.where(DashboardCard.class).equalTo("dashboard_id", dashboardCard.getDashboard_id()).findFirst();
                if (object == null)
                    return;
                object.deleteFromRealm();
                Log.w("DashboardOnRealm: ", "Deleted " + dashboardCard.getName());
            }
        });
    }

    public void deleteDashboard(DashboardCard dashboardCard) {
        if (!globalStatus.isOfflineMode()) {
            deleteDashboardInLocalDb(dashboardCard);
            syncDeletedDashboardWithServer(dashboardCard);
        } else {
            if (dashboardCard.getDashboard_id().length() == TRUE_UUID_LEN) {
                dashboardCard.dashboard_id = "DELETED" + dashboardCard.getDashboard_id();
                globalStatus.setEditedOffline(true);
                sharedPreferences.edit().putString(application.getResources().getString(R.string.edited_offline),
                        application.getResources().getString(R.string.bool_yes)).apply();
            } else if (dashboardCard.getDashboard_id().length() == TEMP_UUID_LEN) {
                deleteDashboardInLocalDb(dashboardCard);
            }
        }
    }

    public void inviteToDashboardOnServer(DashboardPermission dbPermission) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<InfoMessage> call;
        RequestBody requestBody = dashboardPermissionToJson(dbPermission);
        call = service.inviteSoToDashboard(requestBody);
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                Log.w("YellInviteSoToDashboard", "onResponse: " + response);
                if(!response.isSuccessful())
                {
                    if (response.code() == 404) {
                        Toast.makeText(application.getApplicationContext(), "User id này không tồn tại", Toast.LENGTH_LONG).show();
                    }
                    else {
                    }
                }
                else {
                    Toast.makeText(application.getApplicationContext(), "Đã mời thành công", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<InfoMessage> call, Throwable t) {
                Log.w("YellInviteSoToDashboard", "onFailure: " + t.getMessage() );
            }
        });
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
                    DashboardCard dashboardCard = dashboardCardMutableLiveData.getValue();
                    dashboardCard.addTask(yellTask);
                    dashboardCardMutableLiveData.postValue(dashboardCard);
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealmOrUpdate(dashboardCard);
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

    public void deleteTaskOnServer(YellTask yellTask) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<InfoMessage> call;
        RequestBody requestBody = taskToJson(yellTask);
        call = service.deleteTask(requestBody);
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                Log.w("YellTaskDeleted", "onResponse: " + response.body());
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
            }
        });
        deleteTaskOnServer(yellTask);
    }

    private RequestBody dashboardToJson(DashboardCard dashboardCard) {
        String json = moshi.adapter(DashboardCard.class).toJson(dashboardCard);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), json);
        return requestBody;
    }

    private RequestBody dashboardPermissionToJson(DashboardPermission dashboardPermission) {
        String json = moshi.adapter(DashboardPermission.class).toJson(dashboardPermission);
        return RequestBody.create(MediaType.parse("text/plain"), json);
    }

    private RequestBody taskToJson(YellTask currentYellTask) {
        String jsonYellTask = moshi.adapter(YellTask.class).toJson(currentYellTask);
        return RequestBody.create(MediaType.parse("text/plain"), jsonYellTask);
    }

}
