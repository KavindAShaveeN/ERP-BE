package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class SPIssue {

    private UUID spIssueId;
    private String spIssueCode;

    private Integer assetCode;
    private Integer itemCode;

    private String description;

    private String requestedBy;
    private LocalDateTime requestedDate;

    private String issuedBy;
    private LocalDateTime issuedDate;

    private String approvedBy;
    private LocalDateTime approvedDate;

    private Boolean isApproved;

    private String remarks;
}
