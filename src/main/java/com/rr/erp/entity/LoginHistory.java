package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** One login attempt (successful or failed) and, once the frontend reports it, its logout. */
@Getter
@Setter
public class LoginHistory {

    private UUID loginHistoryId;
    // Username as typed — kept even when it matches no credential.
    private String userName;
    // Null when the username does not exist.
    private String employeeCode;
    // Read from the employee record for display; not stored in login_history.
    private String employeeName;
    private String loginStatus;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private String ipAddress;
    private String userAgent;
}
