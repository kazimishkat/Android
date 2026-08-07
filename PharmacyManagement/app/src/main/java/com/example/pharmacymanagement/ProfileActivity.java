package com.example.pharmacymanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pharmacymanagement.api.ApiClient;
import com.example.pharmacymanagement.api.ApiService;
import com.example.pharmacymanagement.model.request.ChangePasswordRequest;
import com.example.pharmacymanagement.model.response.CustomerResponse;
import com.example.pharmacymanagement.session.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    /* =================================================================
     * UI VIEW DECLARATIONS
     * ================================================================= */
    private ImageButton btnBack;
    private MaterialButton btnToggleEdit, btnSaveProfile, btnCancelEdit, btnChangePassword;
    private TextView txtHeaderName, txtHeaderEmail, txtLoyaltyPoints;
    private TextInputEditText etProfileName, etProfileEmail, etProfilePhone;
    private TextInputEditText etNewPassword;
    private LinearLayout layoutEditActions;

    /* =================================================================
     * SERVICES & DATA
     * ================================================================= */
    private SessionManager sessionManager;
    private ApiService apiService;
    private CustomerResponse currentCustomer;

    /* =================================================================
     * ACTIVITY LIFECYCLE
     * ================================================================= */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient(this);

        initViews();
        setupListeners();
        loadProfileData();
    }

    /* =================================================================
     * VIEW INITIALIZATION
     * ================================================================= */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnToggleEdit = findViewById(R.id.btnToggleEdit);

        txtHeaderName = findViewById(R.id.txtHeaderName);
        txtHeaderEmail = findViewById(R.id.txtHeaderEmail);
        txtLoyaltyPoints = findViewById(R.id.txtLoyaltyPoints);

        etProfileName = findViewById(R.id.etProfileName);
        etProfileEmail = findViewById(R.id.etProfileEmail);
        etProfilePhone = findViewById(R.id.etProfilePhone);

        layoutEditActions = findViewById(R.id.layoutEditActions);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);

        etNewPassword = findViewById(R.id.etNewPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        btnToggleEdit.setOnClickListener(v -> enableEditMode(true));

        btnCancelEdit.setOnClickListener(v -> {
            loadProfileData(); // Reset form values
            enableEditMode(false);
        });

        btnSaveProfile.setOnClickListener(v -> handleProfileUpdate());

        btnChangePassword.setOnClickListener(v -> handlePasswordChange());
    }

    /* =================================================================
     * UI EDIT MODE TOGGLE
     * ================================================================= */
    private void enableEditMode(boolean enable) {
        etProfileName.setEnabled(enable);
        etProfilePhone.setEnabled(enable);

        if (enable) {
            btnToggleEdit.setVisibility(View.GONE);
            layoutEditActions.setVisibility(View.VISIBLE);
            etProfileName.requestFocus();
        } else {
            btnToggleEdit.setVisibility(View.VISIBLE);
            layoutEditActions.setVisibility(View.GONE);
        }
    }

    /* =================================================================
     * LOAD DATA FROM SESSION / API
     * ================================================================= */
    private void loadProfileData() {
        currentCustomer = sessionManager.getCustomer();

        if (currentCustomer != null) {
            bindDataToUI(currentCustomer);
        } else if (sessionManager.getUser() != null && sessionManager.getUser().getUserId() != null) {
            // If Session customer is null, fetch from API using User ID
            apiService.getCustomerByUserId(sessionManager.getUser().getUserId()).enqueue(new Callback<CustomerResponse>() {
                @Override
                public void onResponse(Call<CustomerResponse> call, Response<CustomerResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentCustomer = response.body();
                        sessionManager.saveCustomer(currentCustomer);
                        bindDataToUI(currentCustomer);
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed to fetch profile info!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<CustomerResponse> call, Throwable t) {
                    Toast.makeText(ProfileActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void bindDataToUI(CustomerResponse customer) {
        String name = customer.getName() != null ? customer.getName() : "";
        String email = customer.getEmail() != null ? customer.getEmail() : "";
        String phone = customer.getPhone() != null ? customer.getPhone() : "";
        int points = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;

        txtHeaderName.setText(name);
        txtHeaderEmail.setText(email);
        txtLoyaltyPoints.setText("Loyalty Points: " + points);

        etProfileName.setText(name);
        etProfileEmail.setText(email);
        etProfilePhone.setText(phone);
    }

    /* =================================================================
     * UPDATE PROFILE LOGIC
     * ================================================================= */
    private void handleProfileUpdate() {
        if (currentCustomer == null || currentCustomer.getId() == null) {
            Toast.makeText(this, "Invalid customer data!", Toast.LENGTH_SHORT).show();
            return;
        }

        String updatedName = etProfileName.getText().toString().trim();
        String updatedPhone = etProfilePhone.getText().toString().trim();

        if (updatedName.isEmpty()) {
            etProfileName.setError("Name is required!");
            etProfileName.requestFocus();
            return;
        }

        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving...");

        currentCustomer.setName(updatedName);
        currentCustomer.setPhone(updatedPhone);

        apiService.updateCustomerProfile(currentCustomer.getId(), currentCustomer).enqueue(new Callback<CustomerResponse>() {
            @Override
            public void onResponse(Call<CustomerResponse> call, Response<CustomerResponse> response) {
                btnSaveProfile.setEnabled(true);
                btnSaveProfile.setText("Save Changes");

                if (response.isSuccessful() && response.body() != null) {
                    CustomerResponse updated = response.body();
                    sessionManager.saveCustomer(updated);
                    currentCustomer = updated;

                    bindDataToUI(updated);
                    enableEditMode(false);

                    Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to update profile!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CustomerResponse> call, Throwable t) {
                btnSaveProfile.setEnabled(true);
                btnSaveProfile.setText("Save Changes");
                Toast.makeText(ProfileActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* =================================================================
     * CHANGE PASSWORD LOGIC
     * ================================================================= */
    private void handlePasswordChange() {
        String newPass = etNewPassword.getText().toString().trim();

        if (newPass.isEmpty() || newPass.length() < 6) {
            etNewPassword.setError("New password must be at least 6 characters!");
            etNewPassword.requestFocus();
            return;
        }

        btnChangePassword.setEnabled(false);
        btnChangePassword.setText("Updating...");

        ChangePasswordRequest passwordRequest = new ChangePasswordRequest();
        passwordRequest.setToken(sessionManager.getToken()); // Passing session token
        passwordRequest.setNewPassword(newPass);

        apiService.changePassword(passwordRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnChangePassword.setEnabled(true);
                btnChangePassword.setText("Update Password");

                if (response.isSuccessful()) {
                    etNewPassword.setText("");
                    Toast.makeText(ProfileActivity.this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to update password!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnChangePassword.setEnabled(true);
                btnChangePassword.setText("Update Password");
                Toast.makeText(ProfileActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}