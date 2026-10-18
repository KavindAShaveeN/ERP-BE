package com.rr.erp.service;

import com.rr.erp.dto.EmployeeDayAllocationRequest;
import com.rr.erp.entity.EmployeeProjectAllocation;
import com.rr.erp.repository.EmployeeProjectAllocationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class EmployeeProjectAllocationService {

    private static final BigDecimal MAX_HOURS_PER_DAY = BigDecimal.valueOf(24);

    private final EmployeeProjectAllocationRepository allocationRepository;

    public EmployeeProjectAllocationService(EmployeeProjectAllocationRepository allocationRepository) {
        this.allocationRepository = allocationRepository;
    }

    public List<EmployeeProjectAllocation> search(LocalDate from, LocalDate to, String employeeCode, Integer projectId) {
        return allocationRepository.search(from, to, employeeCode, projectId);
    }

    // Replaces each listed employee's allocations for the day; all employees are saved or none are.
    @Transactional
    public List<EmployeeProjectAllocation> saveDay(EmployeeDayAllocationRequest request) {
        if (request.getWorkDate() == null) {
            throw badRequest("Work date is required");
        }
        if (request.getEmployees() == null || request.getEmployees().isEmpty()) {
            throw badRequest("Select at least one employee");
        }

        Set<String> seenEmployees = new HashSet<>();
        for (EmployeeDayAllocationRequest.EmployeeEntry entry : request.getEmployees()) {
            String code = entry.getEmployeeCode();
            if (code == null || code.isBlank()) {
                throw badRequest("Employee is required");
            }
            if (!seenEmployees.add(code)) {
                throw badRequest("Employee " + code + " appears more than once");
            }

            List<EmployeeProjectAllocation> allocations = entry.getAllocations() == null ? List.of() : entry.getAllocations();
            Set<Integer> seenProjects = new HashSet<>();
            BigDecimal totalHours = BigDecimal.ZERO;
            for (EmployeeProjectAllocation allocation : allocations) {
                if (allocation.getProjectId() == null) {
                    throw badRequest(code + ": project is required");
                }
                if (!seenProjects.add(allocation.getProjectId())) {
                    throw badRequest(code + ": the same project is listed twice");
                }
                BigDecimal hours = allocation.getHours();
                if (hours != null) {
                    if (hours.signum() <= 0) {
                        throw badRequest(code + ": hours must be greater than zero");
                    }
                    totalHours = totalHours.add(hours);
                }
            }
            if (totalHours.compareTo(MAX_HOURS_PER_DAY) > 0) {
                throw badRequest(code + ": allocated hours exceed 24 for the day");
            }
        }

        try {
            for (EmployeeDayAllocationRequest.EmployeeEntry entry : request.getEmployees()) {
                allocationRepository.deleteForEmployeeOnDate(entry.getEmployeeCode(), request.getWorkDate());
                if (entry.getAllocations() == null) continue;
                for (EmployeeProjectAllocation allocation : entry.getAllocations()) {
                    String remarks = allocation.getRemarks() == null || allocation.getRemarks().isBlank()
                            ? null : allocation.getRemarks().trim();
                    allocationRepository.insert(entry.getEmployeeCode(), allocation.getProjectId(),
                            request.getWorkDate(), allocation.getHours(), remarks, request.getAssignedBy());
                }
            }
        } catch (DataIntegrityViolationException exception) {
            throw badRequest("Unknown employee or project, or invalid hours");
        }
        return allocationRepository.search(request.getWorkDate(), request.getWorkDate(), null, null);
    }

    // Copies one day's allocations onto another day for employees who have none there yet.
    @Transactional
    public List<EmployeeProjectAllocation> copyDay(LocalDate fromDate, LocalDate toDate, String assignedBy) {
        if (fromDate == null || toDate == null) {
            throw badRequest("Both dates are required");
        }
        if (fromDate.equals(toDate)) {
            throw badRequest("Choose a different day to copy from");
        }
        Set<String> alreadyPlanned = new HashSet<>();
        for (EmployeeProjectAllocation existing : allocationRepository.search(toDate, toDate, null, null)) {
            alreadyPlanned.add(existing.getEmployeeCode());
        }
        for (EmployeeProjectAllocation source : allocationRepository.search(fromDate, fromDate, null, null)) {
            if (alreadyPlanned.contains(source.getEmployeeCode())) continue;
            allocationRepository.insert(source.getEmployeeCode(), source.getProjectId(), toDate,
                    source.getHours(), source.getRemarks(), assignedBy);
        }
        return allocationRepository.search(toDate, toDate, null, null);
    }

    public boolean delete(UUID allocationId) {
        return allocationRepository.delete(allocationId) > 0;
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
