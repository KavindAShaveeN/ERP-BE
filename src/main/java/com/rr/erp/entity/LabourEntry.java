package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * One stint of work by a daily-paid employee at a project on a given day. An employee can have
 * several entries on the same day when they worked at more than one place.
 */
@Getter
@Setter
public class LabourEntry {

    private UUID labourEntryId;
    private String employeeCode;
    // Read-only — joined from the employee / project tables.
    private String employeeName;
    private String projectCode;
    private String projectName;
    private LocalDate workDate;
    // Optional; when both are given the frontend derives hours from them.
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal hours;
    private String workDescription;
    private String recordedBy;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
