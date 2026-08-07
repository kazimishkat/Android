package com.example.pharmacymanagement.model.request;

import lombok.Data;

@Data
public class OnlineOrderItemRequest {

    private Long medicineId; // 👈 কাস্টমার সরাসরি মেডিসিন সিলেক্ট করবে


    // NEWLY ADDED
    private String medicineBrandName;

    private Integer quantity;


    private Double pricePerUnit;

    // NEWLY ADDED
    private transient boolean inStock = true;
}
