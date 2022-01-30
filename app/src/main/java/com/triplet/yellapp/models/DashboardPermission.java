package com.triplet.yellapp.models;

import com.squareup.moshi.Json;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class DashboardPermission extends RealmObject {
    @Json(name = "dashboard_id")
    public String dashboard_id;
    @PrimaryKey
    @Json(name = "uid")
    public String uid;
    @Json(name = "role")
    public String role;
    public String id_uid;

    public DashboardPermission(String dashboard_id, String uid, String role) {
        this.dashboard_id = dashboard_id;
        this.uid = uid;
        this.role = role;
    }

    public void setId_uid() {
        this.id_uid = this.dashboard_id + this.uid;
    }

    public DashboardPermission() {}

    public String getDashboard_id() {
        return dashboard_id;
    }

    public void setDashboard_id(String dashboard_id) {
        this.dashboard_id = dashboard_id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
