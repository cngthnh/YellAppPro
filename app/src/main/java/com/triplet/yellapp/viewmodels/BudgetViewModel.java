package com.triplet.yellapp.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.triplet.yellapp.models.BudgetCard;
import com.triplet.yellapp.models.TransactionCard;
import com.triplet.yellapp.models.YellTask;
import com.triplet.yellapp.repository.BudgetRepository;

public class BudgetViewModel extends AndroidViewModel {

    BudgetRepository repository;
    LiveData<BudgetCard> budgetCardLiveData;

    public BudgetViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        repository = new BudgetRepository(getApplication());
        budgetCardLiveData = repository.getBudgetCardMutableLiveData();
    }

    public boolean getBudget(String budgetId) {
        return repository.getBudget(budgetId);
    }

    public LiveData<BudgetCard> getBudgetCardLiveData() {
        return budgetCardLiveData;
    }

    public void addTransaction(TransactionCard transactionCard) {
        repository.addTransactionToServer(transactionCard);
    }
}
