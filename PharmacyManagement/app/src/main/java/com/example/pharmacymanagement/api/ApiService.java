package com.example.pharmacymanagement.api;

import com.example.pharmacymanagement.model.request.LoginRequest;
import com.example.pharmacymanagement.model.request.OnlineOrderRequest;
import com.example.pharmacymanagement.model.response.CustomerResponse;
import com.example.pharmacymanagement.model.response.LoginResponse;
import com.example.pharmacymanagement.model.response.MedicineResponse;
import com.example.pharmacymanagement.model.response.OnlineOrderResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // --- Authentication & User Endpoints ---
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/customer/user/{id}")
    Call<CustomerResponse> getCustomerByUserId(@Path("id") Long id);

    // --- Medicine Endpoints ---

    // ১. সকল মেডিসিনের তালিকা পাওয়ার জন্য
    @GET("api/medicines")
    Call<List<MedicineResponse>> getAllMedicines();

    // ২. নির্দিষ্ট আইডি দিয়ে একটি মেডিসিনের ডিটেইলস পাওয়ার জন্য (ঐচ্ছিক)
    @GET("api/medicines/{id}")
    Call<MedicineResponse> getMedicineById(@Path("id") Long medicineId);

    // ৩. নাম দিয়ে ওষুধ সার্চ করার জন্য (Search Bar-এর কাজ করার জন্য)
    @GET("api/medicines/search")
    Call<List<MedicineResponse>> searchMedicines(@Query("query") String query);


    // --- Order & Tracking Endpoints ---

    // ৪. নতুন অর্ডার প্লেস করার জন্য
    @POST("api/orders/place")
    Call<ResponseBody> placeOrder(@Body OnlineOrderRequest request);

    // ৫. নির্দিষ্ট কাস্টমারের সব অর্ডার লিস্ট/হিস্ট্রি পাওয়ার জন্য (Order Tracking)
    @GET("api/orders/customer/{customerId}")
    Call<List<OnlineOrderResponse>> getOrdersByCustomerId(@Path("customerId") Long customerId);

    // ৬. নির্দিষ্ট একটি অর্ডারের রিয়েলটাইম স্ট্যাটাস/ডিটেইলস জানার জন্য (ঐচ্ছিক)
    @GET("api/orders/{orderId}")
    Call<OnlineOrderResponse> getOrderDetails(@Path("orderId") Long orderId);


}
