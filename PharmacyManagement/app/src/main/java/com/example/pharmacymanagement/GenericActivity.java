package com.example.pharmacymanagement;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmacymanagement.adapter.GenericAdapter;
import com.example.pharmacymanagement.api.ApiClient;
import com.example.pharmacymanagement.api.ApiService;
import com.example.pharmacymanagement.model.response.GenericMedicineResponse;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenericActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextInputEditText etSearchGeneric;
    private RecyclerView recyclerGenerics;
    private LinearLayout layoutEmptyState;

    private GenericAdapter genericAdapter;
    private List<GenericMedicineResponse> masterGenericList = new ArrayList<>();
    private List<GenericMedicineResponse> filteredGenericList = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic);

        initViews();
        setupRecyclerView();
        setupListeners();

        apiService = ApiClient.getClient(this);

        fetchDynamicGenerics();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etSearchGeneric = findViewById(R.id.etSearchGeneric);
        recyclerGenerics = findViewById(R.id.recyclerGenerics);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
    }

    private void setupRecyclerView() {
        genericAdapter = new GenericAdapter(filteredGenericList, genericMedicine -> {
            // 🟢 ক্লিকে মেডিসিন লিস্টে জেনরিক নাম পাঠিয়ে ফিল্টার করা
            Intent intent = new Intent(GenericActivity.this, MedicineList.class);
            intent.putExtra("SEARCH_GENERIC", genericMedicine.getGenericName());
            startActivity(intent);
        });

        recyclerGenerics.setLayoutManager(new LinearLayoutManager(this));
        recyclerGenerics.setAdapter(genericAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        etSearchGeneric.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterGenerics(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchDynamicGenerics() {
        // ApiService-এ getAllGenericMedicines() এনডপয়েন্ট কল করা
        apiService.getAllGenericMedicines().enqueue(new Callback<List<GenericMedicineResponse>>() {
            @Override
            public void onResponse(Call<List<GenericMedicineResponse>> call, Response<List<GenericMedicineResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    masterGenericList.clear();
                    masterGenericList.addAll(response.body());

                    filteredGenericList.clear();
                    filteredGenericList.addAll(masterGenericList);

                    genericAdapter.notifyDataSetChanged();
                    updateUIState();
                } else {
                    Toast.makeText(GenericActivity.this, "Failed to load generics!", Toast.LENGTH_SHORT).show();
                    updateUIState();
                }
            }

            @Override
            public void onFailure(Call<List<GenericMedicineResponse>> call, Throwable t) {
                Toast.makeText(GenericActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateUIState();
            }
        });
    }

    private void filterGenerics(String query) {
        filteredGenericList.clear();
        if (query.isEmpty()) {
            filteredGenericList.addAll(masterGenericList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (GenericMedicineResponse item : masterGenericList) {
                String gName = item.getGenericName() != null ? item.getGenericName().toLowerCase() : "";
                String cName = item.getCategoryName() != null ? item.getCategoryName().toLowerCase() : "";

                if (gName.contains(lowerQuery) || cName.contains(lowerQuery)) {
                    filteredGenericList.add(item);
                }
            }
        }
        genericAdapter.notifyDataSetChanged();
        updateUIState();
    }

    private void updateUIState() {
        if (filteredGenericList.isEmpty()) {
            recyclerGenerics.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerGenerics.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }
}