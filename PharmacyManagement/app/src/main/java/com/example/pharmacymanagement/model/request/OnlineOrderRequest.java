package com.example.pharmacymanagement.model.request;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import lombok.Data;

@Data
public class OnlineOrderRequest {

    private Long branchId;

    private Long prescriptionId; // ঐচ্ছিক (যদি প্রেসক্রিপশন আপলোড করে)


    private Double totalAmount;


    private String deliveryAddress;

    // NEWLY ADDED
    private String paymentMethod;


    private List<OnlineOrderItemRequest> items;
}
