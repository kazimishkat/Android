package com.example.pharmacymanagement.repository;

import android.content.Context;

import com.example.pharmacymanagement.api.ApiClient;
import com.example.pharmacymanagement.api.ApiService;
import com.example.pharmacymanagement.model.request.OnlineOrderRequest;
import com.example.pharmacymanagement.model.response.OnlineOrderResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;


public class OrderRepository {
    private final ApiService apiService;

    public OrderRepository(Context context) {
        apiService = ApiClient.getClient(context);
    }

    // ১. অনলাইন অর্ডার প্লেস করার জন্য
    public void placeOrder(OnlineOrderRequest orderRequest, Callback<ResponseBody> callback) {
        Call<ResponseBody> call = apiService.placeOrder(orderRequest);
        call.enqueue(callback);
    }

    // ২. কাস্টমারের সকল অর্ডার হিস্ট্রি ও ট্র্যাকিং স্ট্যাটাস পাওয়ার জন্য
    public void getOrdersByCustomerId(Long customerId, Callback<List<OnlineOrderResponse>> callback) {
        Call<List<OnlineOrderResponse>> call = apiService.getOrdersByCustomerId(customerId);
        call.enqueue(callback);
    }

    // ৩. একক কোন অর্ডারের বিস্তারিত দেখার জন্য
    public void getOrderDetails(Long orderId, Callback<OnlineOrderResponse> callback) {
        Call<OnlineOrderResponse> call = apiService.getOrderDetails(orderId);
        call.enqueue(callback);
    }

}
