package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterCredentialRequest {

    private String employeeCode;
    private String userName;
    private String password;
}
