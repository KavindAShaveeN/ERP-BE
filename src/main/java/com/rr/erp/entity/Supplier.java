package com.rr.erp.entity;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Supplier {
    private String supplierCode;
    private String supplierName;
    private String line1;
    private String line2;
    private String city;
    private String country;
    private String phoneNumber;
    @Email(message = "Invalid email address")
    private String email;
    private Boolean status;
    private String telephoneNumber;
    private String accountNumber;
    private String taxRegistrationNumber;
    private String Description;
    private String bankName;
    private String contactPerson;
    private String contactPersonNumber;
}
