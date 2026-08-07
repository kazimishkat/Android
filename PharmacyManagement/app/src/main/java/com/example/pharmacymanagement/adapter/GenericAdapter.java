package com.example.pharmacymanagement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharmacymanagement.R;
import com.example.pharmacymanagement.model.response.GenericMedicineResponse;

import java.util.List;

public class GenericAdapter extends RecyclerView.Adapter<GenericAdapter.GenericViewHolder> {

    // 🟢 ১. ইন্টারফেসে GenericMedicineResponse অবজেক্ট টাইপ দেওয়া হয়েছে
    public interface OnGenericClickListener {
        void onGenericClick(GenericMedicineResponse genericMedicine);
    }

    // 🟢 ২. লিস্টের টাইপ List<GenericMedicineResponse> করা হয়েছে
    private final List<GenericMedicineResponse> genericList;
    private final OnGenericClickListener listener;

    public GenericAdapter(List<GenericMedicineResponse> genericList, OnGenericClickListener listener) {
        this.genericList = genericList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GenericViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_generic, parent, false);
        return new GenericViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder holder, int position) {
        GenericMedicineResponse item = genericList.get(position);

        // 🟢 ৩. অবজেক্ট থেকে জেনরিক নাম ও ক্যাটাগরি সেট করা
        holder.txtGenericName.setText(item.getGenericName() != null ? item.getGenericName() : "");

        if (holder.txtCategoryName != null) {
            holder.txtCategoryName.setText(item.getCategoryName() != null ? item.getCategoryName() : "General");
        }

        View.OnClickListener clickListener = v -> {
            if (listener != null) {
                listener.onGenericClick(item); // 🟢 পুরো অবজেক্ট পাস হচ্ছে
            }
        };

        holder.itemView.setOnClickListener(clickListener);
        if (holder.imgArrow != null) {
            holder.imgArrow.setOnClickListener(clickListener);
        }
    }

    @Override
    public int getItemCount() {
        return genericList != null ? genericList.size() : 0;
    }

    static class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView txtGenericName, txtCategoryName;
        ImageView imgArrow;

        GenericViewHolder(@NonNull View itemView) {
            super(itemView);
            txtGenericName = itemView.findViewById(R.id.txtGenericName);
            txtCategoryName = itemView.findViewById(R.id.txtMedicineCount);
            imgArrow = itemView.findViewById(R.id.imgGenericArrow);
        }
    }
}