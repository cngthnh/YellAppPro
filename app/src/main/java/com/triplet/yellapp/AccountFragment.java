package com.triplet.yellapp;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.triplet.yellapp.databinding.FragmentAccountBinding;
import com.triplet.yellapp.models.ErrorMessage;
import com.triplet.yellapp.models.InfoMessage;
import com.triplet.yellapp.models.UserAccount;
import com.triplet.yellapp.models.UserAccountFull;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.SessionManager;
import com.triplet.yellapp.viewmodels.UserViewModel;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    SessionManager sessionManager;
    ApiService service;
    FragmentAccountBinding binding;
    UserViewModel userViewModel;
    LoadingDialog loadingDialog;
    UserAccountFull user;
    AccountInfoFragment accountInfoFragment;

    AccountFragment(UserViewModel userViewModel) {
        this.userViewModel = userViewModel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = SessionManager.getInstance(getActivity().
                getSharedPreferences(getResources().getString(R.string.yell_sp), Context.MODE_PRIVATE));
        service = Client.createServiceWithAuth(ApiService.class, sessionManager);
        loadingDialog = new LoadingDialog(getActivity());
        userViewModel.getYellUserLiveData().observe(this, new Observer<UserAccountFull>() {
            @Override
            public void onChanged(UserAccountFull userAccountFull) {
                if (loadingDialog != null) {
                    loadingDialog.dismissDialog();
                }
                user = userAccountFull;
                bindingData();
            }
        });
        accountInfoFragment = new AccountInfoFragment(userViewModel);
    }

    private void bindingData() {
        binding.fullNameText.setText(user.getName());
        binding.usernameText.setText("@" + user.getUid());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        if (!userViewModel.getUser())
            loadingDialog.startLoadingDialog();
        else if (user != null)
            bindingData();

        binding.backAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity() != null){
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        binding.signoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.startLoadingDialog();
                Call<InfoMessage> call = service.logout();
                call.enqueue(new Callback<InfoMessage>() {
                    @Override
                    public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                        sessionManager.deleteToken();
                        if (loadingDialog != null) loadingDialog.dismissDialog();
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(Call<InfoMessage> call, Throwable t) {
                        Log.w("YellLogout", "onFailure: " + t.getMessage() );
                    }
                });
            }
        });

        binding.accountInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                activity.getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right,
                                R.anim.slide_in_right, R.anim.slide_out_left)
                        .replace(R.id.fragmentContainer, accountInfoFragment, "ACCOUNT_INFO")
                        .addToBackStack(null).commit();
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}