package com.triplet.yellapp.repository;

import static android.content.Context.MODE_PRIVATE;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.squareup.moshi.Moshi;
import com.triplet.yellapp.R;
import com.triplet.yellapp.adapters.DashboardsAdapter;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.ErrorMessage;
import com.triplet.yellapp.models.InfoMessage;
import com.triplet.yellapp.models.UserAccount;
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
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardRepository {
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

    private void editDashboardOnServer(DashboardCard dashboardCard) {
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

    public void editDashboard(DashboardCard dashboardCard) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(dashboardCard);
            }
        });
        editDashboardOnServer(dashboardCard);
    }

    private void deleteDashboardFromServer(DashboardCard dashboardCard) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<InfoMessage> call;
        RequestBody requestBody = dashboardToJson(dashboardCard);
        call = service.deleteDashboard(requestBody);
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                Log.w("YellDeleteDashboard", "onResponse: " + response);
                if (response.isSuccessful()) {
                }
            }

            @Override
            public void onFailure(Call<InfoMessage> call, Throwable t) {
                Log.w("YellDeleteDashboard", "onFailure: " + t.getMessage() );
            }
        });
    }

    public void deleteDashboard(DashboardCard dashboardCard) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                DashboardCard object = realm.where(DashboardCard.class).equalTo("id",dashboardCard.getId()).findFirst();
                if (object == null)
                    return;
                object.deleteFromRealm();
            }
        });
        deleteDashboardFromServer(dashboardCard);
    }

    private RequestBody dashboardToJson(DashboardCard dashboardCard) {
        String jsonYellTask = moshi.adapter(DashboardCard.class).toJson(dashboardCard);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), jsonYellTask);
        return requestBody;
    }

}
