package com.triplet.yellapp.models;

import com.squareup.moshi.Json;

import java.util.List;

public class UserAccountFull {

    @Json(name = "uid")
    public String uid;
    @Json(name = "name")
    public String name;
    @Json(name = "email")
    public String email;
    @Json(name = "dashboards")
    public List<DashboardCard> dashboards;
    @Json(name = "created_at")
    public String created_at;
    @Json(name = "updated_at")
    public String updated_at;

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

    public List<DashboardCard> getDashboards() {
        return dashboards;
    }

    public void setDashboards(List<DashboardCard> dashboards) {
        this.dashboards = dashboards;
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
