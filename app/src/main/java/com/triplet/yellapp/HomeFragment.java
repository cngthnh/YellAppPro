package com.triplet.yellapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.triplet.yellapp.adapters.DashboardsHomeAdapter;
import com.triplet.yellapp.databinding.FragmentHomeBinding;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.UserAccountFull;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.SessionManager;
import com.triplet.yellapp.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    DashboardsHomeAdapter dashboardsHomeAdapter = null;
    UserViewModel userViewModel;
    LoadingDialog loadingDialog;
    UserAccountFull user;

    SessionManager sessionManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        loadingDialog = new LoadingDialog(getActivity());
        sessionManager = SessionManager.getInstance(getActivity().
                getSharedPreferences(getResources().getString(R.string.yell_sp), Context.MODE_PRIVATE));
        dashboardsHomeAdapter = new DashboardsHomeAdapter(getContext(), sessionManager);
        userViewModel.init();
        userViewModel.getYellUserLiveData().observe(this, new Observer<UserAccountFull>() {
            @Override
            public void onChanged(UserAccountFull userAccountFull) {
                if (loadingDialog != null)
                    loadingDialog.dismissDialog();
                user = userAccountFull;
                bindingData();

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        if(!userViewModel.getUser())
            loadingDialog.startLoadingDialog();
        binding.avatarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                AccountFragment accountFragment = new AccountFragment();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, accountFragment
                        , "ACCOUNT").addToBackStack(null).commit();
            }
        });

        binding.notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                NotificationFragment notificationFragment = new NotificationFragment();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, notificationFragment
                        , "NOTIFICATION").addToBackStack(null).commit();
            }
        });
        binding.viewAllDashboardsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                ListDashboardsFragment dashboardsFragment = new ListDashboardsFragment(userViewModel);
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, dashboardsFragment
                        , "LIST_DASHBOARD").addToBackStack(null).commit();
            }
        });

        binding.viewAllBudgetsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();

                ListBudgetsFragment budgetsFragment = new ListBudgetsFragment();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, budgetsFragment
                        , "LIST_BUDGET").addToBackStack(null).commit();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        binding.dashboardPreviewList.setLayoutManager(layoutManager);
        binding.dashboardPreviewList.setVisibility(View.VISIBLE);
        binding.dashboardPreviewList.setAdapter(dashboardsHomeAdapter);

        return view;
    }

    private void bindingData () {
        dashboardsHomeAdapter.setData(user.getDashboards());
        binding.accountTitle.setText("Hi, "+user.getName());
    }

}