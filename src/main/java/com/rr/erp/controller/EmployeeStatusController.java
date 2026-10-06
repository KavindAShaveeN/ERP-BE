package com.rr.erp.controller;

import com.rr.erp.entity.EmployeeStatus;
import com.rr.erp.service.EmployeeStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee-statuses")
@CrossOrigin(origins = "*")
public class EmployeeStatusController {

    private final EmployeeStatusService employeeStatusService;

    public EmployeeStatusController(
            EmployeeStatusService employeeStatusService
    ) {
        this.employeeStatusService = employeeStatusService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeStatus>>
    getAllEmployeeStatuses() {

        return ResponseEntity.ok(
                employeeStatusService.getAllEmployeeStatuses()
        );
    }
}
