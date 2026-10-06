package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class JobIssueItemReturn {
    private UUID jobIssueItemReturnId;
    private UUID jobCardId;
    private UUID jobIssueItemId;
    private String projectCode;
    private String itemCode;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private String returnedBy;
    private LocalDateTime returnedDate;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
