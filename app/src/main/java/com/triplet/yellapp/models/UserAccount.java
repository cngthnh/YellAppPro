package com.triplet.yellapp.models;

import com.squareup.moshi.Json;

import java.util.List;

public class UserAccount {
    @Json(name="uid")
    public String id;
    @Json(name="name")
    public String name;
    @Json(name="dashboards")
    public List<String> dashboards;
    @Json(name="budgets")
    public List<String> budgets;
    @Json(name="email")
    public String email;

    public List<String> getBudgets() {
        return budgets;
    }

    public List<String> getDashboards() {
        return dashboards;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
