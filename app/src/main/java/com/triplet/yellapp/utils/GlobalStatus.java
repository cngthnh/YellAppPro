package com.triplet.yellapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class GlobalStatus {
    private boolean guestMode;
    private boolean offlineMode;
    private String username;
    private static volatile GlobalStatus INSTANCE = null;
    public static GlobalStatus getInstance() {
        if (INSTANCE == null)
            synchronized (GlobalStatus.class) {
                if (INSTANCE == null) {
                    INSTANCE = new GlobalStatus();
                }
            }
        return INSTANCE;
    }
    private GlobalStatus() {
        guestMode = false;
        username = "Guest";
    }

    public void setOfflineMode(boolean offlineMode) {
        this.offlineMode = offlineMode;
    }

    public boolean isOfflineMode() {
        return offlineMode;
    }

    public void setGuestMode(boolean guestMode) {
        this.guestMode = guestMode;
    }

    public boolean isGuestMode() {
        return this.guestMode;
    }
}
