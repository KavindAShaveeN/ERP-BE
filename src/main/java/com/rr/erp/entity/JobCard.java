package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class JobCard {

    private UUID jobCardId;
    private String jobCardCode;
    /** The project that requested this job (set from the service request, or picked on
     * the create form) — was "projectCode" before it was split from the creating user's
     * own active project below. */
    private String requestingProjectCode;
    /** The project the creating user was logged into at creation time — the project whose
     * store this job card's item issues/returns operate against. */
    private String projectCode;
    private String assetCode;
    private String make;
    private String type;
    private BigDecimal meterReading;
    /** Total hours the assigned labours are expected to take to complete this job. */
    private BigDecimal expectedHoursToComplete;
    private LocalDateTime createdDate;
    private String informedBy;
    private String phoneNumber;
    private String department;
    private Integer jobStatusTypeId;
    private LocalDateTime updatedDate;
    private String jobCheckedBy;
    private LocalDateTime jobCheckedDate;
    private String remarks;
    private BigDecimal cost;
    private Boolean isFinished;
    private Boolean isDelivered;
    private String jobType;
    private UUID parentJobCardId;
    private UUID serviceRequestId;
    private java.util.List<UUID> serviceRequestIds;
    /** Explicit report snapshot selected by the workshop; creation only. */
    private java.util.List<UUID> selectedRequestFaultIds;
}