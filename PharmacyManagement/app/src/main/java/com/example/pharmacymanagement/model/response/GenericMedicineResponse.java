package com.example.pharmacymanagement.model.response;

import lombok.Data;

@Data
public class GenericMedicineResponse {
    private Long id;
    private String genericName;
    private Long categoryId;
    private String categoryName;
    private String description;
    private String indication;
    private String sideEffects;
    private String contraindications;
    private Boolean isActive;
}
