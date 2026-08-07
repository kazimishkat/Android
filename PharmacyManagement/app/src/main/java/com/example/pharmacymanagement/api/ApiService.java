package com.example.pharmacymanagement.api;

import com.example.pharmacymanagement.model.request.ChangePasswordRequest;
import com.example.pharmacymanagement.model.request.CustomerRequest;
import com.example.pharmacymanagement.model.request.ForgotPasswordRequest;
import com.example.pharmacymanagement.model.request.LoginRequest;
import com.example.pharmacymanagement.model.request.OnlineOrderRequest;
import com.example.pharmacymanagement.model.response.CustomerResponse;
import com.example.pharmacymanagement.model.response.GenericMedicineResponse;
import com.example.pharmacymanagement.model.response.LoginResponse;
import com.example.pharmacymanagement.model.response.MedicineResponse;
import com.example.pharmacymanagement.model.response.OnlineOrderResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    /* =================================================================
     * 1. AUTHENTICATION & CUSTOMER PROFILE ENDPOINTS
     * ================================================================= */
    @POST("api/auth/register-customer")
    Call<CustomerResponse> registerCustomer(@Body CustomerRequest request);
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/customers/user/{userId}")
    Call<CustomerResponse> getCustomerByUserId(@Path("userId") Long userId);

    @PUT("api/customers/{id}")
    Call<CustomerResponse> updateCustomerProfile(@Path("id") Long id, @Body CustomerResponse customer);

    @POST("api/auth/change-password")
    Call<ResponseBody> changePassword(@Body ChangePasswordRequest request);

    @POST("api/auth/forgot-password")
    Call<ResponseBody> forgotPassword(@Body ForgotPasswordRequest request);


    /* =================================================================
     * 2. MEDICINE DIRECTORY & SEARCH ENDPOINTS
     * ================================================================= */

    @GET("api/medicines")
    Call<List<MedicineResponse>> getAllMedicines();

    @GET("api/medicines/{id}")
    Call<MedicineResponse> getMedicineById(@Path("id") Long id);

    @GET("api/medicines/search")
    Call<List<MedicineResponse>> searchMedicinesByBrandName(@Query("brandName") String brandName);


    /* =================================================================
     * 3. ONLINE ORDER & TRACKING ENDPOINTS
     * ================================================================= */

    @POST("api/online-orders/place")
    Call<OnlineOrderResponse> placeOrder(
            @Body OnlineOrderRequest request,
            @Query("customerId") Long customerId,
            @Query("transactionId") String transactionId
    );

    @GET("api/online-orders/customer/{customerId}/history")
    Call<List<OnlineOrderResponse>> getCustomerOrderHistory(@Path("customerId") Long customerId);

    @GET("api/online-orders/my-orders")
    Call<List<OnlineOrderResponse>> getMyOrders();

    @GET("api/online-orders/track/{orderNumber}")
    Call<OnlineOrderResponse> trackOrderByNumber(@Path("orderNumber") String orderNumber);

    @GET("api/online-orders/{id}")
    Call<OnlineOrderResponse> getOrderById(@Path("id") Long id);

    @GET("api/online-orders")
    Call<List<OnlineOrderResponse>> getAllOrders();

    @PATCH("api/online-orders/{orderId}/cancel")
    Call<OnlineOrderResponse> cancelOrder(@Path("orderId") Long orderId);

    /* =================================================================
     * 4. BRANCH & INVENTORY ENDPOINTS
     * ================================================================= */

    // NEWLY ADDED
    @GET("api/branches")
    Call<List<com.example.pharmacymanagement.model.response.BranchResponse>> getAllBranches();

    // NEWLY ADDED
    @GET("api/branch-inventories/branch/{branchId}")
    Call<List<com.example.pharmacymanagement.model.response.BranchInventoryResponse>> getInventoryByBranch(@Path("branchId") Long branchId);

    /* =================================================================
     * 5. GENERIC ENDPOINTS
     * ================================================================= */
    @GET("/api/generic-medicines") // আপনার ব্যাকএন্ডের আসল জেনরিক API Endpoint Path
    Call<List<GenericMedicineResponse>> getAllGenericMedicines();


}
