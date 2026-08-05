package com.example.pharmacymanagement.model.request;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String token;        // from email link
    private String newPassword;
}
