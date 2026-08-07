package com.example.pharmacymanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pharmacymanagement.api.ApiClient;
import com.example.pharmacymanagement.api.ApiService;
import com.example.pharmacymanagement.model.request.CustomerRequest;
import com.example.pharmacymanagement.model.response.CustomerResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etSignupName, etSignupPhone, etSignupEmail, etSignupUsername, etSignupPassword;
    private MaterialButton btnRegister;
    private TextView txtLoginRedirect;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        apiService = ApiClient.getClient(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etSignupName = findViewById(R.id.etSignupName);
        etSignupPhone = findViewById(R.id.etSignupPhone);
        etSignupEmail = findViewById(R.id.etSignupEmail);
        etSignupUsername = findViewById(R.id.etSignupUsername);
        etSignupPassword = findViewById(R.id.etSignupPassword);

        btnRegister = findViewById(R.id.btnRegister);
        txtLoginRedirect = findViewById(R.id.txtLoginRedirect);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> handleSignup());

        txtLoginRedirect.setOnClickListener(v -> finish()); // ব্যাক করে লগইন স্ক্রিনে নিয়ে যাবে
    }

    private void handleSignup() {
        String name = etSignupName.getText().toString().trim();
        String phone = etSignupPhone.getText().toString().trim();
        String email = etSignupEmail.getText().toString().trim();
        String username = etSignupUsername.getText().toString().trim();
        String password = etSignupPassword.getText().toString().trim();

        if (name.isEmpty()) {
            etSignupName.setError("Name is required!");
            etSignupName.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etSignupPhone.setError("Phone is required!");
            etSignupPhone.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etSignupEmail.setError("Email is required!");
            etSignupEmail.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            etSignupUsername.setError("Username is required!");
            etSignupUsername.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            etSignupPassword.setError("Password must be at least 6 characters!");
            etSignupPassword.requestFocus();
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Creating Account...");

        // 🟢 CustomerRequest DTO তৈরি ও মান সেটিং
        CustomerRequest request = new CustomerRequest();
        request.setName(name);
        request.setPhone(phone);
        request.setEmail(email);
        request.setUsername(username);
        request.setPassword(password);
        request.setCreateAccount(true); // 🟢 অ্যাকাউন্ট তৈরির জন্য true করা হলো

        apiService.registerCustomer(request).enqueue(new Callback<CustomerResponse>() {
            @Override
            public void onResponse(Call<CustomerResponse> call, Response<CustomerResponse> response) {
                btnRegister.setEnabled(true);
                btnRegister.setText("SIGN UP");

                if (response.isSuccessful()) {
                    Toast.makeText(SignupActivity.this, "Registration Successful! Please Login.", Toast.LENGTH_LONG).show();
                    finish(); // লগইন পেজে ফিরে যাবে
                } else {
                    Toast.makeText(SignupActivity.this, "Registration Failed! Email or Username might exist.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CustomerResponse> call, Throwable t) {
                btnRegister.setEnabled(true);
                btnRegister.setText("SIGN UP");
                Toast.makeText(SignupActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}