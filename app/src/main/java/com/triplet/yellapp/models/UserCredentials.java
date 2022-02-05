package com.triplet.yellapp.models;

import com.squareup.moshi.Json;

public class UserCredentials {
    @Json(name = "uid")
    public String uid;
    @Json(name = "hash")
    public String hash;
    @Json(name="email")
    public String email;
    @Json(name="name")
    public String name;
    @Json(name="old_hash")
    public String oldHash;

    public UserCredentials(String uid) {
        this.uid = uid;
        this.hash = null;
        this.email = null;
        this.name = null;
    }

    public UserCredentials(String uid, String hash)
    {
        this.uid = uid;
        this.hash = hash;
        this.email = null;
        this.name = null;
    }

    public UserCredentials(String uid, String hash, String email, String name)
    {
        this.uid = uid;
        this.hash = hash;
        this.email = email;
        this.name = name;
    }
    public UserCredentials(String uid, String hash, String oldHash, String email, String name)
    {
        this.uid = uid;
        this.hash = hash;
        this.email = email;
        this.name = name;
        this.oldHash = oldHash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getHash() {
        return hash;
    }

    public String getUid() {
        return uid;
    }
}