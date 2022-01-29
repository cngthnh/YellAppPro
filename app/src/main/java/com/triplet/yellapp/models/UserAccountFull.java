package com.triplet.yellapp.models;

import com.squareup.moshi.Json;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UserAccountFull extends RealmObject {
    @PrimaryKey
    @Json(name = "uid")
    public String uid;
    @Json(name = "name")
    public String name;
    @Json(name = "email")
    public String email;
    @Json(name = "dashboards")
    public RealmList<DashboardCard> dashboards;
    @Json(name = "budgets")
    public RealmList<BudgetCard> budgetCards;
    @Json(name = "created_at")
    public String created_at;
    @Json(name = "updated_at")
    public String updated_at;
    public String last_sync;
    public String local_edited_at;

    public String getLast_sync() {
        return last_sync;
    }

    public void setLast_sync(String last_sync) {
        this.last_sync = last_sync;
    }

    public String getLocal_edited_at() {
        return local_edited_at;
    }

    public void setLocal_edited_at(String local_edited_at) {
        this.local_edited_at = local_edited_at;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public RealmList<DashboardCard> getDashboards() {
        return dashboards;
    }

    public void setDashboards(RealmList<DashboardCard> dashboards) {
        this.dashboards = dashboards;
    }

    public void addDashboard(DashboardCard dashboardCard) {
        dashboards.add(dashboardCard);
    }

    public RealmList<BudgetCard> getBudgetCards() {
        return budgetCards;
    }

    public void setBudgetCards(RealmList<BudgetCard> budgetCards) {
        this.budgetCards = budgetCards;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }
}
