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

    /* =================================================================
     * CONSTRUCTOR & INITIALIZATION
     * ================================================================= */

    public MedicineRepository(Context context) {
        this.apiService = ApiClient.getClient(context);
    }


    /* =================================================================
     * MEDICINE FETCHING & SEARCH METHODS
     * ================================================================= */

    public void getAllMedicines(Callback<List<MedicineResponse>> callback) {
        Call<List<MedicineResponse>> call = apiService.getAllMedicines();
        call.enqueue(callback);
    }

    public void getMedicineById(Long medicineId, Callback<MedicineResponse> callback) {
        Call<MedicineResponse> call = apiService.getMedicineById(medicineId);
        call.enqueue(callback);
    }

    public void searchMedicinesByBrandName(String brandName, Callback<List<MedicineResponse>> callback) {
        Call<List<MedicineResponse>> call = apiService.searchMedicinesByBrandName(brandName);
        call.enqueue(callback);
    }
}
