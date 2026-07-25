package com.example.pharmacymanagement.repository;

import android.content.Context;


import com.example.pharmacymanagement.api.ApiClient;
import com.example.pharmacymanagement.api.ApiService;
import com.example.pharmacymanagement.model.response.CustomerResponse;

import retrofit2.Call;
import retrofit2.Callback;

public class CustomerRepository {
    private final ApiService apiService;

    public CustomerRepository(Context context) {
        apiService = ApiClient.getClient(context);
    }

    public void getCustomerByUserId(Long userId,
                                    Callback<CustomerResponse> callback) {

        Call<CustomerResponse> call =
                apiService.getCustomerByUserId(userId);

        call.enqueue(callback);

    }
}
