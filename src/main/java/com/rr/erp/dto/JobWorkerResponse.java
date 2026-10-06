package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class JobWorkerResponse {

    private UUID jobWorkerId;
    private UUID jobCardId;
    private String employeeCode;
    private String fullName;
    private String designationName;
    private BigDecimal hoursWorked;
}