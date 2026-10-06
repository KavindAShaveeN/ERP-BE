package com.rr.erp.service;

import com.rr.erp.dto.EmployeeResponseDTO;
import com.rr.erp.entity.Employee;
import com.rr.erp.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }


    // =========================================================
    // GET ALL
    // =========================================================

    public List<EmployeeResponseDTO> getAllEmployees() {

        return employeeRepository.getAllEmployees();
    }


    // =========================================================
    // CREATE EMPLOYEE
    // =========================================================

    @Transactional
    public Employee createEmployee(Employee employee) {

        employeeRepository.createEmployee(employee);

        if (employee.getProjects() != null) {

            for (Integer projectId : employee.getProjects()) {

                employeeRepository.addAssignedProject(
                        employee.getEmployeeCode(),
                        projectId
                );
            }
        }

        return employee;
    }


    // =========================================================
    // UPDATE EMPLOYEE
    // =========================================================

    @Transactional
    public Employee updateEmployee(
            String employeeCode,
            Employee employee) {

        int updatedRows =
                employeeRepository.updateEmployee(
                        employeeCode,
                        employee
                );

        if (updatedRows == 0) {

            throw new RuntimeException(
                    "Employee not found with employee code: "
                            + employeeCode
            );
        }

        // Remove previous assigned projects
        employeeRepository.deleteAssignedProjects(
                employeeCode
        );

        // Add new assigned projects
        if (employee.getProjects() != null) {

            for (Integer projectId : employee.getProjects()) {

                employeeRepository.addAssignedProject(
                        employeeCode,
                        projectId
                );
            }
        }

        employee.setEmployeeCode(employeeCode);

        return employee;
    }
}