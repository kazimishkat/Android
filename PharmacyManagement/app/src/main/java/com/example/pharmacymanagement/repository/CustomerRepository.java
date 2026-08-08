package com.example.pharmacymanagement.repository;

import android.content.Context;


import com.example.pharmacymanagement.api.ApiClient;
import com.example.pharmacymanagement.api.ApiService;
import com.example.pharmacymanagement.model.request.ChangePasswordRequest;
import com.example.pharmacymanagement.model.response.CustomerResponse;
import com.example.pharmacymanagement.session.SessionManager;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class CustomerRepository {
    private final ApiService apiService;
    private final SessionManager sessionManager;

    /* =================================================================
     * CONSTRUCTOR & INITIALIZATION
     * ================================================================= */

    public CustomerRepository(Context context) {
        this.apiService = ApiClient.getClient(context);
        this.sessionManager = new SessionManager(context);
    }


    /* =================================================================
     * LOCAL SESSION DATA ACCESS
     * ================================================================= */

    public CustomerResponse getLocalCustomer() {
        return sessionManager.getCustomer();
    }


    /* =================================================================
     * REMOTE CUSTOMER PROFILE MANAGEMENT METHODS
     * ================================================================= */

    public void getCustomerByUserId(Long userId, Callback<CustomerResponse> callback) {
        Call<CustomerResponse> call = apiService.getCustomerByUserId(userId);
        call.enqueue(callback);
    }

    public void updateCustomerProfile(Long customerId, okhttp3.RequestBody customerJson, okhttp3.MultipartBody.Part image, retrofit2.Callback<CustomerResponse> callback) {
        retrofit2.Call<CustomerResponse> call = apiService.updateCustomerProfile(customerId, customerJson, image);
        call.enqueue(callback);
    }

    public void changePassword(ChangePasswordRequest passwordRequest, Callback<ResponseBody> callback) {
        Call<ResponseBody> call = apiService.changePassword(passwordRequest);
        call.enqueue(callback);
    }
    }

