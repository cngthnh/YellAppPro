package com.triplet.yellapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.UserAccountFull;
import com.triplet.yellapp.models.YellTask;
import com.triplet.yellapp.utils.GlobalStatus;
import com.triplet.yellapp.utils.NetworkChangeReceiver;
import com.triplet.yellapp.viewmodels.UserViewModel;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {
    private boolean doubleTapToQuit;
    private NetworkChangeReceiver receiver;
    private SharedPreferences sharedPreferences;
    private GlobalStatus globalStatus = GlobalStatus.getInstance();
    private UserViewModel userViewModel;
    private HomeFragment homeFragment;
    private LoadingDialog loadingDialog;
    public static void setNightMode(Context target, Boolean state){

        if (state == null) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            return;
        }

        if (state) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getResources().getColor(R.color.darker_gray));
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(getResources().getString(R.string.yell_sp), MODE_PRIVATE);
        homeFragment = new HomeFragment();
        loadingDialog = new LoadingDialog(this);

        globalStatus.setUiMode(sharedPreferences.getInt(getResources().getString(R.string.yell_ui_mode), 0));
        if (Build.VERSION.SDK_INT < 29) {
            if (globalStatus.getUiMode() == 0) globalStatus.setUiMode(1);
        }

        switch (globalStatus.getUiMode()) {
            case 0:
                setNightMode(this, null);
                break;
            case 2:
                setNightMode(this, true);
                break;
            default:
                setNightMode(this, false);
        }

        Realm.init(this);

        globalStatus.setEditedOffline(sharedPreferences.getString(getResources().getString(R.string.edited_offline),
                getResources().getString(R.string.bool_no)).equals(getResources().getString(R.string.bool_yes)));

        receiver = new NetworkChangeReceiver(this) {
            @Override
            protected void raiseSnackbar() {
                GlobalStatus globalStatus = GlobalStatus.getInstance();
                String conn = globalStatus.isOfflineMode() ? "ngắt kết nối" : "kết nối";
                Snackbar.make(findViewById(R.id.mainActivity), "Đã " + conn + " Internet", Snackbar.LENGTH_LONG).show();
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("HOME");
                if (fragment!=null)
                    getSupportFragmentManager().popBackStack("HOME",0);
            }

            @Override
            protected void syncWithServer() {
                userViewModel = new ViewModelProvider((ViewModelStoreOwner) this.context).get(UserViewModel.class);
                userViewModel.init();
                loadingDialog.startLoadingDialog();
                userViewModel.getSyncDashboardCardLiveData().observe((LifecycleOwner) this.context, new Observer<DashboardCard>() {
                    @Override
                    public void onChanged(DashboardCard dashboardCard) {
                        userViewModel.syncAddTasks(dashboardCard.getTasks());
                    }
                });
                userViewModel.getSyncYellTaskLiveData().observe((LifecycleOwner) this.context, new Observer<YellTask>() {
                    @Override
                    public void onChanged(YellTask yellTask) {
                        userViewModel.syncAddSubTasks(yellTask.getSubtasks(),yellTask);
                    }
                });
                userViewModel.sync().observe((LifecycleOwner) this.context, new Observer<UserAccountFull>() {
                    @Override
                    public void onChanged(UserAccountFull userAccountFull) {
                        if (loadingDialog != null)
                            loadingDialog.dismissDialog();
                    }
                });
            }
        };
        registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        globalStatus.getStartCheck().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                ((FragmentTransaction) transaction).setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                transaction.replace(R.id.fragmentContainer, homeFragment, "HOME");
                transaction.addToBackStack("HOME");
                transaction.commit();
                if (userViewModel != null)
                    userViewModel.sync();
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

