package com.example.pharmacymanagement;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pharmacymanagement.api.ApiClient;
import com.example.pharmacymanagement.api.ApiService;
import com.example.pharmacymanagement.model.request.ForgotPasswordRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etForgotEmail;
    private MaterialButton btnResetPassword;
    private ProgressBar progressBar;
    private TextView txtBackToLogin;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        apiService = ApiClient.getClient(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etForgotEmail = findViewById(R.id.etForgotEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        progressBar = findViewById(R.id.progressBar);
        txtBackToLogin = findViewById(R.id.txtBackToLogin);
    }

    private void setupListeners() {
        btnResetPassword.setOnClickListener(v -> handleForgotPassword());

        txtBackToLogin.setOnClickListener(v -> finish()); // ব্যাক করে লগইন পেজে ফিরিয়ে নেবে
    }

    private void handleForgotPassword() {
        String email = etForgotEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etForgotEmail.setError("Email is required!");
            etForgotEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etForgotEmail.setError("Please enter a valid email!");
            etForgotEmail.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Sending...");

        // 🟢 ForgotPasswordRequest DTO তৈরি
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail(email);

        apiService.forgotPassword(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                btnResetPassword.setEnabled(true);
                btnResetPassword.setText("SEND RESET LINK");

                if (response.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Password reset link sent to your email!",
                            Toast.LENGTH_LONG).show();
                    finish(); // সফল হলে ব্যাক করে লগইন পেজে পাঠিয়ে দিবে
                } else {
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Email not found or reset request failed!",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnResetPassword.setEnabled(true);
                btnResetPassword.setText("SEND RESET LINK");

                Toast.makeText(ForgotPasswordActivity.this,
                        "Network Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}