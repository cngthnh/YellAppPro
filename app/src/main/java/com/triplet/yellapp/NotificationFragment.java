package com.triplet.yellapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.squareup.moshi.Moshi;
import com.triplet.yellapp.adapters.NotificationAdapter;
import com.triplet.yellapp.adapters.UsersDetailAdapter;
import com.triplet.yellapp.databinding.FragmentHomeBinding;
import com.triplet.yellapp.databinding.FragmentListDashboardsBinding;
import com.triplet.yellapp.databinding.FragmentNotificationBinding;
import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.DashboardPermission;
import com.triplet.yellapp.models.InfoMessage;
import com.triplet.yellapp.models.Notification;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.SessionManager;
import com.triplet.yellapp.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment extends Fragment {
    FragmentNotificationBinding binding;
    List<Notification> listNotification;
    NotificationAdapter notificationAdapter;
    UserViewModel userViewModel;
    LoadingDialog loadingDialog;

    public NotificationFragment() {
    }

    public NotificationFragment(UserViewModel viewModel) {
        userViewModel = viewModel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationAdapter = new NotificationAdapter(getActivity(), userViewModel);
        loadingDialog = new LoadingDialog(getActivity());
        userViewModel.getListNotificationLivaData().observe(this, new Observer<List<Notification>>() {
            @Override
            public void onChanged(List<Notification> notifications) {
                listNotification = notifications;
                if (getActivity()!=null) {
                    bindingData();
                    if (loadingDialog != null)
                        loadingDialog.dismissDialog();
                }
            }
        });
    }

    private void bindingData() {
        notificationAdapter.setData(listNotification);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNotificationBinding.inflate(inflater, container,false);
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.yellow_primary));
        View view = binding.getRoot();
        loadingDialog.startLoadingDialog();
        userViewModel.getNotification();
        binding.backNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity() != null){
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        binding.recycleView.setAdapter(notificationAdapter);
        binding.recycleView.setLayoutManager(layoutManager);
        return view;
    }
}
