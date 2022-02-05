package com.triplet.yellapp;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.hash.Hashing;
import com.squareup.moshi.Moshi;
import com.triplet.yellapp.databinding.FragmentAccountBinding;
import com.triplet.yellapp.databinding.FragmentAccountInfoBinding;
import com.triplet.yellapp.models.ErrorMessage;
import com.triplet.yellapp.models.InfoMessage;
import com.triplet.yellapp.models.TokenPair;
import com.triplet.yellapp.models.UserAccountFull;
import com.triplet.yellapp.models.UserCredentials;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.SessionManager;
import com.triplet.yellapp.viewmodels.UserViewModel;

import java.nio.charset.StandardCharsets;

import io.realm.Realm;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountInfoFragment extends Fragment {
    FragmentAccountInfoBinding binding;
    SessionManager sessionManager;
    ApiService service;
    UserViewModel userViewModel;
    LoadingDialog loadingDialog;
    UserAccountFull user;
    Moshi moshi = new Moshi.Builder().build();
    Call<InfoMessage> call;
    Realm realm;

    public AccountInfoFragment(UserViewModel userViewModel) {
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
        realm = Realm.getDefaultInstance();
    }

    private void bindingData() {
        binding.name.setText(user.getName());
        binding.username.setText(user.getUid());
        binding.email.setText(user.getEmail());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountInfoBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        if (!userViewModel.getUser())
            loadingDialog.startLoadingDialog();

        binding.backAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity() != null){
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        binding.revealOldPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                revealOrHidePassword(view);
            }
        });

        binding.revealNewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                revealOrHidePassword(view);
            }
        });

        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.startLoadingDialog();
                String username = user.getUid();
                String oldPassword = binding.oldPasswordInput.getText().toString();
                String newPassword = binding.newPasswordInput.getText().toString();
                String name = binding.name.getText().toString();

                String oldHash = null, newHash = null;

                if (oldPassword.length() > 0)
                    oldHash = Hashing.sha256()
                            .hashString(oldPassword, StandardCharsets.UTF_8)
                            .toString();
                if (newPassword.length() > 0)
                    newHash = Hashing.sha256()
                            .hashString(newPassword, StandardCharsets.UTF_8)
                            .toString();

                UserCredentials userCredentials = new UserCredentials(username, newHash, oldHash, null, name);

                String jsonCredentials = moshi.adapter(UserCredentials.class).toJson(userCredentials);

                RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), jsonCredentials);

                call = service.updateUser(requestBody);
                call.enqueue(new Callback<InfoMessage>() {
                    @Override
                    public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                        if (response.isSuccessful()) {
                            Snackbar.make(getActivity().findViewById(R.id.mainActivity), "Thay đổi thông tin thành công", Snackbar.LENGTH_SHORT).show();
                            realm.executeTransactionAsync(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    UserAccountFull userAccountFull = realm.where(UserAccountFull.class).equalTo("uid", user.getUid()).findFirst();
                                    userAccountFull.setName(name);
                                    if (loadingDialog != null) loadingDialog.dismissDialog();
                                }
                            });
                        }
                        else {
                            Snackbar.make(getActivity().findViewById(R.id.mainActivity), "Không thể thay đổi mật khẩu do mật khẩu cũ không chính xác", Snackbar.LENGTH_SHORT).show();
                            loadingDialog.dismissDialog();
                        }
                    }

                    @Override
                    public void onFailure(Call<InfoMessage> call, Throwable t) {
                        Snackbar.make(getActivity().findViewById(R.id.mainActivity), "Có lỗi không xác định xảy ra", Snackbar.LENGTH_SHORT).show();
                        loadingDialog.dismissDialog();
                    }
                });
            }
        });

        return view;
    }

    private void revealOrHidePassword(View view) {
        ImageButton imageButton = (ImageButton) view;

        if (view.getId() == R.id.revealOldPassword) {
            if (binding.oldPasswordInput.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                binding.oldPasswordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                imageButton.setImageResource(R.drawable.ic_eye_hide);
            } else {
                binding.oldPasswordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                imageButton.setImageResource(R.drawable.ic_eye_show);
            }
        } else if (view.getId() == R.id.revealNewPassword) {
            if (binding.newPasswordInput.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                binding.newPasswordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                imageButton.setImageResource(R.drawable.ic_eye_hide);
            } else {
                binding.newPasswordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                imageButton.setImageResource(R.drawable.ic_eye_show);
            }
        }
    }
}