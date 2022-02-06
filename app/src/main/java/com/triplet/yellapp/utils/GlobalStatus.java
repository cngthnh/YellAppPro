package com.triplet.yellapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.lifecycle.MutableLiveData;

public class GlobalStatus {
    private boolean guestMode;
    private boolean offlineMode;
    private String username;
    private boolean editedOffline;
    private int uiMode;
    private MutableLiveData<Boolean> startCheck = new MutableLiveData<Boolean>();
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
        startCheck.postValue(false);
        uiMode = 1;
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

    public void setEditedOffline(boolean editedOffline) {
        this.editedOffline = editedOffline;
    }

    public boolean isEditedOffline() {
        return editedOffline;
    }

    public void setStartCheck(boolean startCheck) {
        this.startCheck.postValue(startCheck);
    }

    public MutableLiveData<Boolean> getStartCheck() {
        return startCheck;
    }

    public void setUiMode(int uiMode) {
        this.uiMode = uiMode;
    }

    public int getUiMode() {
        return uiMode;
    }
}
