package com.triplet.yellapp.models;

import com.squareup.moshi.Json;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DashboardCard extends RealmObject {
    @PrimaryKey
    @Json(name = "dashboard_id")
    public String dashboard_id;
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

    public DashboardCard(String dashboard_id, String name) {
        this.dashboard_id = dashboard_id;
        this.name = name;
    }

    public DashboardCard(String dashboard_id, String name, String description, RealmList<YellTask> tasks, RealmList<DashboardPermission> users) {
        this.dashboard_id = dashboard_id;
        this.name = name;
        //this.description = description;
        this.tasks = tasks;
        this.users = users;
    }

    public String getDashboard_id() {
        return dashboard_id;
    }

    public void setDashboard_id(String dashboard_id) {
        this.dashboard_id = dashboard_id;
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

    public RealmList<YellTask> getTasks() {
        return tasks;
    }

    public void setTasks(RealmList<YellTask> tasks) {
        this.tasks = tasks;
    }

    public void addTask(YellTask yellTask) {
        if (tasks == null)
            tasks = new RealmList<>();
        tasks.add(yellTask);
    }

    public void removeTask(YellTask yellTask) {
        if (tasks != null)
            try {
                tasks.remove(yellTask);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
    }

    public RealmList<DashboardPermission> getUsers() {
        return users;
    }

    public void setUsers(RealmList<DashboardPermission> users) {
        this.users = users;
    }
}
