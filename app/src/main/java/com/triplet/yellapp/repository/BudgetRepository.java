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
import com.triplet.yellapp.utils.GlobalStatus;
import com.triplet.yellapp.utils.RealmListJsonAdapterFactory;
import com.triplet.yellapp.utils.SessionManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BudgetRepository {
    private final int TRUE_UUID_LEN = 36;
    private final int TEMP_UUID_LEN = 40;
    private final int DELETED_UUID_LEN = 43;
    SessionManager sessionManager;
    ApiService service;
    SharedPreferences sharedPreferences;
    Application application;
    Moshi moshi;
    DateFormat df;
    MutableLiveData<BudgetCard> budgetCardMutableLiveData;
    GlobalStatus globalStatus = GlobalStatus.getInstance();

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
                    try {
                        ErrorMessage apiError = ErrorMessage.convertErrors(response.errorBody());
                        Toast.makeText(application.getApplicationContext(), apiError.getMessage(), Toast.LENGTH_LONG).show();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
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
            if (!globalStatus.isOfflineMode()) {
                long diff = 0;
                try {
                    Date dt_sync = df.parse(object.last_sync);
                    Date dt_now = df.parse(df.format(new Date()));
                    diff = TimeUnit.MINUTES.convert(dt_now.getTime() - dt_sync.getTime(), TimeUnit.MILLISECONDS);
                } catch (ParseException e) {
                    e.printStackTrace();
                    getBudgetFromServer(budgetId);
                    return false;
                } catch (NullPointerException e) {
                    return true;
                }

                if (diff > 5) {
                    getBudgetFromServer(budgetId);
                    return false;
                }
            }
            BudgetCard result = realm.copyFromRealm(object);
            // những budget nào đã được đánh dấu xoá thì không hiển thị
            for (TransactionCard transaction : result.transactions) {
                if (transaction.getTran_id().length() == DELETED_UUID_LEN) result.removeTransaction(transaction);
            }

            budgetCardMutableLiveData.postValue(result);
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

    private void deleteBudgetInLocalDb(BudgetCard budgetCard) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                BudgetCard object = realm.where(BudgetCard.class).equalTo("id", budgetCard.getId()).findFirst();
                if (object == null)
                    return;
                object.deleteFromRealm();
            }
        });
    }

    public void syncDeletedBudgetWithServer(BudgetCard budgetCard) {
        deleteBudgetInLocalDb(new BudgetCard(budgetCard.getId(), ""));
        if (budgetCard.getId().length() == DELETED_UUID_LEN)
            budgetCard.id = budgetCard.getId().replace("DELETED", "");
        deleteBudgetFromServer(budgetCard);
    }

    public void deleteBudget(BudgetCard budgetCard) {
        if (!globalStatus.isOfflineMode()) {
            syncDeletedBudgetWithServer(budgetCard);
        } else {
            if (budgetCard.getId().length() == TRUE_UUID_LEN) {
                deleteBudgetInLocalDb(budgetCard);
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        budgetCard.id = "DELETED" + budgetCard.getId();
                        budgetCard.local_edited_at = df.format(new Date());
                        realm.copyToRealmOrUpdate(budgetCard);
                    }
                });
                globalStatus.setEditedOffline(true);
                sharedPreferences.edit().putString(application.getResources().getString(R.string.edited_offline),
                        application.getResources().getString(R.string.bool_yes)).apply();
            } else if (budgetCard.getId().length() == TEMP_UUID_LEN) {
                deleteBudgetInLocalDb(budgetCard);
            }
        }
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

                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            BudgetCard budgetCard = realm.copyFromRealm(realm
                                    .where(BudgetCard.class)
                                    .equalTo("id",transactionCard.getBudget_id())
                                    .findFirst());
                            TransactionCard needToDelete = null;
                            if (transactionCard.getTran_id() != null) {
                                needToDelete = realm.where(TransactionCard.class).equalTo("transaction_id", transactionCard.getTran_id()).findFirst();
                                budgetCard.removeTransaction(transactionCard);
                            }
                            transactionCard.setTran_id(response.body().getTran_id());
                            transactionCard.local_edited_at = null;
                            transactionCard.last_sync = df.format(new Date());
                            budgetCard.addTransaction(transactionCard);
                            budgetCard.setBalance(budgetCard.getBalance()+transactionCard.getAmount());
                            budgetCardMutableLiveData.postValue(budgetCard);
                            realm.copyToRealmOrUpdate(budgetCard);
                            if (needToDelete != null) {
                                needToDelete.deleteFromRealm();
                            }
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

    private void deleteTransactionOnServer(TransactionCard transactionCard) {
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        Call<InfoMessage> call;
        RequestBody requestBody = transactionToJson(transactionCard);
        call = service.deleteTransaction(requestBody);
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                Log.w("TransactionDeleted", "onResponse: " + response.body().getMessage());
                if (response.isSuccessful()) {

                    BudgetCard budgetCard = budgetCardMutableLiveData.getValue();
                    budgetCard.deleteTransaction(transactionCard);
                    budgetCard.setBalance(budgetCard.getBalance()-transactionCard.getAmount());
                    budgetCardMutableLiveData.postValue(budgetCard);
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealmOrUpdate(budgetCard);
                        }
                    });
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

    private void deleteTransactionInLocalDb(TransactionCard transactionCard) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                TransactionCard object = realm.where(TransactionCard.class)
                        .equalTo("transaction_id",transactionCard.getTran_id()).findFirst();
                if (object == null)
                    return;
                object.deleteFromRealm();
                Log.w("TransactionDeleted", "Deleted " + transactionCard.getContent()+" on DB");
            }
        });
    }

    public void syncDeletedTransactionWithServer(TransactionCard transactionCard) {
        deleteTransactionInLocalDb(new TransactionCard(transactionCard.getTran_id()));
        if (transactionCard.getTran_id().length() == DELETED_UUID_LEN)
            transactionCard.transaction_id = transactionCard.getTran_id().replace("DELETED", "");
        deleteTransactionOnServer(transactionCard);
    }

    public void deleteTransaction(TransactionCard transactionCard) {
        if (!globalStatus.isOfflineMode()) {
            syncDeletedTransactionWithServer(transactionCard);
        } else {
            if (transactionCard.getTran_id().length() == TRUE_UUID_LEN) {
                deleteTransactionInLocalDb(transactionCard);
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        transactionCard.transaction_id = "DELETED" + transactionCard.getTran_id();
                        transactionCard.local_edited_at = df.format(new Date());
                        realm.copyToRealmOrUpdate(transactionCard);
                    }
                });
                globalStatus.setEditedOffline(true);
                sharedPreferences.edit().putString(application.getResources().getString(R.string.edited_offline),
                        application.getResources().getString(R.string.bool_yes)).apply();
            } else if (transactionCard.getTran_id().length() == TEMP_UUID_LEN) {
                deleteTransactionInLocalDb(transactionCard);
            }
        }
    }

    public void addTransaction(TransactionCard transactionCard) {
        transactionCard.budget_id = budgetCardMutableLiveData.getValue().getId();
        if (globalStatus.isOfflineMode())
            addTransactionInLocalDb(transactionCard);
        else
            addTransactionToServer(transactionCard);
    }

    private void addTransactionInLocalDb(TransactionCard transactionCard) {
        transactionCard.setTran_id("TEMP" + UUID.randomUUID().toString());
        transactionCard.local_edited_at = df.format(new Date());
        BudgetCard budgetCard = budgetCardMutableLiveData.getValue();
        budgetCard.addTransaction(transactionCard);
        budgetCard.setBalance(budgetCard.getBalance() + transactionCard.getAmount());
        budgetCardMutableLiveData.postValue(budgetCard);
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(budgetCard);
            }
        });

        sharedPreferences.edit().putString(application.getResources().getString(R.string.edited_offline),
                application.getResources().getString(R.string.bool_yes)).apply();
        GlobalStatus globalStatus = GlobalStatus.getInstance();
        globalStatus.setEditedOffline(true);
    }
}
