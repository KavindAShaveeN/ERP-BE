package com.rr.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
public class EmployeeResponseDTO {

    private String employeeCode;
    private String employeeType;
    private String temporaryYNumber;
    private String epfNumber;
    private Integer employeeStatusId;
    private String statusName;
    private LocalDate joinedDate;
    private LocalDate permanentAppointmentDate;
    private String department;
    private Integer designationId;
    private String designationName;
    private String reportingSupervisorCode;
    private String reportingSupervisorName;
    private String nic;
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

    private List<ProjectResponse> projectIds;
}


