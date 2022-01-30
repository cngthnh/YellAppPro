package com.triplet.yellapp.models;

public class CategoryStatistic {
    public int total;
    public String purpose;
    public int amount;
    public int type;


    public CategoryStatistic(int total, String purpose, int amount, int type) {
        this.total = total;
        this.purpose = purpose;
        this.amount = amount;
        this.type = type;
    }

    public int getTotal() {
        return total;
    }

    public String getPurpose() {
        return purpose;
    }

    public int getAmount() {
        return amount;
    }

    public int getType() {
        return type;
    }
}
