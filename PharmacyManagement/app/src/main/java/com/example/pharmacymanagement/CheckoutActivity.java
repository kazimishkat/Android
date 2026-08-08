package com.example.pharmacymanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmacymanagement.adapter.CheckoutCartAdapter;
import com.example.pharmacymanagement.api.ApiClient;
import com.example.pharmacymanagement.api.ApiService;
import com.example.pharmacymanagement.enums.PaymentMethod;
import com.example.pharmacymanagement.model.request.OnlineOrderItemRequest;
import com.example.pharmacymanagement.model.request.OnlineOrderRequest;
import com.example.pharmacymanagement.model.response.BranchInventoryResponse;
import com.example.pharmacymanagement.model.response.BranchResponse;
import com.example.pharmacymanagement.model.response.CustomerResponse;
import com.example.pharmacymanagement.model.response.MedicineResponse;
import com.example.pharmacymanagement.model.response.OnlineOrderResponse;
import com.example.pharmacymanagement.session.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    /* =================================================================
     * UI VIEW DECLARATIONS
     * ================================================================= */
    private ImageButton btnBack;
    private AutoCompleteTextView spinnerBranch, spinnerPaymentMethod;
    private TextInputEditText etDeliveryAddress;
    private RecyclerView recyclerCheckoutSummary;
    private TextView txtGrandTotal;
    private MaterialButton btnSubmitOrder, btnCancel;
    private TextInputLayout layoutTransactionId;
    private TextInputEditText etTransactionId;

    /* =================================================================
     * ADAPTER, DATA & SERVICES
     * ================================================================= */
    private CheckoutCartAdapter checkoutCartAdapter;
    private List<OnlineOrderItemRequest> cartItemList;
    private ApiService apiService;
    private SessionManager sessionManager;
    private List<BranchResponse> branchList = new ArrayList<>();
    private BranchResponse selectedBranch = null;

    // Out of stock tracking flag
    private boolean hasOutOfStockItems = false;

    /* =================================================================
     * ACTIVITY LIFECYCLE
     * ================================================================= */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient(this);

        initViews();
        setupRecyclerView();
        loadCheckoutSummary();
        setupDropdowns();
        setupListeners();


    }

    /* =================================================================
     * VIEW INITIALIZATION & DROPDOWNS
     * ================================================================= */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        spinnerBranch = findViewById(R.id.spinnerBranch);
        spinnerPaymentMethod = findViewById(R.id.spinnerPaymentMethod);
        etDeliveryAddress = findViewById(R.id.etDeliveryAddress);
        recyclerCheckoutSummary = findViewById(R.id.recyclerCheckoutSummary);
        txtGrandTotal = findViewById(R.id.txtGrandTotal);
        btnSubmitOrder = findViewById(R.id.btnSubmitOrder);
        btnCancel = findViewById(R.id.btnCancel);
        layoutTransactionId = findViewById(R.id.layoutTransactionId);
        etTransactionId = findViewById(R.id.etTransactionId);
    }

    private void fetchBranches() {
        apiService.getAllBranches().enqueue(new Callback<List<BranchResponse>>() {
            @Override
            public void onResponse(Call<List<BranchResponse>> call, Response<List<BranchResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    branchList.clear();
                    branchList.addAll(response.body());

                    String[] branchNames = new String[branchList.size()];
                    for (int i = 0; i < branchList.size(); i++) {
                        branchNames[i] = branchList.get(i).getName();
                    }

                    ArrayAdapter<String> branchAdapter = new ArrayAdapter<>(CheckoutActivity.this, android.R.layout.simple_dropdown_item_1line, branchNames);
                    spinnerBranch.setAdapter(branchAdapter);

                    if (!branchList.isEmpty()) {
                        selectedBranch = branchList.get(0);
                        spinnerBranch.setText(selectedBranch.getName(), false);
                        checkBranchStock(selectedBranch.getId());
                    }
                } else {
                    Toast.makeText(CheckoutActivity.this, "Failed to load branches", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<BranchResponse>> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Network Error: Failed to load branches", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkBranchStock(Long branchId) {
        apiService.getInventoryByBranch(branchId).enqueue(new Callback<List<BranchInventoryResponse>>() {
            @Override
            public void onResponse(Call<List<BranchInventoryResponse>> call, Response<List<BranchInventoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BranchInventoryResponse> inventories = response.body();
                    hasOutOfStockItems = false;

                    for (OnlineOrderItemRequest item : cartItemList) {
                        boolean itemInStock = false;
                        for (BranchInventoryResponse inv : inventories) {
                            if (inv.getMedicineId() != null && inv.getMedicineId().equals(item.getMedicineId())) {
                                if (inv.getQuantityOnHand() != null && inv.getQuantityOnHand() >= item.getQuantity()) {
                                    itemInStock = true;
                                }
                                break;
                            }
                        }
                        item.setInStock(itemInStock);
                        if (!itemInStock) {
                            hasOutOfStockItems = true;
                        }
                    }

                    if (checkoutCartAdapter != null) {
                        checkoutCartAdapter.notifyDataSetChanged();
                    }
                } else {
                    hasOutOfStockItems = true;
                    for (OnlineOrderItemRequest item : cartItemList) {
                        item.setInStock(false);
                    }
                    if (checkoutCartAdapter != null) {
                        checkoutCartAdapter.notifyDataSetChanged();
                    }
                    Toast.makeText(CheckoutActivity.this, "Failed to load branch inventory!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<BranchInventoryResponse>> call, Throwable t) {
                hasOutOfStockItems = true;
                for (OnlineOrderItemRequest item : cartItemList) {
                    item.setInStock(false);
                }
                if (checkoutCartAdapter != null) {
                    checkoutCartAdapter.notifyDataSetChanged();
                }
                Toast.makeText(CheckoutActivity.this, "Network Error: Failed to check stock", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDropdowns() {
        fetchBranches();

        PaymentMethod[] methods = PaymentMethod.values();
        String[] paymentDisplayNames = new String[methods.length];
        for (int i = 0; i < methods.length; i++) {
            paymentDisplayNames[i] = methods[i].getDisplayName();
        }
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, paymentDisplayNames);
        spinnerPaymentMethod.setAdapter(paymentAdapter);
        spinnerPaymentMethod.setText(PaymentMethod.CASH.getDisplayName(), false);
    }

    /* =================================================================
     * RECYCLERVIEW SETUP & REMOVE FUNCTIONALITY
     * ================================================================= */
    private void setupRecyclerView() {
        cartItemList = new ArrayList<>();

        // 🟢 (int position) দিয়ে ক্লিক হ্যান্ডেল করা হয়েছে যা ডিলিট অপশন নিশ্চিত করবে
        checkoutCartAdapter = new CheckoutCartAdapter(cartItemList, (int position) -> {
            cartItemList.remove(position);
            checkoutCartAdapter.notifyItemRemoved(position);

            // ডাটাবেজ আপডেট এবং স্টকের ফ্ল্যাগ রি-চেক
            sessionManager.saveCartItems(cartItemList);
            calculateGrandTotal();

            if (selectedBranch != null) {
                checkBranchStock(selectedBranch.getId());
            }

            if (cartItemList.isEmpty()) {
                Toast.makeText(CheckoutActivity.this, "Cart is now empty!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        recyclerCheckoutSummary.setLayoutManager(new LinearLayoutManager(this));
        recyclerCheckoutSummary.setAdapter(checkoutCartAdapter);
    }

    private void loadCheckoutSummary() {
        cartItemList.clear();
        List<OnlineOrderItemRequest> savedItems = sessionManager.getCartItems();

        if (savedItems != null && !savedItems.isEmpty()) {
            for (OnlineOrderItemRequest item : savedItems) {
                if (item.getMedicineId() != null
                        && item.getQuantity() != null && item.getQuantity() > 0
                        && item.getPricePerUnit() != null) {
                    cartItemList.add(item);
                }
            }

            for (OnlineOrderItemRequest item : cartItemList) {
                if (item.getMedicineBrandName() == null || item.getMedicineBrandName().trim().isEmpty()) {
                    resolveMedicineName(item, () -> {
                        checkoutCartAdapter.notifyDataSetChanged();
                        sessionManager.saveCartItems(cartItemList);
                    });
                }
            }
        }

        checkoutCartAdapter.notifyDataSetChanged();
        saveAndUpdateTotal();
    }

    private void resolveMedicineName(OnlineOrderItemRequest item, Runnable callback) {
        if (item.getMedicineId() == null) return;
        apiService.getMedicineById(item.getMedicineId()).enqueue(new Callback<MedicineResponse>() {
            @Override
            public void onResponse(Call<MedicineResponse> call, Response<MedicineResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    item.setMedicineBrandName(response.body().getBrandName());
                }
                if (callback != null) callback.run();
            }

            @Override
            public void onFailure(Call<MedicineResponse> call, Throwable t) {
                if (callback != null) callback.run();
            }
        });
    }

    private void saveAndUpdateTotal() {
        sessionManager.saveCartItems(cartItemList);
        calculateGrandTotal();
    }

    private void calculateGrandTotal() {
        double total = 0.0;
        for (OnlineOrderItemRequest item : cartItemList) {
            double price = item.getPricePerUnit() != null ? item.getPricePerUnit() : 0.0;
            total += (price * item.getQuantity());
        }

        txtGrandTotal.setText(String.format("৳ %.2f", total));
    }

    /* =================================================================
     * EVENT LISTENERS & SUBMIT LOGIC
     * ================================================================= */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        btnCancel.setOnClickListener(v -> finish());

        spinnerBranch.setOnItemClickListener((parent, view, position, id) -> {
            if (position < branchList.size()) {
                selectedBranch = branchList.get(position);
                checkBranchStock(selectedBranch.getId());
            }
        });

        spinnerPaymentMethod.setOnItemClickListener((parent, view, position, id) -> {
            PaymentMethod selectedMethod = PaymentMethod.values()[position];
            if (selectedMethod == PaymentMethod.CASH) {
                layoutTransactionId.setVisibility(View.GONE);
            } else {
                layoutTransactionId.setVisibility(View.VISIBLE);
            }
        });

        btnSubmitOrder.setOnClickListener(v -> handleSubmitOrder());
    }

    private void handleSubmitOrder() {
        if (selectedBranch == null) {
            Toast.makeText(this, "Please select a branch first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String address = etDeliveryAddress.getText().toString().trim();
        if (address.isEmpty()) {
            etDeliveryAddress.setError("Shipping address is required!");
            etDeliveryAddress.requestFocus();
            return;
        }

        if (cartItemList.isEmpty()) {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Out of Stock চেক
        if (hasOutOfStockItems) {
            Toast.makeText(this, "Please remove out-of-stock items before placing the order", Toast.LENGTH_LONG).show();
            return;
        }

        String selectedPaymentText = spinnerPaymentMethod.getText().toString().trim();
        PaymentMethod selectedPayment = null;
        for (PaymentMethod m : PaymentMethod.values()) {
            if (m.getDisplayName().equalsIgnoreCase(selectedPaymentText)) {
                selectedPayment = m;
                break;
            }
        }

        String transactionId = "COD_" + System.currentTimeMillis();
        if (selectedPayment != null && selectedPayment != PaymentMethod.CASH) {
            String txnId = etTransactionId.getText().toString().trim();
            if (txnId.isEmpty()) {
                etTransactionId.setError("Transaction ID is required for mobile banking/card payment!");
                etTransactionId.requestFocus();
                return;
            }
            transactionId = txnId;
        }

        CustomerResponse customer = sessionManager.getCustomer();
        Long customerId = (customer != null && customer.getId() != null) ? customer.getId() : 1L;

        btnSubmitOrder.setEnabled(false);
        btnSubmitOrder.setText("Submitting...");

        OnlineOrderRequest orderRequest = new OnlineOrderRequest();
        orderRequest.setBranchId(selectedBranch.getId());
        orderRequest.setDeliveryAddress(address);

        double total = 0.0;
        for (OnlineOrderItemRequest item : cartItemList) {
            double price = item.getPricePerUnit() != null ? item.getPricePerUnit() : 0.0;
            total += (price * item.getQuantity());
        }
        orderRequest.setTotalAmount(total);

        if (selectedPayment != null) {
            orderRequest.setPaymentMethod(selectedPayment.name());
        }

        orderRequest.setItems(cartItemList);

        apiService.placeOrder(orderRequest, customerId, transactionId).enqueue(new Callback<OnlineOrderResponse>() {
            @Override
            public void onResponse(Call<OnlineOrderResponse> call, Response<OnlineOrderResponse> response) {
                btnSubmitOrder.setEnabled(true);
                btnSubmitOrder.setText("Submit Order");

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CheckoutActivity.this, "Order placed successfully!", Toast.LENGTH_LONG).show();

                    sessionManager.clearCart();

                    Intent intent = new Intent(CheckoutActivity.this, OrderHistory.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CheckoutActivity.this, "Failed to place order!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OnlineOrderResponse> call, Throwable t) {
                btnSubmitOrder.setEnabled(true);
                btnSubmitOrder.setText("Submit Order");
                Toast.makeText(CheckoutActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}