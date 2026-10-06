package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubcontractorProjectAssignment {

    private Integer subcontractorId;
    private String projectCode;
    /** Google Drive (or similar) link to the uploaded contract for this subcontractor
     * on this project. Optional. */
    private String contractLink;

    // Joined from `subcontractor`, for display on the project-scoped subcontractors page.
    private String subcontractorName;
    private String phoneNumber;
    private String email;
    private Boolean isActive;
}
