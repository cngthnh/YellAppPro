package com.triplet.yellapp;

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.google.android.material.snackbar.Snackbar;
import com.triplet.yellapp.databinding.FragmentAccountBinding;
import com.triplet.yellapp.databinding.FragmentSettingsBinding;
import com.triplet.yellapp.utils.GlobalStatus;

public class SettingsFragment extends Fragment {

    FragmentSettingsBinding binding;
    GlobalStatus globalStatus = GlobalStatus.getInstance();
    SharedPreferences sharedPreferences;

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getActivity().
                getSharedPreferences(getResources().getString(R.string.yell_sp), Context.MODE_PRIVATE);
    }

    private void setNightMode(Context target, Boolean state){

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

    private void reload() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.autoModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!b) {
                    binding.darkModeCard.setVisibility(View.VISIBLE);
                    int nightModeFlags =
                            getContext().getResources().getConfiguration().uiMode &
                                    Configuration.UI_MODE_NIGHT_MASK;

                    binding.darkModeSwitch.setChecked(nightModeFlags == Configuration.UI_MODE_NIGHT_YES);
                    if (binding.darkModeSwitch.isChecked()) {
                        sharedPreferences.edit().putInt(getResources().getString(R.string.yell_ui_mode), 2).apply();
                        globalStatus.setUiMode(2);
                    } else {
                        sharedPreferences.edit().putInt(getResources().getString(R.string.yell_ui_mode), 1).apply();
                        globalStatus.setUiMode(1);
                    }
                    binding.darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if (b) {
                                sharedPreferences.edit().putInt(getResources().getString(R.string.yell_ui_mode), 2).apply();
                                globalStatus.setUiMode(2);
                                setNightMode(getActivity(), true);
                                reload();
                            } else {
                                sharedPreferences.edit().putInt(getResources().getString(R.string.yell_ui_mode), 1).apply();
                                globalStatus.setUiMode(1);
                                setNightMode(getActivity(), false);
                                reload();
                            }
                        }
                    });

                } else if (Build.VERSION.SDK_INT >= 29){
                    binding.darkModeCard.setVisibility(View.GONE);
                    sharedPreferences.edit().putInt(getResources().getString(R.string.yell_ui_mode), 0).apply();
                    globalStatus.setUiMode(0);
                    setNightMode(getActivity(), null);
                    reload();
                } else {
                    binding.autoModeSwitch.setChecked(true);
                    Snackbar.make(getActivity().findViewById(R.id.mainActivity),
                            "Phiên bản Android bạn đang sử dụng cũ hơn Android Q nên không thể thay đổi màu giao diện theo hệ thống",
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        binding.autoModeSwitch.setChecked(globalStatus.getUiMode() == 0);



        binding.backAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity() != null){
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        return view;
    }
}