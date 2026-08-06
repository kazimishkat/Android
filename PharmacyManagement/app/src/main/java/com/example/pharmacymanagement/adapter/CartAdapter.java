package com.example.pharmacymanagement.adapter;

import android.text.Editable; // NEWLY ADDED
import android.text.TextWatcher; // NEWLY ADDED
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText; // NEWLY ADDED
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmacymanagement.R;
import com.example.pharmacymanagement.model.request.OnlineOrderItemRequest;

import java.util.List;

import lombok.NonNull;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    public interface OnCartItemChangeListener {
        void onQuantityChanged(int position, int newQuantity);
        void onItemRemoved(int position);
    }

    private final List<OnlineOrderItemRequest> cartItems;
    private final OnCartItemChangeListener listener;

    /* =================================================================
     * CONSTRUCTOR
     * ================================================================= */

    public CartAdapter(List<OnlineOrderItemRequest> cartItems, OnCartItemChangeListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        OnlineOrderItemRequest item = cartItems.get(position);

        holder.txtMedicineName.setText(item.getMedicineBrandName() != null ? item.getMedicineBrandName() : "Medicine");

        // Remove previous watcher to avoid recursion
        if (holder.etQuantity.getTag() instanceof TextWatcher) {
            holder.etQuantity.removeTextChangedListener((TextWatcher) holder.etQuantity.getTag());
        }

        holder.etQuantity.setText(String.valueOf(item.getQuantity()));

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String val = s.toString().trim();
                if (!val.isEmpty()) {
                    try {
                        int qty = Integer.parseInt(val);
                        if (qty >= 1 && qty != item.getQuantity()) {
                            item.setQuantity(qty);
                            if (listener != null) listener.onQuantityChanged(holder.getBindingAdapterPosition(), qty);
                            
                            double unitPrice = item.getPricePerUnit() != null ? item.getPricePerUnit() : 0.0;
                            double lineTotal = unitPrice * qty;
                            holder.txtPrice.setText(String.format("৳ %.2f", lineTotal));
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        };
        holder.etQuantity.addTextChangedListener(watcher);
        holder.etQuantity.setTag(watcher);

        double unitPrice = item.getPricePerUnit() != null ? item.getPricePerUnit() : 0.0;
        double lineTotal = unitPrice * item.getQuantity();
        holder.txtPrice.setText(String.format("৳ %.2f", lineTotal));

        // Increase Quantity Button
        holder.btnIncrease.setOnClickListener(v -> {
            int currentQty = item.getQuantity();
            int newQty = currentQty + 1;
            item.setQuantity(newQty);
            holder.etQuantity.setText(String.valueOf(newQty));
        });

        // Decrease Quantity Button
        holder.btnDecrease.setOnClickListener(v -> {
            int currentQty = item.getQuantity();
            if (currentQty > 1) {
                int newQty = currentQty - 1;
                item.setQuantity(newQty);
                holder.etQuantity.setText(String.valueOf(newQty));
            }
        });

        // Remove Item Button
        holder.btnRemove.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (listener != null) listener.onItemRemoved(pos);
        });
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    /* =================================================================
     * VIEWHOLDER CLASS
     * ================================================================= */

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView txtMedicineName;
        TextView txtPrice;
        EditText etQuantity;
        ImageButton btnDecrease;
        ImageButton btnIncrease;
        ImageButton btnRemove;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMedicineName = itemView.findViewById(R.id.txtCartMedicineName);
            txtPrice = itemView.findViewById(R.id.txtCartPrice);
            etQuantity = itemView.findViewById(R.id.etCartQuantity);
            btnDecrease = itemView.findViewById(R.id.btnDecreaseQty);
            btnIncrease = itemView.findViewById(R.id.btnIncreaseQty);
            btnRemove = itemView.findViewById(R.id.btnRemoveCartItem);
        }
    }
}
