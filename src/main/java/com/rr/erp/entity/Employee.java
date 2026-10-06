package com.rr.erp.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
public class Employee {

    @NotBlank(message = "Employee code is required")
    private String employeeCode;
    private String employeeType;
    private String temporaryYNumber;
    private String epfNumber;
    private Integer employeeStatusId;
    private LocalDate joinedDate;
    private LocalDate permanentAppointmentDate;
    private String department;
    private Integer designationId;
    private String reportingSupervisorCode;
    @NotBlank(message = "NIC number is required")
    private String nic;
    @NotBlank(message = "Full name is required")
    private String fullName;
    private String nameWithInitials;
    private LocalDate dob;
    private String gender;
    private String maritalStatus;
    private String nationality;
    private String contactNumber;
    private String altContactNumber;
    private String email;
    private String residentialAddress;
    private String emergencyContactPerson;
    private String emergencyContactNumber;
    private String emergencyContactRelationship;
    private String bankName;
    private String bankBranch;
    private String bankAccountNumber;
    private String accountHolderName;
    private BigDecimal basicSalary;
    private String profilePhoto;
    private String nicCopy;
    private String epfDocuments;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<Integer> projects;
}

