package com.triplet.yellapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.triplet.yellapp.utils.GlobalStatus;

public abstract class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            GlobalStatus globalStatus = GlobalStatus.getInstance();
            globalStatus.setOfflineMode(!isOnline(context));
            raiseSnackbar();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    protected abstract void raiseSnackbar();

    public boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();

            return (netInfo != null && netInfo.isConnected());
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }
}