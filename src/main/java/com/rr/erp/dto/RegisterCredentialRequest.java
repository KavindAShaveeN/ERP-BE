package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterCredentialRequest {

    private String employeeCode;
    private String userName;
    private String password;
    /** The role the new user starts with. */
    private Integer roleId;
    /** The project the role applies to; null means every project. */
    private String projectCode;
}
