package com.example.pharmacymanagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmacymanagement.adapter.OrderAdapter;
import com.example.pharmacymanagement.api.ApiClient;
import com.example.pharmacymanagement.api.ApiService;
import com.example.pharmacymanagement.model.response.CustomerResponse;
import com.example.pharmacymanagement.model.response.LoginResponse;
import com.example.pharmacymanagement.model.response.OnlineOrderResponse;
import com.example.pharmacymanagement.session.SessionManager;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerDashboardActivity extends AppCompatActivity {

    /* =================================================================
     * UI VIEW DECLARATIONS
     * ================================================================= */
    private TextView txtUserName, txtUserEmail, txtUserRole;
    private Button btnLogout;

    private TextView txtStatInTransitValue, txtStatPendingValue, txtStatDeliveredValue;

    private MaterialCardView cardBrowseMedicine, cardCart, cardMyOrders;

    private TextView txtViewAll;
    private RecyclerView recyclerOrders;
    private LinearLayout layoutEmptyState;

    private LinearLayout navGenerics, navCheckout, navProfile;

    /* =================================================================
     * ADAPTER, DATA & SERVICES
     * ================================================================= */
    private OrderAdapter orderAdapter;
    private List<OnlineOrderResponse> recentOrderList;
    private ApiService apiService;
    private SessionManager sessionManager;

    /* =================================================================
     * ACTIVITY LIFECYCLE
     * ================================================================= */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        }

        initViews();
        setupRecyclerView();
        setupClickListeners();

        apiService = ApiClient.getClient(this);
        sessionManager = new SessionManager(this);

        loadUserProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchOrdersAndCalculateStats();
    }

    /* =================================================================
     * VIEW INITIALIZATION & RECYCLERVIEW SETUP
     * ================================================================= */
    private void initViews() {
        btnLogout = findViewById(R.id.btnLogout);
        txtUserName = findViewById(R.id.txtUserName);
        txtUserEmail = findViewById(R.id.txtUserEmail);
        txtUserRole = findViewById(R.id.txtUserRole);

        txtStatInTransitValue = findViewById(R.id.txtStatInTransitValue);
        txtStatPendingValue = findViewById(R.id.txtStatPendingValue);
        txtStatDeliveredValue = findViewById(R.id.txtStatDeliveredValue);

        cardBrowseMedicine = findViewById(R.id.cardBrowseMedicine);
        cardCart = findViewById(R.id.cardCart);
        cardMyOrders = findViewById(R.id.cardMyOrders);

        txtViewAll = findViewById(R.id.txtViewAll);
        recyclerOrders = findViewById(R.id.recyclerOrders);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        // 🟢 3 Footer Views Init
        navGenerics = findViewById(R.id.navGenerics);
        navCheckout = findViewById(R.id.navCheckout);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupRecyclerView() {
        recentOrderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(recentOrderList, order -> {
            Intent intent = new Intent(CustomerDashboardActivity.this, OrderHistory.class);
            startActivity(intent);
        });

        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(orderAdapter);
    }

    /* =================================================================
     * EVENT LISTENERS & NAVIGATION
     * ================================================================= */
    private void setupClickListeners() {
        cardBrowseMedicine.setOnClickListener(v ->
                startActivity(new Intent(CustomerDashboardActivity.this, MedicineList.class)));

        cardCart.setOnClickListener(v ->
                startActivity(new Intent(CustomerDashboardActivity.this, CartActivity.class)));

        cardMyOrders.setOnClickListener(v ->
                startActivity(new Intent(CustomerDashboardActivity.this, OrderHistory.class)));

        txtViewAll.setOnClickListener(v ->
                startActivity(new Intent(CustomerDashboardActivity.this, OrderHistory.class)));

        btnLogout.setOnClickListener(v -> performLogout());

        // 🟢 3 FOOTER MENU NAVIGATION LISTENERS
        navGenerics.setOnClickListener(v ->
                startActivity(new Intent(CustomerDashboardActivity.this, GenericActivity.class)));

        navCheckout.setOnClickListener(v ->
                startActivity(new Intent(CustomerDashboardActivity.this, CheckoutActivity.class)));

        navProfile.setOnClickListener(v ->
                startActivity(new Intent(CustomerDashboardActivity.this, ProfileActivity.class)));
    }

    /* =================================================================
     * USER DATA & LOCAL SESSION
     * ================================================================= */
    private void loadUserProfile() {
        CustomerResponse customer = sessionManager.getCustomer();
        LoginResponse user = sessionManager.getUser();

        if (customer != null) {
            txtUserName.setText(customer.getName());
            txtUserEmail.setText(customer.getEmail());
        } else if (user != null) {
            txtUserName.setText(user.getFullName() != null ? user.getFullName() : user.getUsername());
            txtUserEmail.setText(user.getEmail());
        }

        if (user != null && user.getRole() != null) {
            txtUserRole.setText(user.getRole().toUpperCase());
        } else {
            txtUserRole.setText("CUSTOMER");
        }
    }

    /* =================================================================
     * API CALLS & STATS LOGIC
     * ================================================================= */
    private void fetchOrdersAndCalculateStats() {
        CustomerResponse customer = sessionManager.getCustomer();
        LoginResponse user = sessionManager.getUser();

        Log.d("DashboardDebug", "Token: " + sessionManager.getToken());

        if (customer != null && customer.getId() != null) {
            Log.d("DashboardDebug", "Customer ID found in session: " + customer.getId());
            fetchOrderHistoryForCustomer(customer.getId());
        } else if (user != null && user.getUserId() != null) {
            Log.d("DashboardDebug", "Customer ID not in session. Fetching customer profile for User ID: " + user.getUserId());
            apiService.getCustomerByUserId(user.getUserId()).enqueue(new Callback<CustomerResponse>() {
                @Override
                public void onResponse(Call<CustomerResponse> call, Response<CustomerResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        CustomerResponse fetchedCustomer = response.body();
                        sessionManager.saveCustomer(fetchedCustomer);
                        Log.d("DashboardDebug", "Fetched Customer ID: " + fetchedCustomer.getId());
                        if (fetchedCustomer.getId() != null) {
                            fetchOrderHistoryForCustomer(fetchedCustomer.getId());
                        } else {
                            Log.e("DashboardDebug", "Fetched Customer ID is null");
                            resetStatsAndShowEmpty();
                        }
                    } else {
                        Log.e("DashboardDebug", "Failed to fetch customer profile. Response Code: " + response.code());
                        resetStatsAndShowEmpty();
                    }
                }

                @Override
                public void onFailure(Call<CustomerResponse> call, Throwable t) {
                    Log.e("DashboardDebug", "Error fetching customer profile: " + t.getMessage(), t);
                    resetStatsAndShowEmpty();
                }
            });
        } else {
            Log.e("DashboardDebug", "No customer or user details found in session");
            resetStatsAndShowEmpty();
        }
    }

    private void fetchOrderHistoryForCustomer(Long customerId) {
        apiService.getCustomerOrderHistory(customerId).enqueue(new Callback<List<OnlineOrderResponse>>() {
            @Override
            public void onResponse(Call<List<OnlineOrderResponse>> call, Response<List<OnlineOrderResponse>> response) {
                Log.d("DashboardDebug", "Order History Response Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    List<OnlineOrderResponse> allOrders = response.body();
                    Log.d("DashboardDebug", "Fetched orders count: " + allOrders.size());
                    calculateAndShowStats(allOrders);
                    updateRecentOrdersList(allOrders);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e("DashboardDebug", "Failed to fetch orders. Error body: " + errorBody);
                    } catch (Exception e) {
                        Log.e("DashboardDebug", "Failed to read error body", e);
                    }
                    resetStatsAndShowEmpty();
                }
            }

            @Override
            public void onFailure(Call<List<OnlineOrderResponse>> call, Throwable t) {
                Log.e("DashboardDebug", "Order history network failure: " + t.getMessage(), t);
                resetStatsAndShowEmpty();
            }
        });
    }

    private void calculateAndShowStats(List<OnlineOrderResponse> allOrders) {
        int countInTransit = 0;
        int countPending = 0;
        int countDelivered = 0;

        for (OnlineOrderResponse order : allOrders) {
            if (order.getStatus() != null) {
                switch (order.getStatus()) {
                    case PENDING_VERIFICATION:
                        countPending++;
                        break;
                    case CONFIRMED:
                    case READY_FOR_PICKUP:
                    case DISPATCHED:
                        countInTransit++;
                        break;
                    case DELIVERED:
                        countDelivered++;
                        break;
                    case CANCELLED:
                        break;
                }
            }
        }

        txtStatInTransitValue.setText(String.format("%02d", countInTransit));
        txtStatPendingValue.setText(String.format("%02d", countPending));
        txtStatDeliveredValue.setText(String.format("%02d", countDelivered));
    }

    private void updateRecentOrdersList(List<OnlineOrderResponse> allOrders) {
        if (allOrders == null || allOrders.isEmpty()) {
            resetStatsAndShowEmpty();
            return;
        }

        recentOrderList.clear();

        int limit = Math.min(allOrders.size(), 5);
        for (int i = 0; i < limit; i++) {
            recentOrderList.add(allOrders.get(i));
        }

        orderAdapter.notifyDataSetChanged();

        recyclerOrders.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
        txtViewAll.setVisibility(View.VISIBLE);
    }

    private void resetStatsAndShowEmpty() {
        txtStatInTransitValue.setText("00");
        txtStatPendingValue.setText("00");
        txtStatDeliveredValue.setText("00");

        recentOrderList.clear();
        orderAdapter.notifyDataSetChanged();

        recyclerOrders.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
        txtViewAll.setVisibility(View.GONE);
    }

    /* =================================================================
     * LOGOUT MANAGEMENT
     * ================================================================= */
    private void performLogout() {
        sessionManager.logout();

        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(CustomerDashboardActivity.this, login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}