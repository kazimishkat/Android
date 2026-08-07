package com.example.pharmacymanagement.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

        // NEWLY ADDED
        holder.txtMedicineName.setText(item.getMedicineBrandName() != null ? item.getMedicineBrandName() : "");

        // TextWatcher সেশন ক্লিয়ার করা
        if (holder.etQuantity.getTag() instanceof TextWatcher) {
            holder.etQuantity.removeTextChangedListener((TextWatcher) holder.etQuantity.getTag());
        }

        holder.etQuantity.setText(String.valueOf(item.getQuantity()));

        // 🟢 ২. টেক্সট চেঞ্জ লিসেনার (ম্যানুয়ালি ইনপুট দিলে)
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
                            updateLineTotal(holder, item);
                            if (listener != null) listener.onQuantityChanged(holder.getBindingAdapterPosition(), qty);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        };
        holder.etQuantity.addTextChangedListener(watcher);
        holder.etQuantity.setTag(watcher);

        // প্রাইস সেটআপ
        updateLineTotal(holder, item);

        // 🟢 ৩. Increase Quantity Button (+ ক্লিক করলে)
        holder.btnIncrease.setOnClickListener(v -> {
            int currentQty = item.getQuantity();
            int newQty = currentQty + 1;
            item.setQuantity(newQty);
            holder.etQuantity.setText(String.valueOf(newQty));
            updateLineTotal(holder, item);

            // টোটাল পেয়াবল আপডেট ট্রিগার
            if (listener != null) listener.onQuantityChanged(holder.getBindingAdapterPosition(), newQty);
        });

        // 🟢 ৪. Decrease Quantity Button (- ক্লিক করলে)
        holder.btnDecrease.setOnClickListener(v -> {
            int currentQty = item.getQuantity();
            if (currentQty > 1) {
                int newQty = currentQty - 1;
                item.setQuantity(newQty);
                holder.etQuantity.setText(String.valueOf(newQty));
                updateLineTotal(holder, item);

                // টোটাল পেয়াবল আপডেট ট্রিগার
                if (listener != null) listener.onQuantityChanged(holder.getBindingAdapterPosition(), newQty);
            }
        });

        // 🟢 ৫. Remove Item Button
        holder.btnRemove.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) {
                listener.onItemRemoved(pos);
            }
        });
    }

    private void updateLineTotal(CartViewHolder holder, OnlineOrderItemRequest item) {
        double unitPrice = item.getPricePerUnit() != null ? item.getPricePerUnit() : 0.0;
        double lineTotal = unitPrice * item.getQuantity();
        holder.txtPrice.setText(String.format("৳ %.2f", lineTotal));
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

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