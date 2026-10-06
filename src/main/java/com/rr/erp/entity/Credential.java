package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Credential {

    private Integer credentialId;
    private String employeeCode;
    private String userName;
    private String password;
}