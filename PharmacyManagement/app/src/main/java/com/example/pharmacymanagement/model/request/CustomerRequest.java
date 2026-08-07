package com.example.pharmacymanagement.model.request;

import com.example.pharmacymanagement.enums.Gender;

import lombok.Data;

@Data
public class CustomerRequest {
    private String name;
    private String phone;
    private String email;
    private Gender gender;
    private Integer age;
    private AddressRequest address;
    private Boolean createAccount = false;
    private String username;
    private String password;
}
