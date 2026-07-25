package com.example.pharmacymanagement.model.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";

    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String role;

    // Branch info — শুধুমাত্র তখনই সেট হবে যদি ইউজারের কোনো ব্রাঞ্চ অ্যাসাইন করা থাকে
    private Long branchId;
    private String branchName;
}
