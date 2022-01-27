package com.triplet.yellapp.models;

import com.squareup.moshi.Json;

public class DashboardPermission {
    @Json(name = "dashboard_id")
    public String id;
    @Json(name = "uid")
    public String uid;
    @Json(name = "role")
    public String role;

    public DashboardPermission(String id, String uid, String role) {
        this.id = id;
        this.uid = uid;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
