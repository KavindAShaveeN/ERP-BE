package com.rr.erp.service;


import com.rr.erp.entity.EmployeeStatus;
import com.rr.erp.repository.EmployeeStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeStatusService {

    private final EmployeeStatusRepository employeeStatusRepository;

    public EmployeeStatusService(
            EmployeeStatusRepository employeeStatusRepository
    ) {
        this.employeeStatusRepository = employeeStatusRepository;
    }

    public List<EmployeeStatus> getAllEmployeeStatuses() {
        return employeeStatusRepository.getAllEmployeeStatuses();
    }
}