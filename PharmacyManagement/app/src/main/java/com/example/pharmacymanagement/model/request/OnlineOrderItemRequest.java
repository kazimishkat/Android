package com.example.pharmacymanagement.model.request;

import lombok.Data;

@Data
public class OnlineOrderItemRequest {

    private Long medicineId; // 👈 কাস্টমার সরাসরি মেডিসিন সিলেক্ট করবে


    private transient String medicineBrandName;

    private Integer quantity;


    private Double pricePerUnit;
}
