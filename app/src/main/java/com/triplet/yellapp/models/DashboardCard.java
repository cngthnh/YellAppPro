package com.triplet.yellapp.models;

import com.squareup.moshi.Json;

import java.util.List;

public class DashboardCard {

    @Json(name = "dashboard_id")
    public String id;
    @Json(name = "name")
    public String name;
    @Json(name = "description")
    public String description;
    @Json(name = "tasks")
    public List<YellTask> tasks;
    @Json(name = "users")
    public List<DashboardPermission> users;



    public DashboardCard(String name) {
        this.name = name;
    }

    public DashboardCard(String id, String name, String description, List<YellTask> tasks, List<DashboardPermission> users) {
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

    public void setTasks(List<YellTask> tasks) {
        this.tasks = tasks;
    }

    public List<DashboardPermission> getUsers() {
        return users;
    }

    public void setUsers(List<DashboardPermission> users) {
        this.users = users;
    }
}
