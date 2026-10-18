package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/** One employee's manually recorded attendance for one day. */
@Getter
@Setter
public class EmployeeAttendance {

    private UUID attendanceId;
    private String employeeCode;
    // Read-only — joined from the employee table.
    private String employeeName;
    private String department;
    private LocalDate attendanceDate;
    // PRESENT, ABSENT, HALF_DAY or ON_LEAVE.
    private String status;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private String remarks;
    private String recordedBy;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
