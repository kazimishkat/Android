package com.example.pharmacymanagement.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmacymanagement.R;
import com.example.pharmacymanagement.model.request.OnlineOrderItemRequest;

import java.util.List;

public class CheckoutCartAdapter extends RecyclerView.Adapter<CheckoutCartAdapter.CheckoutViewHolder> {

    // 🟢 ডিলিট ইভেন্ট শোনানোর জন্য ইন্টারফেস
    public interface OnItemRemoveListener {
        void onItemRemoved(int position);
    }

    private final List<OnlineOrderItemRequest> items;
    private final OnItemRemoveListener removeListener;

    public CheckoutCartAdapter(List<OnlineOrderItemRequest> items, OnItemRemoveListener removeListener) {
        this.items = items;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkout_cart, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
        OnlineOrderItemRequest item = items.get(position);

        holder.txtMedicineName.setText(item.getMedicineBrandName() != null ? item.getMedicineBrandName() : "");
        holder.txtQuantity.setText("Qty: " + item.getQuantity());

        double unitPrice = item.getPricePerUnit() != null ? item.getPricePerUnit() : 0.0;
        double lineTotal = unitPrice * item.getQuantity();
        holder.txtPrice.setText(String.format("৳ %.2f", lineTotal));

        // Stock Status Badge Update
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(12f);

        if (item.isInStock()) {
            holder.txtStockStatus.setText("In Stock");
            holder.txtStockStatus.setTextColor(Color.parseColor("#137333"));
            bg.setColor(Color.parseColor("#E6F4EA"));
        } else {
            holder.txtStockStatus.setText("Out of Stock");
            holder.txtStockStatus.setTextColor(Color.parseColor("#C5221F"));
            bg.setColor(Color.parseColor("#FCE8E6"));
        }

        holder.txtStockStatus.setBackground(bg);

        // 🟢 Delete Button Click Listener
        if (holder.btnRemove != null) {
            holder.btnRemove.setOnClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && removeListener != null) {
                    removeListener.onItemRemoved(pos);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        TextView txtMedicineName;
        TextView txtPrice;
        TextView txtQuantity;
        TextView txtStockStatus;
        ImageButton btnRemove; // 🟢 Trash Button

        CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMedicineName = itemView.findViewById(R.id.txtCheckoutMedicineName);
            txtPrice = itemView.findViewById(R.id.txtCheckoutPrice);
            txtQuantity = itemView.findViewById(R.id.txtCheckoutQuantity);
            txtStockStatus = itemView.findViewById(R.id.txtStockStatus);
            btnRemove = itemView.findViewById(R.id.btnRemoveCheckoutItem); // 🟢 XML ID Bind
        }
    }
}