package com.triplet.yellapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.triplet.yellapp.utils.GlobalStatus;
import com.triplet.yellapp.viewmodels.UserViewModel;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity {
    private boolean doubleTapToQuit;
    private NetworkChangeReceiver receiver;
    private SharedPreferences sharedPreferences;
    private GlobalStatus globalStatus = GlobalStatus.getInstance();
    private UserViewModel userViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getResources().getColor(R.color.darker_gray));
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(getResources().getString(R.string.yell_sp), MODE_PRIVATE);

        Realm.init(this);

        globalStatus.setEditedOffline(sharedPreferences.getString(getResources().getString(R.string.edited_offline),
                getResources().getString(R.string.bool_no)).equals(getResources().getString(R.string.bool_yes)));

        receiver = new NetworkChangeReceiver(this) {
            @Override
            protected void raiseSnackbar() {
                GlobalStatus globalStatus = GlobalStatus.getInstance();
                String conn = globalStatus.isOfflineMode() ? "ngắt kết nối" : "kết nối";
                Snackbar.make(findViewById(R.id.mainActivity), "Đã " + conn + " Internet", Snackbar.LENGTH_LONG).show();
            }

            @Override
            protected void syncWithServer() {
                userViewModel = new ViewModelProvider((ViewModelStoreOwner) this.context).get(UserViewModel.class);
                userViewModel.init();
                userViewModel.sync();
            }
        };
        registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        globalStatus.getStartCheck().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                HomeFragment homeFragment = new HomeFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                ((FragmentTransaction) transaction).setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                transaction.replace(R.id.fragmentContainer, homeFragment, "HOME");
                transaction.addToBackStack("HOME");
                transaction.commit();
            }
        });
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

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}

