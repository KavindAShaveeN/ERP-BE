package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class JobIssueItemResponse {

    private UUID jobIssueItemId;

    private UUID jobCardId;

    private String projectCode;

    private String itemCode;

    private String partNumber;

    private String serialNumber;

    private String description;

    private BigDecimal unitPrice;

    private BigDecimal quantity;

    private String issuedBy;

    private String issuedPerson;

    private LocalDateTime issuedDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}