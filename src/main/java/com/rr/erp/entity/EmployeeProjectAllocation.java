package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class EmployeeProjectAllocation {

    private UUID allocationId;
    private String employeeCode;
    private String employeeName;
    private String department;
    private Integer projectId;
    private String projectCode;
    private String projectName;
    private LocalDate workDate;
    private BigDecimal hours;
    private String remarks;
    private String assignedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
