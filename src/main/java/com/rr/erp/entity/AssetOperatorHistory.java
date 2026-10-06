package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class AssetOperatorHistory {

    private UUID assetOperatorHistoryId;
    private String assetCode;
    // The employee who operates the asset — name is read from the employee record.
    private String operatorEmployeeCode;
    // Defaults to the operator's own contact number when created, but editable afterwards
    // (only on the latest entry) without touching the employee record.
    private String contactNumber;
    private String changedBy;
    private LocalDateTime changedDate;
    private String remarks;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
