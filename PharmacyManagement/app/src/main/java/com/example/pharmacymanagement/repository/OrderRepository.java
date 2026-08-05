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

    /* =================================================================
     * CONSTRUCTOR & INITIALIZATION
     * ================================================================= */

    public OrderRepository(Context context) {
        this.apiService = ApiClient.getClient(context);
    }


    /* =================================================================
     * ORDER CREATION & MANAGEMENT METHODS
     * ================================================================= */

    public void placeOrder(OnlineOrderRequest orderRequest, Long customerId, String transactionId, Callback<OnlineOrderResponse> callback) {
        Call<OnlineOrderResponse> call = apiService.placeOrder(orderRequest, customerId, transactionId);
        call.enqueue(callback);
    }

    public void cancelOrder(Long orderId, Callback<OnlineOrderResponse> callback) {
        Call<OnlineOrderResponse> call = apiService.cancelOrder(orderId);
        call.enqueue(callback);
    }


    /* =================================================================
     * ORDER FETCHING & TRACKING METHODS
     * ================================================================= */

    public void getOrdersByCustomerId(Long customerId, Callback<List<OnlineOrderResponse>> callback) {
        Call<List<OnlineOrderResponse>> call = apiService.getCustomerOrderHistory(customerId);
        call.enqueue(callback);
    }

    public void getMyOrders(Callback<List<OnlineOrderResponse>> callback) {
        Call<List<OnlineOrderResponse>> call = apiService.getMyOrders();
        call.enqueue(callback);
    }

    public void getOrderDetails(Long orderId, Callback<OnlineOrderResponse> callback) {
        Call<OnlineOrderResponse> call = apiService.getOrderById(orderId);
        call.enqueue(callback);
    }

    public void trackOrder(String orderNumber, Callback<OnlineOrderResponse> callback) {
        Call<OnlineOrderResponse> call = apiService.trackOrderByNumber(orderNumber);
        call.enqueue(callback);
    }

}
