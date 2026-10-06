package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Where one intra_project_issue_item line came from, resolved by item id. Used when
 * recording a return so each line can be validated (against how much of that line is
 * still outstanding) and, once approved, credited back to the project that actually
 * issued it -- items on the same return may originate from different issues (a
 * subcontractor or employee can hold items issued across several intra project issues).
 */
@Getter
@Setter
public class IntraProjectIssueItemSource {
    private UUID intraProjectIssueId;
    private String issuedProjectCode;
    private String issueType;
    private BigDecimal quantity;

    // Dimensional items only (see com.rr.erp.util.DimensionalItems). Carried here so
    // IntraProjectIssueReturnService can compare a return line's size against the size it
    // was originally issued at, to tell an ordinary same-size return apart from a
    // cut-return (a new size born from cutting the issued bars).
    private String itemCode;
    private BigDecimal lengthM;
    private BigDecimal widthM;

    // Set when the issued line moved one specific registered asset (see asset_movement_tracking.sql).
    private String assetCode;
}
