package com.rr.erp.dto;

import com.rr.erp.entity.EmployeeProjectAllocation;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Replaces the project allocations of the listed employees for one day.
 * An employee with an empty allocation list ends up with no allocations that day.
 */
@Getter
@Setter
public class EmployeeDayAllocationRequest {

    private LocalDate workDate;
    private String assignedBy;
    private List<EmployeeEntry> employees;

    @Getter
    @Setter
    public static class EmployeeEntry {
        private String employeeCode;
        // Only projectId, hours and remarks are read.
        private List<EmployeeProjectAllocation> allocations;
    }
}
