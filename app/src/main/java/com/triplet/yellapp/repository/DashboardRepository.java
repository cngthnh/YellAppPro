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
import com.triplet.yellapp.models.InfoMessage;
import com.triplet.yellapp.models.UserAccount;
import com.triplet.yellapp.models.UserAccountFull;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.RealmListJsonAdapterFactory;
import com.triplet.yellapp.utils.SessionManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

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
        moshi = new Moshi.Builder()
                .add(new RealmListJsonAdapterFactory())
                .build();
        realm = Realm.getDefaultInstance();
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public MutableLiveData<DashboardCard> getDashboardCardMutableLiveData() {
        return dashboardCardMutableLiveData;
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
