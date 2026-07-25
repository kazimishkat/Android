package com.example.pharmacymanagement.api;

import com.example.pharmacymanagement.model.request.LoginRequest;
import com.example.pharmacymanagement.model.response.CustomerResponse;
import com.example.pharmacymanagement.model.response.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/customer/user/{id}")
    Call<CustomerResponse> getCustomerByUserId(@Path("id") Long id);
}
