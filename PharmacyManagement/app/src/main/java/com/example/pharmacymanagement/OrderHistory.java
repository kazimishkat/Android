package com.example.pharmacymanagement;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmacymanagement.adapter.OrderAdapter;
import com.example.pharmacymanagement.api.ApiClient;
import com.example.pharmacymanagement.api.ApiService;
import com.example.pharmacymanagement.model.response.OnlineOrderResponse;
import com.example.pharmacymanagement.enums.OnlineOrderStatus; // NEWLY ADDED
import com.example.pharmacymanagement.session.SessionManager; // NEWLY ADDED
import com.example.pharmacymanagement.model.response.CustomerResponse; // NEWLY ADDED
import com.google.android.material.chip.ChipGroup;
import androidx.appcompat.widget.Toolbar; // NEWLY ADDED

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistory extends AppCompatActivity {
    /* =================================================================
     * UI VIEW DECLARATIONS
     * ================================================================= */
    private ImageButton btnBack, btnRefresh;
    private EditText edtSearch;
    private ChipGroup chipGroupStatus;
    private RecyclerView recyclerOrders;
    private LinearLayout layoutEmptyState;
    private ProgressBar progressLoading;

    /* =================================================================
     * ADAPTER, DATA & SERVICES
     * ================================================================= */
    private OrderAdapter orderAdapter;
    private List<OnlineOrderResponse> masterOrderList; // আসল সব ডেটা জমা থাকবে
    private List<OnlineOrderResponse> filteredOrderList; // ফিল্টার হওয়া ডেটা
    private ApiService apiService;
    private SessionManager sessionManager; // NEWLY ADDED

    /* =================================================================
     * ACTIVITY LIFECYCLE
     * ================================================================= */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        // NEWLY ADDED
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("");
        }

        initViews();
        setupRecyclerView();
        setupListeners();

        apiService = ApiClient.getClient(this);
        sessionManager = new SessionManager(this); // NEWLY ADDED

        fetchOrderHistory();
    }

    /* =================================================================
     * VIEW INITIALIZATION & RECYCLERVIEW SETUP
     * ================================================================= */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnRefresh = findViewById(R.id.btnRefresh);
        edtSearch = findViewById(R.id.edtSearch);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
        recyclerOrders = findViewById(R.id.recyclerOrders);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        progressLoading = findViewById(R.id.progressLoading);
    }

    private void setupRecyclerView() {
        masterOrderList = new ArrayList<>();
        filteredOrderList = new ArrayList<>();

        orderAdapter = new OrderAdapter(filteredOrderList, order -> {
            String orderNo = order.getOrderNumber() != null ? order.getOrderNumber() : "#" + order.getId();
            Toast.makeText(this, "Order Selected: " + orderNo, Toast.LENGTH_SHORT).show();
        });

        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(orderAdapter);
    }

    /* =================================================================
     * EVENT LISTENERS (SEARCH, CHIPS & ACTIONS)
     * ================================================================= */
    private void setupListeners() {
        // NEWLY ADDED
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        btnRefresh.setOnClickListener(v -> fetchOrderHistory());

        // Real-time Search Filter
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Status Filter Chips Handler
        chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> applyFilters());
    }

    /* =================================================================
     * API CALLS & DATA FETCHING
     * ================================================================= */
    private void fetchOrderHistory() {
        showLoadingState();

        // NEWLY ADDED
        CustomerResponse customer = sessionManager.getCustomer();
        if (customer == null || customer.getId() == null) {
            hideLoadingState();
            showEmptyState();
            Toast.makeText(OrderHistory.this, "Customer session not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // NEWLY ADDED
        apiService.getCustomerOrderHistory(customer.getId()).enqueue(new Callback<List<OnlineOrderResponse>>() {
            @Override
            public void onResponse(Call<List<OnlineOrderResponse>> call, Response<List<OnlineOrderResponse>> response) {
                hideLoadingState();

                if (response.isSuccessful() && response.body() != null) {
                    masterOrderList.clear();
                    masterOrderList.addAll(response.body());

                    // বর্তমান ফিল্টার অনুযায়ী লিস্ট রেন্ডার করা
                    applyFilters();
                } else {
                    showEmptyState();
                    Toast.makeText(OrderHistory.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<OnlineOrderResponse>> call, Throwable t) {
                hideLoadingState();
                showEmptyState();
                Toast.makeText(OrderHistory.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* =================================================================
     * SEARCH & CHIP FILTERING LOGIC
     * ================================================================= */
    private void applyFilters() {
        String searchQuery = edtSearch.getText().toString().trim().toLowerCase();
        int checkedChipId = chipGroupStatus.getCheckedChipId();

        filteredOrderList.clear();

        for (OnlineOrderResponse order : masterOrderList) {
            boolean matchesSearch = true;
            boolean matchesChip = true;

            // ১. সার্চ ফিল্টার (Order Number / Tracking Number সার্চ)
            if (!searchQuery.isEmpty()) {
                String orderNo = order.getOrderNumber() != null ? order.getOrderNumber().toLowerCase() : "";
                String trackingNo = order.getTrackingNumber() != null ? order.getTrackingNumber().toLowerCase() : "";
                String idStr = String.valueOf(order.getId());

                matchesSearch = orderNo.contains(searchQuery) || trackingNo.contains(searchQuery) || idStr.contains(searchQuery);
            }

            // ২. স্ট্যাটাস চিপ ফিল্টার
            if (order.getStatus() != null) {
                // NEWLY ADDED
                if (checkedChipId == R.id.chipPending) {
                    matchesChip = order.getStatus() == OnlineOrderStatus.PENDING_VERIFICATION;
                } else if (checkedChipId == R.id.chipInTransit) {
                    matchesChip = order.getStatus() == OnlineOrderStatus.CONFIRMED 
                               || order.getStatus() == OnlineOrderStatus.READY_FOR_PICKUP 
                               || order.getStatus() == OnlineOrderStatus.DISPATCHED;
                } else if (checkedChipId == R.id.chipDelivered) {
                    matchesChip = order.getStatus() == OnlineOrderStatus.DELIVERED;
                }
            }

            // উভয় ফিল্টার ম্যাচ করলে লিস্টে যোগ করা হবে
            if (matchesSearch && matchesChip) {
                filteredOrderList.add(order);
            }
        }

        orderAdapter.notifyDataSetChanged();

        if (filteredOrderList.isEmpty()) {
            showEmptyState();
        } else {
            showDataState();
        }
    }

    /* =================================================================
     * UI STATE MANAGEMENT HELPER METHODS
     * ================================================================= */
    private void showDataState() {
        recyclerOrders.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        recyclerOrders.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
    }

    private void showLoadingState() {
        progressLoading.setVisibility(View.VISIBLE);
        recyclerOrders.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    private void hideLoadingState() {
        progressLoading.setVisibility(View.GONE);
    }
}