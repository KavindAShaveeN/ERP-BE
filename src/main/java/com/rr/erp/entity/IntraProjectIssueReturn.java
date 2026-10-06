package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class IntraProjectIssueReturn {

    private UUID intraProjectIssueReturnId;
    private String intraProjectIssueReturnCode;

    /** Set only for GENERAL/LOAN returns, which are always against one specific issue.
     * Left null for SUBCONTRACTOR/PERSONAL returns, whose items may span several issues
     * (see items[].intraProjectIssueItemId, which every line always carries). */
    private UUID intraProjectIssueId;

    /** The project the return was recorded against (the active project at creation time)
     * — informational/for filtering only. Each item is credited back to its own source
     * issue's project (resolved via IntraProjectIssueRepository.findItemSource), which may
     * differ from this field when items span multiple issues/projects. */
    private String issuedProjectCode;

    /** GENERAL / SUBCONTRACTOR / PERSONAL / LOAN / JOB_CARD. */
    private String returnType;

    /** SUBCONTRACTOR returns only — who the return is from. */
    private Integer subcontractorId;

    /** PERSONAL returns only — who the return is from. */
    private String employeeCode;

    /** JOB_CARD returns only — which job card's issued items are being returned; items may
     * span several JOB_CARD issues made to that same job card. */
    private UUID jobCardId;

    /** Optional, any return type — the registered asset the returned items are coming back
     * from (the counterpart of IntraProjectIssue#forAssetCode). */
    private String fromAssetCode;

    private LocalDate returnDate;
    private String returnedBy;
    private String remarks;

    private String approvedBy;
    private LocalDate approvedDate;
    private Boolean isApproved;

    private List<IntraProjectIssueReturnItem> items;
}
