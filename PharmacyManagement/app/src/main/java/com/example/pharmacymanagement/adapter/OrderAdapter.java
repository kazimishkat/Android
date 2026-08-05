package com.example.pharmacymanagement.adapter;

import android.graphics.drawable.GradientDrawable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmacymanagement.R;
import com.example.pharmacymanagement.model.response.OnlineOrderResponse;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import lombok.NonNull;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    public interface OnOrderClickListener {
        void onOrderClick(OnlineOrderResponse order);
    }

    private final List<OnlineOrderResponse> orderList;
    private final OnOrderClickListener listener;

    /* =================================================================
     * CONSTRUCTOR
     * ================================================================= */

    public OrderAdapter(List<OnlineOrderResponse> orderList, OnOrderClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OnlineOrderResponse order = orderList.get(position);

        holder.txtOrderId.setText(order.getOrderNumber() != null ? order.getOrderNumber() : "#" + order.getId());

        // Format Date
        if (order.getOrderDate() != null) {
            try {
                LocalDateTime dateTime = order.getOrderDate();
                Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
                holder.txtDate.setText(DateFormat.format("MMM d, yyyy", date));
            } catch (Exception e) {
                holder.txtDate.setText(order.getOrderDate().toString());
            }
        } else {
            holder.txtDate.setText("-");
        }

        // Apply Dynamic Status Pill Badge & Colors
        String status = order.getStatus() != null ? order.getStatus().name() : "";
        applyStatusBadge(holder, status);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOrderClick(order);
        });
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    /* =================================================================
     * DYNAMIC STATUS BADGE MAPPING (Neumorphic Soft Modern Colors)
     * ================================================================= */

    private void applyStatusBadge(OrderViewHolder holder, String status) {
        GradientDrawable bg = (GradientDrawable) holder.txtStatus.getBackground().mutate();
        View context = holder.itemView;

        switch (status.toUpperCase()) {
            case "PENDING":
            case "PENDING_VERIFICATION": // NEWLY ADDED
                holder.txtStatus.setText("PENDING"); // NEWLY ADDED
                bg.setColor(ContextCompat.getColor(context.getContext(), R.color.colorPausedYellowBg));
                holder.txtStatus.setTextColor(ContextCompat.getColor(context.getContext(), R.color.colorPausedYellow));
                break;

            case "PROCESSING":
            case "SHIPPED":
            case "IN_TRANSIT":
            case "CONFIRMED": // NEWLY ADDED
            case "READY_FOR_PICKUP": // NEWLY ADDED
            case "DISPATCHED": // NEWLY ADDED
                holder.txtStatus.setText("RUNNING");
                bg.setColor(ContextCompat.getColor(context.getContext(), R.color.colorRunningBg)); // NEWLY ADDED
                holder.txtStatus.setTextColor(ContextCompat.getColor(context.getContext(), R.color.colorRunningText)); // NEWLY ADDED
                break;

            case "DELIVERED":
            case "COMPLETED": // NEWLY ADDED
                holder.txtStatus.setText("DELIVERED");
                bg.setColor(ContextCompat.getColor(context.getContext(), R.color.colorDeliveredBg)); // NEWLY ADDED
                holder.txtStatus.setTextColor(ContextCompat.getColor(context.getContext(), R.color.colorDeliveredText)); // NEWLY ADDED
                break;

            case "CANCELLED":
                holder.txtStatus.setText("CANCELLED");
                bg.setColor(ContextCompat.getColor(context.getContext(), R.color.colorCompletedRedBg));
                holder.txtStatus.setTextColor(ContextCompat.getColor(context.getContext(), R.color.colorCompletedRed));
                break;

            default:
                holder.txtStatus.setText(status);
                bg.setColor(ContextCompat.getColor(context.getContext(), R.color.colorRunningBg)); // NEWLY ADDED
                holder.txtStatus.setTextColor(ContextCompat.getColor(context.getContext(), R.color.colorRunningText)); // NEWLY ADDED
                break;
        }
    }

    /* =================================================================
     * VIEWHOLDER CLASS
     * ================================================================= */

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtOrderId;
        TextView txtStatus;
        TextView txtDate;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtDate = itemView.findViewById(R.id.txtDate);
        }
    }
}
