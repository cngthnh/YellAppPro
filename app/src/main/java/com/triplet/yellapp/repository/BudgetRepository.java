package com.triplet.yellapp.repository;

import static android.content.Context.MODE_PRIVATE;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.squareup.moshi.Moshi;
import com.triplet.yellapp.R;
import com.triplet.yellapp.models.BudgetCard;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.ErrorMessage;
import com.triplet.yellapp.models.InfoMessage;
import com.triplet.yellapp.models.TransactionCard;
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
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BudgetRepository {
    SessionManager sessionManager;
    ApiService service;
    SharedPreferences sharedPreferences;
    Application application;
    Moshi moshi;
    DateFormat df;
    MutableLiveData<BudgetCard> budgetCardMutableLiveData;

    private Realm realm;

    public BudgetRepository(Application application) {
        this.application = application;
        sharedPreferences = application.getSharedPreferences(application.getResources().getString(R.string.yell_sp), MODE_PRIVATE);
        sessionManager = SessionManager.getInstance(sharedPreferences);
        service = Client.createService(ApiService.class);
        budgetCardMutableLiveData = new MutableLiveData<>();

        moshi = new Moshi.Builder()
                .add(new RealmListJsonAdapterFactory())
                .build();
        realm = Realm.getDefaultInstance();
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public MutableLiveData<BudgetCard> getBudgetCardMutableLiveData() {
        return budgetCardMutableLiveData;
    }

    private void getBudgetFromServer(String budgetId) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<BudgetCard> call;
        call = service.getBudget(budgetId,"full");
        call.enqueue(new Callback<BudgetCard>() {
            @Override
            public void onResponse(Call<BudgetCard> call, Response<BudgetCard> response) {
                if (response.isSuccessful()) {
                    BudgetCard budget = response.body();
                    budget.last_sync = df.format(new Date());
                    budgetCardMutableLiveData.postValue(budget);
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealmOrUpdate(budget);
                        }
                    });
                } else {
                    ErrorMessage apiError = ErrorMessage.convertErrors(response.errorBody());
                    Toast.makeText(application.getApplicationContext(), apiError.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<BudgetCard> call, Throwable t) {
                budgetCardMutableLiveData.postValue(null);
                Log.w("BudgetCardFragment", "onFailure: " + t.getMessage() );
            }
        });
    }

    public boolean getBudget(String budgetId) {
        BudgetCard object = realm.where(BudgetCard.class).equalTo("id", budgetId).findFirst();
        if (object == null) {
            getBudgetFromServer(budgetId);
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
                getBudgetFromServer(budgetId);
                return false;
            }
            catch (NullPointerException e) {
                return true;
            }

            if (diff > 5) {
                getBudgetFromServer(budgetId);
                return false;
            }

            budgetCardMutableLiveData.postValue(realm.copyFromRealm(object));
            return true;
        }
    }

    private RequestBody budgetToJson(BudgetCard budgetCard) {
        String jsonBudget = moshi.adapter(BudgetCard.class).toJson(budgetCard);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"),jsonBudget);
        return requestBody;
    }

    private RequestBody transactionToJson(TransactionCard transactionCard) {
        String jsonTrans = moshi.adapter(TransactionCard.class).toJson(transactionCard);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"),jsonTrans);
        return requestBody;
    }

    private void deleteBudgetFromServer(BudgetCard budgetCard) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<InfoMessage> call;
        RequestBody requestBody = budgetToJson(budgetCard);
        call = service.deleteBudgets(requestBody);
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                Log.w("YellDeleteBudget", "onResponse: " + response);
                if (response.isSuccessful()) {
                }
            }

            @Override
            public void onFailure(Call<InfoMessage> call, Throwable t) {
                Log.w("YellDeleteBudget", "onFailure: " + t.getMessage() );
            }
        });
    }

    public void deleteBudget(BudgetCard budgetCard) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                BudgetCard object = realm.where(BudgetCard.class).equalTo("id",budgetCard.getId()).findFirst();
                if (object == null)
                    return;
                object.deleteFromRealm();
            }
        });
        deleteBudgetFromServer(budgetCard);
    }

    public void addTransactionToServer(TransactionCard transactionCard) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<TransactionCard> call;
        RequestBody requestBody = transactionToJson(transactionCard);
        call = service.addTransaction(requestBody);
        call.enqueue(new Callback<TransactionCard>() {
            @Override
            public void onResponse(Call<TransactionCard> call, Response<TransactionCard> response) {
                Log.w("YellCreateTransaction", "onResponse: " + response);
                if (response.isSuccessful()) {
                    transactionCard.setTran_id(response.body().getTran_id());
                    transactionCard.last_sync = df.format(new Date());
                    BudgetCard budgetCard = budgetCardMutableLiveData.getValue();
                    budgetCard.addTransaction(transactionCard);
                    budgetCard.setBalance(budgetCard.getBalance()+transactionCard.getAmount());
                    budgetCardMutableLiveData.postValue(budgetCard);
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealmOrUpdate(budgetCard);
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
            public void onFailure(Call<TransactionCard> call, Throwable t) {
                Toast.makeText(application.getApplicationContext(), "Lỗi khi kết nối với server", Toast.LENGTH_LONG).show();
            }
        });
    }


}
