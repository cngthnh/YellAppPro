package com.triplet.yellapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.common.hash.Hashing;
import com.squareup.moshi.Moshi;
import com.triplet.yellapp.databinding.ActivityRegisterBinding;
import com.triplet.yellapp.models.EmailConfirmation;
import com.triplet.yellapp.models.ErrorMessage;
import com.triplet.yellapp.models.InfoMessage;
import com.triplet.yellapp.models.TokenPair;
import com.triplet.yellapp.models.UserCredentials;
import com.triplet.yellapp.utils.ApiService;
import com.triplet.yellapp.utils.Client;
import com.triplet.yellapp.utils.SessionManager;

import java.nio.charset.StandardCharsets;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    ApiService service;
    private ActivityRegisterBinding binding;
    AwesomeValidation registerValidator, emailConfValidator;
    Moshi moshi = new Moshi.Builder().build();
    SessionManager sessionManager;
    Call<InfoMessage> call;
    private String currentUsername = null, currentHash = null, currentEmail = null;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        sharedPreferences = getSharedPreferences(getResources().getString(R.string.yell_sp), MODE_PRIVATE);
        sessionManager = SessionManager.getInstance(sharedPreferences);

        service = Client.createService(ApiService.class);

        prepareComponents();
        if (sharedPreferences.contains("CR_UID")) {
            loadPrevInfo();
            confirmEmail();
        }

        registerValidator = new AwesomeValidation(ValidationStyle.BASIC);
        emailConfValidator = new AwesomeValidation(ValidationStyle.BASIC);
        setupInputRules();
    }

    private void loadPrevInfo() {
        currentUsername = sharedPreferences.getString("CR_UID", null);
        currentHash = sharedPreferences.getString("CR_HSH", null);
        currentEmail = sharedPreferences.getString("CR_EML", null);
    }

    private void savePrevInfo() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("CR_UID", currentUsername);
        editor.putString("CR_HSH", currentHash);
        editor.putString("CR_EML", currentEmail);
        editor.apply();
    }

    private void removePrevInfo() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        currentEmail = null;
        currentUsername = null;
        currentHash = null;
        editor.remove("CR_UID");
        editor.remove("CR_HSH");
        editor.remove("CR_EML");
        editor.apply();
    }

    private void setupInputRules() {
        registerValidator.addValidation(binding.nameInput, RegexTemplate.NOT_EMPTY, getResources().getString(R.string.invalid_name));
        registerValidator.addValidation(binding.usernameInput, RegexTemplate.NOT_EMPTY, getResources().getString(R.string.invalid_username));
        registerValidator.addValidation(binding.emailInput, Patterns.EMAIL_ADDRESS, getResources().getString(R.string.invalid_email));
        registerValidator.addValidation(binding.passwordInput, "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$", getResources().getString(R.string.invalid_password));
        registerValidator.addValidation(binding.passConfInput, binding.passwordInput, getResources().getString(R.string.invalid_pass_conf));
        emailConfValidator.addValidation(binding.code1, RegexTemplate.NOT_EMPTY, getResources().getString(R.string.invalid_email_conf_code));
        emailConfValidator.addValidation(binding.code2, RegexTemplate.NOT_EMPTY, getResources().getString(R.string.invalid_email_conf_code));
        emailConfValidator.addValidation(binding.code3, RegexTemplate.NOT_EMPTY, getResources().getString(R.string.invalid_email_conf_code));
        emailConfValidator.addValidation(binding.code4, RegexTemplate.NOT_EMPTY, getResources().getString(R.string.invalid_email_conf_code));
    }

    private void prepareComponents() {
        binding.revealPassword.setOnClickListener(this);
        binding.cancelVeri.setOnClickListener(this);
        binding.resend.setOnClickListener(this);
        binding.checkVeri.setOnClickListener(this);
        binding.registerBtn.setOnClickListener(this);
        binding.gotoLogin.setOnClickListener(this);
        binding.code1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() == 1) {
                    binding.code1.clearFocus();
                    binding.code2.requestFocus();
                }
            }
        });
        binding.code2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() == 1) {
                    binding.code2.clearFocus();
                    binding.code3.requestFocus();
                }
            }
        });
        binding.code3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() == 1) {
                    binding.code3.clearFocus();
                    binding.code4.requestFocus();
                }
            }
        });
        binding.code4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() == 1) {
                    binding.code4.clearFocus();
                    binding.confirmEmailBtn.requestFocus();
                }
            }
        });
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.registerBtn.setEnabled(false);
        binding.confirmEmailBtn.setEnabled(false);
    }

    private void showForm() {
        binding.confirmEmailForm.setVisibility(View.GONE);
        binding.signupForm.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);
        binding.registerBtn.setEnabled(true);
    }

    private void showConfForm() {
        binding.signupForm.setVisibility(View.GONE);
        binding.confirmEmailForm.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);
        binding.confirmEmailBtn.setEnabled(true);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.registerBtn) {
            registerValidator.clear();
            if (registerValidator.validate())
                register();
        } else if (viewId == R.id.gotoLogin) {
            gotoLogin();
        } else if (viewId == R.id.confirmEmailBtn) {
            sendConfirmation();
        } else if (viewId == R.id.resend) {
            resendEmail();
        } else if (viewId == R.id.checkVeri) {
            login(true);
        } else if (viewId == R.id.cancelVeri) {
            removePrevInfo();
            showForm();
        } else if (viewId == R.id.revealPassword) {
            revealOrHidePassword(view);
        }
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

    private void resendEmail() {
        showLoading();

        UserCredentials userCredentials = new UserCredentials(currentUsername);

        String jsonCredentials = moshi.adapter(UserCredentials.class).toJson(userCredentials);

        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), jsonCredentials);

        Call<InfoMessage> resendCall;
        resendCall = service.resendVerification(requestBody);
        resendCall.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Đã gửi lại email xác thực", Toast.LENGTH_LONG).show();
                    showConfForm();
                }
            }

            @Override
            public void onFailure(Call<InfoMessage> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Không thể gửi email xác thực", Toast.LENGTH_LONG).show();
                showConfForm();
            }
        });
    }

    private void login(boolean stay) {
        showLoading();

        UserCredentials userCredentials = new UserCredentials(currentUsername, currentHash);

        String jsonCredentials = moshi.adapter(UserCredentials.class).toJson(userCredentials);

        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), jsonCredentials);

        Call<TokenPair> loginCall;
        loginCall = service.login(requestBody);
        loginCall.enqueue(new Callback<TokenPair>() {
            @Override
            public void onResponse(Call<TokenPair> call, Response<TokenPair> response) {

                Log.w("YellLogin", "onResponse: " + response);

                if (response.isSuccessful()) {
                    sessionManager.saveToken(response.body());
                    sharedPreferences.edit().putString("uid",currentUsername);
                    sharedPreferences.edit().apply();
                    removePrevInfo();
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                } else {
                    if (stay) {
                        Toast.makeText(RegisterActivity.this, "Email chưa được xác thực. Vui lòng thử lại", Toast.LENGTH_LONG).show();
                        showConfForm();
                    }
                    else {
                        removePrevInfo();
                        gotoLogin();
                        Toast.makeText(RegisterActivity.this, "Email chưa được xác thực. Vui lòng thử đăng nhập lại", Toast.LENGTH_LONG).show();
                    }
                }

            }

            @Override
            public void onFailure(Call<TokenPair> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Lỗi khi kết nối với server", Toast.LENGTH_LONG).show();
                showForm();
            }
        });
    }

    private void sendConfirmation() {
        String code = "";

        emailConfValidator.clear();
        if (!emailConfValidator.validate()) {
            return;
        }

        code += binding.code1.getText().toString();
        code += binding.code2.getText().toString();
        code += binding.code3.getText().toString();
        code += binding.code4.getText().toString();

        EmailConfirmation emailConfirmation = new EmailConfirmation(currentUsername, currentEmail, code);

        String bodyString = moshi.adapter(EmailConfirmation.class).toJson(emailConfirmation);

        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), bodyString);

        call = service.verify(requestBody);
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {
                if (response.isSuccessful()) {
                    showLoading();
                    Toast.makeText(RegisterActivity.this, "Đã xác thực email thành công!", Toast.LENGTH_LONG).show();
                    login(false);
                } else {
                    showConfForm();
                    Toast.makeText(RegisterActivity.this, "Mã bảo mật không chính xác", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<InfoMessage> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Lỗi khi kết nối với server", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void gotoLogin() {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }

    private void register() {
        currentUsername = binding.usernameInput.getText().toString();
        String password = binding.passwordInput.getText().toString();
        String name = binding.nameInput.getText().toString();
        currentEmail = binding.emailInput.getText().toString();

        currentHash = Hashing.sha256()
                .hashString(password, StandardCharsets.UTF_8)
                .toString();

        UserCredentials userCredentials = new UserCredentials(currentUsername, currentHash, currentEmail, name);

        String jsonCredentials = moshi.adapter(UserCredentials.class).toJson(userCredentials);

        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), jsonCredentials);

        showLoading();
        try {
            call = service.register(requestBody);
        }
        catch (Exception e)
        {
            Log.e("YellSignup", e.toString());
        }
        call.enqueue(new Callback<InfoMessage>() {
            @Override
            public void onResponse(Call<InfoMessage> call, Response<InfoMessage> response) {

                Log.w("YellSignup", "onResponse: " + response);

                if (response.isSuccessful()) {
                    confirmEmail();
                }
                else {
                    if (response.code() == 401) {
                        ErrorMessage apiError = ErrorMessage.convertErrors(response.errorBody());
                        Toast.makeText(RegisterActivity.this, apiError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    showForm();
                }

            }

            @Override
            public void onFailure(Call<InfoMessage> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Lỗi khi kết nối với server", Toast.LENGTH_LONG).show();
                showForm();
            }
        });
    }

    private void confirmEmail() {
        showConfForm();
        String description = "Nhập mã bảo mật có 4 chữ số đã được gửi đến email ";
        String text = description + currentEmail;
        Spannable spannable = new SpannableString(description + currentEmail);

        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.orange)), description.length(), text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.instructionText.setText(spannable);
        binding.confirmEmailBtn.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentUsername != null)
            savePrevInfo();
    }
}