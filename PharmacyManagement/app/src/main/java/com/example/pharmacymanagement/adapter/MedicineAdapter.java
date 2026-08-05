package com.example.pharmacymanagement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pharmacymanagement.R;
import com.example.pharmacymanagement.api.ApiClient;
import com.example.pharmacymanagement.model.response.MedicineResponse;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import lombok.NonNull;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {
    public interface OnMedicineClickListener {
        void onMedicineClick(MedicineResponse medicine);
        void onAddToCartClick(MedicineResponse medicine);
    }

    private final List<MedicineResponse> medicineList;
    private final OnMedicineClickListener listener;

    /* =================================================================
     * CONSTRUCTOR
     * ================================================================= */

    public MedicineAdapter(List<MedicineResponse> medicineList, OnMedicineClickListener listener) {
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

        // Load Medicine Image using Glide
        if (medicine.getImage() != null && !medicine.getImage().isEmpty()) {
            String imageUrl = ApiClient.IMAGE_URL + "/medicine/" + medicine.getImage();
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_search)
                    .error(android.R.drawable.ic_menu_search)
                    .into(holder.imgMedicine);
        } else {
            holder.imgMedicine.setImageResource(android.R.drawable.ic_menu_search);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onMedicineClick(medicine);
        });

        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) listener.onAddToCartClick(medicine);
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
        ShapeableImageView imgMedicine;
        TextView txtMedicineName;
        TextView txtGenericName;
        TextView txtPrice;
        Button btnAddToCart;

        MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMedicine = itemView.findViewById(R.id.imgMedicine);
            txtMedicineName = itemView.findViewById(R.id.txtMedicineName);
            txtGenericName = itemView.findViewById(R.id.txtGenericName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}
