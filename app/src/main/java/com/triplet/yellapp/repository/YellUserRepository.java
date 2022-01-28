package com.triplet.yellapp.repository;

import static android.content.Context.MODE_PRIVATE;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.squareup.moshi.Moshi;
import com.triplet.yellapp.LoginActivity;
import com.triplet.yellapp.R;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.ErrorMessage;
import com.triplet.yellapp.models.UserAccount;
import com.triplet.yellapp.models.UserAccountFull;
import com.triplet.yellapp.models.YellTask;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class YellUserRepository {
    // TODO: Lớp này có thể sử dụng cho nhiều fragment vì dữ liệu mà nó cung cấp
    //
    SessionManager sessionManager;
    ApiService service;
    SharedPreferences sharedPreferences;
    Application application;
    Moshi moshi = new Moshi.Builder().build();
    List<DashboardCard> dashboards;

    public MutableLiveData<List<DashboardCard>> getDashboardsLiveData() {
        return dashboardsLiveData;
    }

    MutableLiveData<List<DashboardCard>> dashboardsLiveData;

    public YellUserRepository(Application application)
    {
        this.application = application;
        sharedPreferences = application.getSharedPreferences(application.getResources().getString(R.string.yell_sp), MODE_PRIVATE);
        sessionManager = SessionManager.getInstance(sharedPreferences);
        service = Client.createService(ApiService.class);
        dashboardsLiveData = new MutableLiveData<>();
    }

    public void getUserFromServer() {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<UserAccountFull> call;
        call = service.getUserFull("full");
        call.enqueue(new Callback<UserAccountFull>() {
            @Override
            public void onResponse(Call<UserAccountFull> call, Response<UserAccountFull> response) {
                if (response.isSuccessful()) {
                    UserAccountFull userAccountFull = response.body();
                    dashboards = new ArrayList<>(userAccountFull.getDashboards());
                    dashboardsLiveData.postValue(dashboards);
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


}
