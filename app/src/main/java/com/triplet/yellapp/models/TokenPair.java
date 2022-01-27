package com.triplet.yellapp.models;

import com.squareup.moshi.Json;

public class TokenPair {
    @Json(name = "access_token")
    public String accessToken;
    @Json(name = "refresh_token")
    public String refreshToken;

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
