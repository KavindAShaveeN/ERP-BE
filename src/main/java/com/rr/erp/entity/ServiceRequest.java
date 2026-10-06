package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ServiceRequest {

    private UUID serviceRequestId;

    private String serviceRequestCode;

    private String submittedBy;

    private LocalDateTime requestedDate;

    private String projectCode;

    private String assetCode;

    private String operatorName;

    private String phoneNumber;

    private String maintenanceWorks;

    /** BREAKDOWN, FAULTS or ACCIDENT. */
    private String requestType;

    private String remarks;

    private Boolean isApproved;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}