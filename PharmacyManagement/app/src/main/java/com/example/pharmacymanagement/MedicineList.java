package com.example.pharmacymanagement;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmacymanagement.adapter.MedicineAdapter;
import com.example.pharmacymanagement.api.ApiClient;
import com.example.pharmacymanagement.api.ApiService;
import com.example.pharmacymanagement.model.response.MedicineResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedicineList extends AppCompatActivity {

    /* =================================================================
     * UI VIEW DECLARATIONS
     * ================================================================= */
    private ImageButton btnBack;
    private EditText etSearchMedicine;
    private RecyclerView recyclerMedicines;
    private LinearLayout layoutEmptyState;

    /* =================================================================
     * ADAPTER, DATA & SERVICES
     * ================================================================= */
    private MedicineAdapter medicineAdapter;
    private List<MedicineResponse> masterMedicineList;   // মূল মেডিসিন লিস্ট
    private List<MedicineResponse> filteredMedicineList; // সার্চের পর দেখানোর লিস্ট
    private ApiService apiService;

    /* =================================================================
     * ACTIVITY LIFECYCLE
     * ================================================================= */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_list);

        initViews();
        setupRecyclerView();
        setupListeners();

        // ApiClient initialization with context
        apiService = ApiClient.getClient(this);

        // 🟢 GenericActivity
        String searchGeneric = getIntent().getStringExtra("SEARCH_GENERIC");
        if (searchGeneric != null && !searchGeneric.trim().isEmpty()) {
            etSearchMedicine.setText(searchGeneric.trim());
        }

        fetchMedicineCatalog();
    }

    /* =================================================================
     * VIEW INITIALIZATION & RECYCLERVIEW SETUP
     * ================================================================= */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etSearchMedicine = findViewById(R.id.etSearchMedicine);
        recyclerMedicines = findViewById(R.id.recyclerMedicines);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
    }

    private void setupRecyclerView() {
        masterMedicineList = new ArrayList<>();
        filteredMedicineList = new ArrayList<>();

        medicineAdapter = new MedicineAdapter(this, filteredMedicineList, new MedicineAdapter.OnMedicineClickListener() {
            @Override
            public void onMedicineClick(MedicineResponse medicine) {
                // Item Click Listener
            }

            @Override
            public void onAddToCartClick(MedicineResponse medicine) {
                Toast.makeText(MedicineList.this, medicine.getBrandName() + " added to cart", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerMedicines.setLayoutManager(new LinearLayoutManager(this));
        recyclerMedicines.setAdapter(medicineAdapter);
    }

    /* =================================================================
     * EVENT LISTENERS & REAL-TIME SEARCH
     * ================================================================= */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // EditText Search Listener
        etSearchMedicine.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMedicines(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /* =================================================================
     * API FETCHING & SEARCH FILTERING LOGIC
     * ================================================================= */
    private void fetchMedicineCatalog() {
        apiService.getAllMedicines().enqueue(new Callback<List<MedicineResponse>>() {
            @Override
            public void onResponse(Call<List<MedicineResponse>> call, Response<List<MedicineResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    masterMedicineList.clear();
                    masterMedicineList.addAll(response.body());

                    // টেক্সট ফিল্ডের বর্তমান ভ্যালু দিয়ে অটোমেটিক ফিল্টার করা
                    filterMedicines(etSearchMedicine.getText().toString().trim());
                } else {
                    showEmptyState();
                    Toast.makeText(MedicineList.this, "Failed to load medicines", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MedicineResponse>> call, Throwable t) {
                showEmptyState();
                Toast.makeText(MedicineList.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterMedicines(String query) {
        filteredMedicineList.clear();

        if (query.isEmpty()) {
            filteredMedicineList.addAll(masterMedicineList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (MedicineResponse med : masterMedicineList) {
                String brand = med.getBrandName() != null ? med.getBrandName().toLowerCase() : "";
                String generic = med.getGenericName() != null ? med.getGenericName().toLowerCase() : "";

                if (brand.contains(lowerCaseQuery) || generic.contains(lowerCaseQuery)) {
                    filteredMedicineList.add(med);
                }
            }
        }

        medicineAdapter.notifyDataSetChanged();

        if (filteredMedicineList.isEmpty()) {
            showEmptyState();
        } else {
            showDataState();
        }
    }

    /* =================================================================
     * UI STATE MANAGEMENT HELPER METHODS
     * ================================================================= */
    private void showDataState() {
        recyclerMedicines.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        recyclerMedicines.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
    }
}