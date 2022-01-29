package com.triplet.yellapp.models;

import com.squareup.moshi.Json;

import java.io.Serializable;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DashboardCard extends RealmObject {
    @PrimaryKey
    @Json(name = "dashboard_id")
    public String id;
    @Json(name = "name")
    public String name;
    @Json(name = "description")
    public String description;
    @Json(name = "tasks")
    public RealmList<YellTask> tasks;
    @Json(name = "users")
    public RealmList<DashboardPermission> users;
    public String last_sync;
    public String local_edited_at;

    public DashboardCard() {

    }


    public DashboardCard(String name) {
        this.name = name;
    }

    public DashboardCard(String id, String name, String description, RealmList<YellTask> tasks, RealmList<DashboardPermission> users) {
        this.id = id;
        this.name = name;
        //this.description = description;
        this.tasks = tasks;
        this.users = users;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<YellTask> getTasks() {
        return tasks;
    }

    public void setTasks(RealmList<YellTask> tasks) {
        this.tasks = tasks;
    }

    public List<DashboardPermission> getUsers() {
        return users;
    }

    public void setUsers(RealmList<DashboardPermission> users) {
        this.users = users;
    }
}
