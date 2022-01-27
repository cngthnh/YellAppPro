package com.triplet.yellapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.common.hash.Hashing;
import com.squareup.moshi.Moshi;
import com.triplet.yellapp.databinding.ActivityLoginBinding;
import com.triplet.yellapp.models.ErrorMessage;
import com.triplet.yellapp.models.TokenPair;
import com.triplet.yellapp.models.UserCredentials;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.SessionManager;

import java.nio.charset.StandardCharsets;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    ApiService service;
    SessionManager sessionManager;
    AwesomeValidation validator;
    Call<TokenPair> call;
    private ActivityLoginBinding binding;
    Moshi moshi = new Moshi.Builder().build();
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        prepareOnClickListeners();

        service = Client.createService(ApiService.class);

        sharedPreferences = getSharedPreferences(getResources().getString(R.string.yell_sp), MODE_PRIVATE);
        // if exists registration in progress
        if (sharedPreferences.contains("CR_UID")) {
            goToRegister();
        }

        sessionManager = SessionManager.getInstance(sharedPreferences);

        validator = new AwesomeValidation(ValidationStyle.BASIC);

        setupInputRules();

        if (sessionManager.getToken().getAccessToken() != null){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    private void prepareOnClickListeners() {
        binding.loginBtn.setOnClickListener(this);
        binding.gotoRegister.setOnClickListener(this);
        binding.revealPassword.setOnClickListener(this);
    }

    private void showLoading(){
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.loginBtn.setEnabled(false);
    }

    private void showForm(){
        binding.progressBar.setVisibility(View.GONE);
        binding.loginBtn.setEnabled(true);
    }

    private void revealOrHidePassword(View view) {
        ImageButton imageButton = (ImageButton) view;
        if (binding.passwordInput.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
            binding.passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            imageButton.setImageResource(R.drawable.ic_eye_hide);
        } else {
            binding.passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imageButton.setImageResource(R.drawable.ic_eye_show);
        }
    }


    void login() {

        validator.clear();

        if (!validator.validate()) {
            return;
        }
        else {
            showLoading();

            String username = binding.usernameInput.getText().toString();
            String password = binding.passwordInput.getText().toString();

            String hash = Hashing.sha256()
                    .hashString(password, StandardCharsets.UTF_8)
                    .toString();

            UserCredentials userCredentials = new UserCredentials(username, hash);

            String jsonCredentials = moshi.adapter(UserCredentials.class).toJson(userCredentials);

            RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), jsonCredentials);

            call = service.login(requestBody);
            call.enqueue(new Callback<TokenPair>() {
                @Override
                public void onResponse(Call<TokenPair> call, Response<TokenPair> response) {

                    Log.w("YellLogin", "onResponse: " + response);

                    if (response.isSuccessful()) {
                        sessionManager.saveToken(response.body());
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                    else {
                        if (response.code() == 401) {
                            ErrorMessage apiError = ErrorMessage.convertErrors(response.errorBody());
                            Toast.makeText(LoginActivity.this, apiError.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        showForm();
                    }

                }

                @Override
                public void onFailure(Call<TokenPair> call, Throwable t) {
                    Log.w("YellLogin", "onFailure: " + t.getMessage());
                    showForm();
                }
            });

        }

    }

    void goToRegister() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        finish();
    }

    public void setupInputRules() {

        validator.addValidation(binding.usernameInput, RegexTemplate.NOT_EMPTY, getResources().getString(R.string.invalid_username));
        validator.addValidation(binding.passwordInput, "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$", getResources().getString(R.string.invalid_password));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (call != null) {
            call.cancel();
            call = null;
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.loginBtn) {
            login();
        } else if (viewId == R.id.gotoRegister) {
            goToRegister();
        } else if (viewId == R.id.revealPassword) {
            revealOrHidePassword(view);
        }
    }
}