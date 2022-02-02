package com.triplet.yellapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.triplet.yellapp.utils.GlobalStatus;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity {
    private boolean doubleTapToQuit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getResources().getColor(R.color.darker_gray));
        setContentView(R.layout.activity_main);

        Realm.init(this);

        NetworkChangeReceiver receiver = new NetworkChangeReceiver() {
            @Override
            protected void raiseSnackbar() {
                GlobalStatus globalStatus = GlobalStatus.getInstance();
                String conn = globalStatus.isOfflineMode() ? "ngắt kết nối" : "kết nối";
                Snackbar.make(findViewById(R.id.mainActivity), "Đã " + conn + " Internet", Snackbar.LENGTH_LONG).show();
            }
        };
        registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        ((FragmentTransaction) transaction).setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.replace(R.id.fragmentContainer, homeFragment, "HOME");
        transaction.addToBackStack("HOME");
        transaction.commit();
    }
    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment instanceof HomeFragment) {
            if (doubleTapToQuit) {
                finish();
                return;
            }
            this.doubleTapToQuit = true;
            Toast.makeText(this, "Nhấn quay lại một lần nữa để thoát ứng dụng", Toast.LENGTH_SHORT).show();

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleTapToQuit = false;
                }
            }, 2000);

            ((HomeFragment) currentFragment).onBackPressed();
        } else
            super.onBackPressed();
    }
}

