package com.triplet.yellapp.models;

import com.squareup.moshi.Json;

public class EmailConfirmation {
    @Json(name="email")
    public String email;
    @Json(name="uid")
    public String uid;
    @Json(name="code")
    public String code;
    public EmailConfirmation(String uid, String email, String code) {
        this.uid = uid;
        this.email = email;
        this.code = code;
    }
}
