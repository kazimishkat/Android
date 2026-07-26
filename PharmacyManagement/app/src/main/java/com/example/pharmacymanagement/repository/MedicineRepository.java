package com.example.pharmacymanagement.repository;

import android.content.Context;

import com.example.pharmacymanagement.api.ApiClient;
import com.example.pharmacymanagement.api.ApiService;
import com.example.pharmacymanagement.model.response.MedicineResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class MedicineRepository {
    private final ApiService apiService;

    public MedicineRepository(Context context) {
        apiService = ApiClient.getClient(context);
    }

    // ১. সকল মেডিসিন ফেচ করার জন্য
    public void getAllMedicines(Callback<List<MedicineResponse>> callback) {
        Call<List<MedicineResponse>> call = apiService.getAllMedicines();
        call.enqueue(callback);
    }

    // ২. নাম দিয়ে মেডিসিন ফিল্টার/সার্চ করার জন্য
    public void searchMedicines(String query, Callback<List<MedicineResponse>> callback) {
        Call<List<MedicineResponse>> call = apiService.searchMedicines(query);
        call.enqueue(callback);
    }

    // ৩. নির্দিষ্ট আইডি দিয়ে একটি মেডিসিনের ডিটেইলস পাওয়ার জন্য
    public void getMedicineById(Long medicineId, Callback<MedicineResponse> callback) {
        Call<MedicineResponse> call = apiService.getMedicineById(medicineId);
        call.enqueue(callback);
    }
}
