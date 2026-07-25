package com.example.pharmacymanagement.model.response;

import com.example.pharmacymanagement.enums.Gender;

import lombok.Data;

@Data
public class CustomerResponse {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private Gender gender;
    private Integer age;
    private AddressResponse address;
    private Integer loyaltyPoints;
    private Boolean isActive;
    private String image;

    // 🟢 অনলাইন কাস্টমারের জন্য ইউজার অ্যাকাউন্ট ট্র্যাকিং ফিল্ড
    private Long userId;
    private String username;
    private Boolean accountCreated;
    private Boolean userEnabled;

}
