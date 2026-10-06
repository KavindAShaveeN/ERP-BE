package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class IntraProjectIssue {

    private UUID intraProjectIssueId;
    private String intraProjectIssueCode;
    private String issuedBy;
    private LocalDateTime issuedDate;
    private String issuedProjectCode;

    /** GENERAL / SUBCONTRACTOR / PERSONAL / LOAN / JOB_CARD. */
    private String issueType;

    // JOB_CARD only — a workshop project issuing items directly against a job card.
    private UUID jobCardId;

    // GENERAL only: issued to a phase within the same project, confirmed by an employee.
    private Integer receivedProjectPhaseId;
    private String receivedBy;
    private LocalDateTime receivedDate;

    private String approvedBy;
    private LocalDateTime approvedDate;
    private Boolean isAuthorized;
    private Boolean isIssued;
    private Boolean isReceived;

    // SUBCONTRACTOR only.
    private Integer subcontractorId;

    // LOAN only, when the recipient is an outside person rather than an employee
    // (an employee recipient uses receivedBy, same as PERSONAL).
    private String receiverName;
    private String receiverNic;
    private LocalDate expectedReturnDate;

    // Optional, any issue type: the registered asset these items are being issued for (e.g. spare
    // parts fitted to a vehicle). Unlike IntraProjectIssueItem#assetCode, which hands out the asset
    // itself, this only records which asset the issued items were used on.
    private String forAssetCode;

    private List<IntraProjectIssueItem> items;
}
