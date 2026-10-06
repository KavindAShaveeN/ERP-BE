package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** One employee's hours on one job card — the row shape behind the labor utilization report. */
@Getter
@Setter
public class LaborEntryRow {

    private String employeeCode;
    private String fullName;
    private String designationName;
    private UUID jobCardId;
    private String jobCardCode;
    private String assetCode;
    private String jobType;
    private String department;
    private LocalDateTime createdDate;
    private BigDecimal hoursWorked;
}
