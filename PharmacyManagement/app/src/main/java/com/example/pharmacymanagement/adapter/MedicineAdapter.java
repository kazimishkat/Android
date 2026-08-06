package com.example.pharmacymanagement.adapter;

import android.content.Context; // NEWLY ADDED
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pharmacymanagement.R;
import com.example.pharmacymanagement.api.ApiClient;
import com.example.pharmacymanagement.model.request.OnlineOrderItemRequest;
import com.example.pharmacymanagement.model.response.MedicineResponse;
import com.example.pharmacymanagement.session.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {
    public interface OnMedicineClickListener {
        void onMedicineClick(MedicineResponse medicine);
        void onAddToCartClick(MedicineResponse medicine);
    }

    private final Context context; // NEWLY ADDED
    private final List<MedicineResponse> medicineList;
    private final OnMedicineClickListener listener;

    /* =================================================================
     * CONSTRUCTOR
     * ================================================================= */

    // NEWLY ADDED
    public MedicineAdapter(Context context, List<MedicineResponse> medicineList, OnMedicineClickListener listener) {
        this.context = context;
        this.medicineList = medicineList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        MedicineResponse medicine = medicineList.get(position);

        holder.txtMedicineName.setText(medicine.getBrandName() != null ? medicine.getBrandName() : "");
        holder.txtGenericName.setText(medicine.getGenericName() != null ? medicine.getGenericName() : "");

        double price = medicine.getDefaultSellingPrice() != null ? medicine.getDefaultSellingPrice().doubleValue() : 0.0;
        holder.txtPrice.setText(String.format("৳ %.2f", price));

        // Image loading removed as image placeholder is deleted from item layout // NEWLY ADDED

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onMedicineClick(medicine);
        });

        holder.btnAddToCart.setOnClickListener(v -> {
            SessionManager sessionManager = new SessionManager(context);

            // ১. আগের সেভ করা কার্ট লিস্ট নিয়ে আসা
            List<OnlineOrderItemRequest> cartList = sessionManager.getCartItems();
            if (cartList == null) {
                cartList = new ArrayList<>();
            }

            // ২. মেডিসিনটি কার্টে আগে থেকেই আছে কি না তা চেক করা
            boolean isExist = false;
            for (OnlineOrderItemRequest item : cartList) {
                if (item.getMedicineId() != null && item.getMedicineId().equals(medicine.getId())) {
                    // থাকলে পরিমাণ ১ বাড়িয়ে দেওয়া
                    item.setQuantity(item.getQuantity() + 1);
                    isExist = true;
                    break;
                }
            }

            // ৩. নতুন মেডিসিন হলে অবজেক্ট বানিয়ে লিস্টে যোগ করা
            if (!isExist) {
                OnlineOrderItemRequest newItem = new OnlineOrderItemRequest();
                newItem.setMedicineId(medicine.getId());
                newItem.setMedicineBrandName(medicine.getBrandName());
                newItem.setQuantity(1);

                // Price সেট করা (Double-এ কাস্ট করে)
                if (medicine.getDefaultSellingPrice() != null) {
                    newItem.setPricePerUnit(medicine.getDefaultSellingPrice().doubleValue());
                } else {
                    newItem.setPricePerUnit(0.0);
                }

                cartList.add(newItem);
            }

            // ৪. আপডেট হওয়া কার্ট লিস্ট SessionManager-এ সেভ করা
            sessionManager.saveCartItems(cartList);

            Toast.makeText(context, medicine.getBrandName() + " added to cart!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return medicineList != null ? medicineList.size() : 0;
    }

    /* =================================================================
     * VIEWHOLDER CLASS
     * ================================================================= */

    static class MedicineViewHolder extends RecyclerView.ViewHolder {
        // imgMedicine removed // NEWLY ADDED
        TextView txtMedicineName;
        TextView txtGenericName;
        TextView txtPrice;
        Button btnAddToCart;

        MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMedicineName = itemView.findViewById(R.id.txtMedicineName);
            txtGenericName = itemView.findViewById(R.id.txtGenericName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}
