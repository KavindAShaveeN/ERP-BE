package com.rr.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {

    private String employeeCode;
    private String employeeName;
    private List<AssignedProject> assignedProjects;
    // Id of this login's login_history row — send it back to POST /api/logout. Null only if the
    // history could not be written (the login itself still succeeds).
    private UUID sessionId;
}