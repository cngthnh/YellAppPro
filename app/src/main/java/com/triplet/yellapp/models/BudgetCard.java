package com.triplet.yellapp.models;
import com.squareup.moshi.Json;

import java.util.List;

public class BudgetCard {
    @Json(name = "name")
    public String name;
    @Json(name = "balance")
    public Integer balance;
    @Json(name = "threshold")
    public Integer threshold;
    @Json(name = "created_at")
    public String created_at;
    @Json(name = "type")
    public Integer type;
    @Json(name = "budget_id")
    public String id;
    @Json(name = "transactions")
    public List<TransactionCard> transactions;
    public String updated_at;

    public BudgetCard(){}
    public BudgetCard(String name, Integer balance, Integer threshold, String created_at) {
        this.name = name;
        this.balance =balance;
        this.threshold=threshold;
        this.created_at=created_at;
    }
    public BudgetCard(String name, Integer balance){
        this.name = name;
        this.balance = balance;
    }
    public BudgetCard(String name, Integer balance, Integer threshold, Integer type) {
        this.name = name;
        this.balance = balance;
        this.threshold= threshold;
        this.type = type;
    }
    public BudgetCard(String name, Integer balance, Integer threshold, Integer type, String created_at) {
        this.name = name;
        this.balance = balance;
        this.threshold= threshold;
        this.type = type;
        this.created_at = created_at;
    }
    public BudgetCard(String name, Integer balance, Integer threshold, Integer type, String id, List<TransactionCard> transactions) {
        this.name = name;
        this.balance = balance;
        this.threshold= threshold;
        this.type = type;
        this.id = id;
        this.transactions = transactions;
    }


    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id= id;
    }
    public Integer getType() { return type;}
    public void setType(int type) { this.type = type;}


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Integer getBalance() {
        return balance;
    }
    public void setBalance(Integer balance) { this.balance = balance; }

    public Integer getThreshold() {
        return threshold;
    }
    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public String getCreated_at() { return created_at;}
    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public List<TransactionCard> getTransactionsList() { return transactions;}
    public void setTransactionsList(List<TransactionCard> transactions) { this.transactions = transactions;}

    public String getUpdated_at() { return updated_at;}
    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

}
