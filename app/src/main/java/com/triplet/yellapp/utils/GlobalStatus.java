package com.triplet.yellapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class GlobalStatus {
    private boolean guestMode;
    private String username;
    private static class GlobalStatusHelper {
        private static final GlobalStatus INSTANCE = new GlobalStatus();
    }
    public static GlobalStatus getInstance() {
        return GlobalStatusHelper.INSTANCE;
    }
    private GlobalStatus() {
        guestMode = false;
        username = "Guest";
    }
    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void setGuestMode(boolean guestMode) {
        this.guestMode = guestMode;
    }

    public boolean getGuestMode() {
        return this.guestMode;
    }
}
