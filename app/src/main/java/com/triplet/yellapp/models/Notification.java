package com.triplet.yellapp.models;

import com.squareup.moshi.Json;

public class Notification {
    @Json(name = "notif_id")
    public String id;
    @Json(name = "type")
    public String type;
    @Json(name = "message")
    public String message;
    @Json(name = "read")
    public Boolean read;
    @Json(name = "role")
    public String role;
    @Json(name = "created_at")
    public String createdAt;
    @Json(name = "updated_at")
    public String updatedAt;

    public Notification(String id, String type, String message, Boolean read, String role, String createdAt, String updatedAt) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.read = read;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
