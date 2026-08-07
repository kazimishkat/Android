package com.example.pharmacymanagement.model.response;

import lombok.Data;

@Data
public class BranchInventoryResponse { // NEWLY ADDED
    private Long id;
    private Long branchId;
    private Long medicineId;
    private Integer quantityOnHand;
}
