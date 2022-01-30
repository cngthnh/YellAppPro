package com.triplet.yellapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.triplet.yellapp.databinding.FragmentAccountBinding;
import com.triplet.yellapp.models.ErrorMessage;
import com.triplet.yellapp.models.InfoMessage;
import com.triplet.yellapp.models.UserAccount;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.SessionManager;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    SessionManager sessionManager;
    ApiService service;
    FragmentAccountBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        sessionManager = SessionManager.getInstance(getActivity().
                getSharedPreferences(getResources().getString(R.string.yell_sp), Context.MODE_PRIVATE));

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
                Call<InfoMessage> call = service.logout();
                call.enqueue(new Callback<InfoMessage>() {
                    @Override
                    public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                        sessionManager.deleteToken();
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

        if (sessionManager.getToken()!=null) {
            service = Client.createServiceWithAuth(ApiService.class, sessionManager);
            Call<UserAccount> call;
            call = service.getUserProfile("compact");
            call.enqueue(new Callback<UserAccount>() {
                @Override
                public void onResponse(Call<UserAccount> call, Response<UserAccount> response) {
                    if (response.isSuccessful()) {
                        binding.fullNameText.setText(response.body().getName());
                        binding.usernameText.setText("@" + response.body().getId());
                    } else {
                        ErrorMessage apiError = ErrorMessage.convertErrors(response.errorBody());
                        Toast.makeText(getActivity(), apiError.getMessage(), Toast.LENGTH_LONG).show();
                        sessionManager.deleteToken();
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                    }
                }

                @Override
                public void onFailure(Call<UserAccount> call, Throwable t) {
                    Log.w("YellAccFragment", "onFailure: " + t.getMessage() );
                }
            });
        }
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}