package com.rr.erp.entity;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CashAccount {

    public static final String TYPE_MAIN_CASH = "MAIN_CASH";
    public static final String TYPE_EMPLOYEE = "EMPLOYEE";

    private UUID cashAccountId;

    @Size(max = 60, message = "Employee code must be at most 60 characters")
    private String employeeCode;

    @NotBlank(message = "Account name is required")
    @Size(max = 150, message = "Account name must be at most 150 characters")
    private String accountName;

    @NotBlank(message = "Account type is required")
    private String accountType;

    // Defaults to 0 on create; kept as-is on update when omitted
    @Digits(integer = 13, fraction = 2, message = "Account balance must have at most 13 digits and 2 decimal places")
    private BigDecimal accountBalance;

    private Boolean isActive;
    private LocalDateTime createdAt;

    // Read-only, joined from employee
    private String employeeName;
}
