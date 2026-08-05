package com.example.pharmacymanagement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        holder.txtQuantity.setText(String.valueOf(item.getQuantity()));

        double unitPrice = item.getPricePerUnit() != null ? item.getPricePerUnit() : 0.0;
        double lineTotal = unitPrice * item.getQuantity();
        holder.txtPrice.setText(String.format("৳ %.2f", lineTotal));

        // Increase Quantity Button
        holder.btnIncrease.setOnClickListener(v -> {
            int currentQty = item.getQuantity();
            int newQty = currentQty + 1;
            item.setQuantity(newQty);
            notifyItemChanged(holder.getAdapterPosition());
            if (listener != null) listener.onQuantityChanged(holder.getAdapterPosition(), newQty);
        });

        // Decrease Quantity Button
        holder.btnDecrease.setOnClickListener(v -> {
            int currentQty = item.getQuantity();
            if (currentQty > 1) {
                int newQty = currentQty - 1;
                item.setQuantity(newQty);
                notifyItemChanged(holder.getAdapterPosition());
                if (listener != null) listener.onQuantityChanged(holder.getAdapterPosition(), newQty);
            }
        });

        // Remove Item Button
        holder.btnRemove.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
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
        TextView txtQuantity;
        ImageButton btnDecrease;
        ImageButton btnIncrease;
        ImageButton btnRemove;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMedicineName = itemView.findViewById(R.id.txtCartMedicineName);
            txtPrice = itemView.findViewById(R.id.txtCartPrice);
            txtQuantity = itemView.findViewById(R.id.txtCartQuantity);
            btnDecrease = itemView.findViewById(R.id.btnDecreaseQty);
            btnIncrease = itemView.findViewById(R.id.btnIncreaseQty);
            btnRemove = itemView.findViewById(R.id.btnRemoveCartItem);
        }
    }
}
