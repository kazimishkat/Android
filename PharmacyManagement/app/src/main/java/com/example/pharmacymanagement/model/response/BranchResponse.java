package com.example.pharmacymanagement.model.response;

import lombok.Data;

@Data
public class BranchResponse { // NEWLY ADDED
    private Long id;
    private String branchCode;
    private String name;
    private String phone;
    private String email;
    private Boolean isActive;
}
