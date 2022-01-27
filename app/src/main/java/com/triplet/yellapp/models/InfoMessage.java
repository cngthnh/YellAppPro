package com.triplet.yellapp.models;

import com.squareup.moshi.Json;

public class InfoMessage {
    @Json(name="message")
    public String message;

    public InfoMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
}
