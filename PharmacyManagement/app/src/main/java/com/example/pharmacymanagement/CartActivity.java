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
        }

        cartAdapter.notifyDataSetChanged();
        updateTotalPriceUI();
    }

    private void saveAndUpdateTotal() {
        sessionManager.saveCartItems(cartItemList);
        updateTotalPriceUI();
    }

    private void updateTotalPriceUI() {
        double total = 0.0;
        for (OnlineOrderItemRequest item : cartItemList) {
            double price = item.getPricePerUnit() != null ? item.getPricePerUnit() : 0.0;
            int qty = item.getQuantity();
            total += (price * qty);
        }

        txtCartTotal.setText(String.format("৳ %.2f", total));

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
     * CHECKOUT API PROCESS
     * ================================================================= */
    private void handleCheckout() {
        if (cartItemList.isEmpty()) {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Customer Info বের করা
        CustomerResponse customer = sessionManager.getCustomer();
        Long customerId = (customer != null && customer.getId() != null) ? customer.getId() : 1L;

        // ক্যাশ অন ডেলিভারির জন্য ডিফল্ট Transaction ID
        String transactionId = "COD_" + System.currentTimeMillis();

        btnProceedCheckout.setEnabled(false);
        btnProceedCheckout.setText("Processing...");

        OnlineOrderRequest orderRequest = new OnlineOrderRequest();
        orderRequest.setItems(cartItemList);

        // 🟢 ApiService-এর ৩টি প্যারামিটার অনুযায়ী কল করা হলো
        apiService.placeOrder(orderRequest, customerId, transactionId).enqueue(new Callback<OnlineOrderResponse>() {
            @Override
            public void onResponse(Call<OnlineOrderResponse> call, Response<OnlineOrderResponse> response) {
                btnProceedCheckout.setEnabled(true);
                btnProceedCheckout.setText("Proceed Checkout");

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CartActivity.this, "Order placed successfully!", Toast.LENGTH_LONG).show();

                    // SessionManager দিয়ে কার্ট ক্লিয়ার
                    sessionManager.clearCart();

                    Intent intent = new Intent(CartActivity.this, OrderHistory.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CartActivity.this, "Failed to place order. Try again!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OnlineOrderResponse> call, Throwable t) {
                btnProceedCheckout.setEnabled(true);
                btnProceedCheckout.setText("Proceed Checkout");
                Toast.makeText(CartActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}