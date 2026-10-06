package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class JobWorker {

    private UUID jobWorkerId;

    private UUID jobCardId;

    private String employeeCode;

    private BigDecimal hoursWorked;
}