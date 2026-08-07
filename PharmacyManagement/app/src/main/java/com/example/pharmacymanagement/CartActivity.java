package com.example.pharmacymanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmacymanagement.adapter.CartAdapter;
import com.example.pharmacymanagement.api.ApiClient;
import com.example.pharmacymanagement.api.ApiService;
import com.example.pharmacymanagement.model.request.OnlineOrderItemRequest;
import com.example.pharmacymanagement.model.request.OnlineOrderRequest;
import com.example.pharmacymanagement.model.response.CustomerResponse;
import com.example.pharmacymanagement.model.response.OnlineOrderResponse;
import com.example.pharmacymanagement.session.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {

    /* =================================================================
     * UI VIEW DECLARATIONS
     * ================================================================= */
    private ImageButton btnBack;
    private RecyclerView recyclerCart;
    private LinearLayout layoutEmptyCart;
    private TextView txtCartTotal;
    private MaterialButton btnProceedCheckout;

    /* =================================================================
     * ADAPTER, DATA & SERVICES
     * ================================================================= */
    private CartAdapter cartAdapter;
    private List<OnlineOrderItemRequest> cartItemList;
    private ApiService apiService;
    private SessionManager sessionManager;

    /* =================================================================
     * ACTIVITY LIFECYCLE
     * ================================================================= */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        setupRecyclerView();
        setupListeners();

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient(this);

        loadCartData();
    }

    /* =================================================================
     * VIEW INITIALIZATION & RECYCLERVIEW SETUP
     * ================================================================= */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        recyclerCart = findViewById(R.id.recyclerCart);
        layoutEmptyCart = findViewById(R.id.layoutEmptyCart);
        txtCartTotal = findViewById(R.id.txtCartTotal);
        btnProceedCheckout = findViewById(R.id.btnProceedCheckout);
    }

    private void setupRecyclerView() {
        cartItemList = new ArrayList<>();

        // 🟢 CartAdapter.OnCartItemChangeListener ব্যবহার করা হয়েছে
        cartAdapter = new CartAdapter(cartItemList, new CartAdapter.OnCartItemChangeListener() {
            @Override
            public void onQuantityChanged(int position, int newQuantity) {
                saveAndUpdateTotal();
            }

            @Override
            public void onItemRemoved(int position) {
                cartItemList.remove(position);
                cartAdapter.notifyItemRemoved(position);
                saveAndUpdateTotal();
            }
        });

        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerCart.setAdapter(cartAdapter);
    }

    /* =================================================================
     * EVENT LISTENERS & NAVIGATION
     * ================================================================= */
    private void setupListeners() {
        // NEWLY ADDED
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        btnProceedCheckout.setOnClickListener(v -> handleCheckout());
    }

    /* =================================================================
     * SESSION CART DATA & TOTAL CALCULATION
     * ================================================================= */
    private void loadCartData() {
        cartItemList.clear();
        List<OnlineOrderItemRequest> savedItems = sessionManager.getCartItems();

        if (savedItems != null && !savedItems.isEmpty()) {
            cartItemList.addAll(savedItems);
            // NEWLY ADDED - Dynamically resolve any missing brand names on startup
            for (OnlineOrderItemRequest item : cartItemList) {
                if (item.getMedicineBrandName() == null || item.getMedicineBrandName().trim().isEmpty()) {
                    resolveMedicineName(item, () -> {
                        cartAdapter.notifyDataSetChanged();
                        sessionManager.saveCartItems(cartItemList);
                    });
                }
            }
        }

        cartAdapter.notifyDataSetChanged();
        updateTotalPriceUI();
    }

    // NEWLY ADDED
    private void resolveMedicineName(OnlineOrderItemRequest item, Runnable callback) {
        if (item.getMedicineId() == null) return;
        apiService.getMedicineById(item.getMedicineId()).enqueue(new Callback<com.example.pharmacymanagement.model.response.MedicineResponse>() {
            @Override
            public void onResponse(Call<com.example.pharmacymanagement.model.response.MedicineResponse> call, Response<com.example.pharmacymanagement.model.response.MedicineResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    item.setMedicineBrandName(response.body().getBrandName());
                }
                if (callback != null) callback.run();
            }

            @Override
            public void onFailure(Call<com.example.pharmacymanagement.model.response.MedicineResponse> call, Throwable t) {
                if (callback != null) callback.run();
            }
        });
    }

    private void saveAndUpdateTotal() {
        sessionManager.saveCartItems(cartItemList);
        updateTotalPriceUI();
    }

    // NEWLY ADDED
    private void calculateTotalPayable() {
        double total = 0.0;
        for (OnlineOrderItemRequest item : cartItemList) {
            double price = item.getPricePerUnit() != null ? item.getPricePerUnit() : 0.0;
            int qty = item.getQuantity();
            total += (price * qty);
        }
        txtCartTotal.setText(String.format("৳ %.2f", total));
    }

    private void updateTotalPriceUI() {
        calculateTotalPayable(); // NEWLY ADDED

        if (cartItemList.isEmpty()) {
            recyclerCart.setVisibility(View.GONE);
            layoutEmptyCart.setVisibility(View.VISIBLE);
            btnProceedCheckout.setEnabled(false);
        } else {
            recyclerCart.setVisibility(View.VISIBLE);
            layoutEmptyCart.setVisibility(View.GONE);
            btnProceedCheckout.setEnabled(true);
        }
    }

    /* =================================================================
     * CHECKOUT NAVIGATION (REDIRECT TO CHECKOUT ACTIVITY)
     * ================================================================= */
    private void handleCheckout() {
        // ১. কার্ট যদি খালি থাকে তবে মেসেজ দিয়ে আটকে দেবে
        if (cartItemList == null || cartItemList.isEmpty()) {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        // NEWLY ADDED - Validate cart item properties to avoid NullPointerExceptions during checkout serialization
        List<OnlineOrderItemRequest> validItems = new ArrayList<>();
        for (OnlineOrderItemRequest item : cartItemList) {
            if (item.getMedicineId() != null 
                    && item.getQuantity() != null && item.getQuantity() > 0 
                    && item.getPricePerUnit() != null) {
                validItems.add(item);
            }
        }

        if (validItems.isEmpty()) {
            Toast.makeText(this, "Cart contains invalid items. Please refresh and try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save cleaned list to session manager
        sessionManager.saveCartItems(validItems);

        // ২. সরাসরি CheckoutActivity-তে নিয়ে যাবে (অর্ডার সাবমিটের কাজ CheckoutActivity করবে)
        Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
        startActivity(intent);
    }
}