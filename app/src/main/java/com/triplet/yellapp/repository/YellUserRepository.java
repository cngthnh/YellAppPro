package com.triplet.yellapp.repository;

import static android.content.Context.MODE_PRIVATE;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.squareup.moshi.Moshi;
import com.triplet.yellapp.R;
import com.triplet.yellapp.models.BudgetCard;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.DashboardPermission;
import com.triplet.yellapp.models.ErrorMessage;
import com.triplet.yellapp.models.InfoMessage;
import com.triplet.yellapp.models.Notification;
import com.triplet.yellapp.models.TransactionCard;
import com.triplet.yellapp.models.UserAccountFull;
import com.triplet.yellapp.models.YellTask;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.RealmListJsonAdapterFactory;
import com.triplet.yellapp.utils.SessionManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmList;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class YellUserRepository {

    SessionManager sessionManager;
    ApiService service;
    SharedPreferences sharedPreferences;
    Application application;
    Moshi moshi;
    MutableLiveData<UserAccountFull> yellUserLiveData;
    MutableLiveData<List<Notification>> listNotificationLiveData;
    MutableLiveData<Notification> notificationMutableLiveData;
    DateFormat df;
    String uid;
    private Realm realm;

    public MutableLiveData<UserAccountFull> getYellUserLiveData() {
        return yellUserLiveData;
    }

    public MutableLiveData<Notification> getNotificationMutableLiveData() {
        return notificationMutableLiveData;
    }

    public MutableLiveData<List<Notification>> getListNotificationLiveData() {
        return listNotificationLiveData;
    }

    public YellUserRepository(Application application)
    {
        this.application = application;
        sharedPreferences = application.getSharedPreferences(application.getResources().getString(R.string.yell_sp), MODE_PRIVATE);
        sessionManager = SessionManager.getInstance(sharedPreferences);
        service = Client.createService(ApiService.class);
        yellUserLiveData = new MutableLiveData<>();
        listNotificationLiveData = new MutableLiveData<>();
        notificationMutableLiveData = new MutableLiveData<>();
        uid = sharedPreferences.getString("uid",null);
        moshi = new Moshi.Builder()
                .add(new RealmListJsonAdapterFactory())
                .build();
        realm = Realm.getDefaultInstance();
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public void getUserFromServer() {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<UserAccountFull> call;
        call = service.getUserFull("full");
        call.enqueue(new Callback<UserAccountFull>() {
            @Override
            public void onResponse(Call<UserAccountFull> call, Response<UserAccountFull> response) {
                if (response.isSuccessful()) {
                    UserAccountFull user = response.body();
                    yellUserLiveData.postValue(user);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("uid",user.getUid());
                    editor.apply();
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            user.last_sync = df.format(new Date());
                            List<DashboardCard> dashboardCards = user.getDashboards();
                            List<YellTask> yellTasks;
                            for (int i = 0;i<dashboardCards.size();i++) {
                                dashboardCards.get(i).last_sync = user.getLast_sync();
                                yellTasks = dashboardCards.get(i).getTasks();
                                for (int j = 0;j<yellTasks.size();j++) {
                                    yellTasks.get(j).last_sync = user.getLast_sync();
                                }
                                for (int j =0;j<dashboardCards.get(i).getUsers().size();j++)
                                    dashboardCards.get(i).users.get(j).setId_uid(dashboardCards.get(i).dashboard_id);
                            }
                            List<BudgetCard> budgetCards = user.getBudgetCards();
                            List<TransactionCard> transactionCards;
                            for (int i = 0;i<budgetCards.size();i++) {
                                budgetCards.get(i).last_sync = user.getLast_sync();
                                transactionCards = budgetCards.get(i).getTransactionsList();
                                for (int j = 0;j<transactionCards.size();j++) {
                                    transactionCards.get(j).last_sync = user.getLast_sync();
                                }
                            }
                            realm.copyToRealmOrUpdate(user);
                        }
                    });
                } else {
                    Toast.makeText(application.getApplicationContext(), response.errorBody().toString(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<UserAccountFull> call, Throwable t) {
                Log.w("YellUserRepo", "onFailure: " + t.getMessage() );
            }
        });
    }

    public boolean getUser() {
        UserAccountFull object = realm.where(UserAccountFull.class).equalTo("uid", uid).findFirst();
        if (object == null) {
            getUserFromServer();
            return false;
        }
        else {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            long diff = 0;
            try {
                Date dt_sync = df.parse(object.getLast_sync());
                Date dt_now = df.parse(df.format(new Date()));
                diff = TimeUnit.MINUTES.convert(dt_now.getTime() - dt_sync.getTime(), TimeUnit.MILLISECONDS);
            } catch (ParseException e) {
                e.printStackTrace();
                getUserFromServer();
                return false;
            }

            if (diff > 5) {
                getUserFromServer();
                return false;
            }

            yellUserLiveData.postValue(realm.copyFromRealm(object));
            return true;
        }
    }

    public void addDashboardToServer(DashboardCard dashboardCard) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<DashboardCard> call;
        RequestBody requestBody = dashboardToJson(dashboardCard);
        call = service.addDashboard(requestBody);
        call.enqueue(new Callback<DashboardCard>() {
            @Override
            public void onResponse(Call<DashboardCard> call, Response<DashboardCard> response) {
                Log.w("YellCreateDashboard", "onResponse: " + response);
                if (response.isSuccessful()) {
                    dashboardCard.setDashboard_id(response.body().getDashboard_id());
                    dashboardCard.last_sync = df.format(new Date());
                    RealmList<DashboardPermission> dashboardPermissions = new RealmList<>();
                    DashboardPermission permission = new DashboardPermission(dashboardCard.getDashboard_id(),uid,"admin");
                    permission.setId_uid(dashboardCard.getDashboard_id());
                    dashboardPermissions.add(permission);
                    dashboardCard.setUsers(dashboardPermissions);
                    UserAccountFull userAccountFull = yellUserLiveData.getValue();
                    userAccountFull.addDashboard(dashboardCard);
                    userAccountFull.last_sync = dashboardCard.last_sync;
                    yellUserLiveData.postValue(userAccountFull);
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealmOrUpdate(userAccountFull);
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
            public void onFailure(Call<DashboardCard> call, Throwable t) {
                Toast.makeText(application.getApplicationContext(), "Lỗi khi kết nối với server", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void addBudgetToServer(BudgetCard budgetCard) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<BudgetCard> call;
        RequestBody requestBody = budgetToJson(budgetCard);
        call = service.createBudget(requestBody);
        call.enqueue(new Callback<BudgetCard>() {
            @Override
            public void onResponse(Call<BudgetCard> call, Response<BudgetCard> response) {
                Log.w("YellCreateDashboard", "onResponse: " + response);
                if (response.isSuccessful()) {
                    budgetCard.setId(response.body().getId());
                    budgetCard.last_sync = df.format(new Date());
                    UserAccountFull userAccountFull = yellUserLiveData.getValue();
                    userAccountFull.addBudget(budgetCard);
                    userAccountFull.last_sync = budgetCard.last_sync;
                    yellUserLiveData.postValue(userAccountFull);
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealmOrUpdate(userAccountFull);
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
            public void onFailure(Call<BudgetCard> call, Throwable t) {
                Toast.makeText(application.getApplicationContext(), "Lỗi khi kết nối với server", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getNotificationFromServer() {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<List<Notification>> call;
        call = service.getNotification(null);
        call.enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                Log.w("YellGetNotification", "onResponse: " + response);
                if (response.isSuccessful()) {
                    listNotificationLiveData.postValue(response.body());
                    getUserFromServer();
                    Log.w("YellGetNotification", "Get Notification Successfully " + response);
                }
                listNotificationLiveData.postValue(response.body());
            }
            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                Log.w("YellGetNotification", "onFailure: " + t.getMessage() );
                listNotificationLiveData.postValue(null);
            }
        });
    }

    public void rejectInvited(Notification notification) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<InfoMessage> call;
        RequestBody requestBody = notificationToJson(notification);
        call = service.rejectInvited(requestBody);
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                Log.w("YellReject", "onResponse: " + response);
                if(response.isSuccessful()){
                    notification.setRole(null);
                    notificationMutableLiveData.postValue(notification);
                    Log.w("YellReject", "Rejected Successfully");
                }
            }
            @Override
            public void onFailure(Call<InfoMessage> call, Throwable t) {
                Toast.makeText(application.getApplicationContext(), "Lỗi khi kết nối với server", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void confirmInvited(Notification notification) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<InfoMessage> call;
        RequestBody requestBody = notificationToJson(notification);
        call = service.confirmInvited(requestBody);
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                Log.w("YellConfirm", "onResponse: " + response);
                if(response.isSuccessful()){
                    notification.setRole(null);
                    notificationMutableLiveData.postValue(notification);
                    Log.w("YellConfirm", "Confirm Successfully");
                }
            }
            @Override
            public void onFailure(Call<InfoMessage> call, Throwable t) {
                Toast.makeText(application.getApplicationContext(), "Lỗi khi kết nối với server", Toast.LENGTH_LONG).show();
            }
        });
    }

    private RequestBody dashboardToJson(DashboardCard dashboardCard) {
        String jsonYellTask = moshi.adapter(DashboardCard.class).toJson(dashboardCard);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), jsonYellTask);
        return requestBody;
    }

    private RequestBody budgetToJson(BudgetCard budgetCard) {
        String jsonBudget = moshi.adapter(BudgetCard.class).toJson(budgetCard);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"),jsonBudget);
        return requestBody;
    }

    private RequestBody notificationToJson(Notification notification) {
        String json = moshi.adapter(Notification.class).toJson(notification);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), json);
        return requestBody;
    }


}
