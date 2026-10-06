package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class IntraProjectIssueItem {

    private UUID intraProjectIssueItemId;
    private UUID intraProjectIssueId;
    private String itemCode;
    private String description;
    private String size;
    private Integer uom;
    private BigDecimal quantity;
    private String remarks;
    private BigDecimal unitPrice;
    private BigDecimal amount;

    /** Deductible / non-deductible / returnable classification, same lookup as GINItem. */
    private Integer issueItemTypeId;

    // Dimensional items only (see com.rr.erp.util.DimensionalItems) — which exact size is
    // being issued, so the FIFO draw is scoped to that size.
    private BigDecimal lengthM;
    private BigDecimal widthM;

    // Set when this line moves one specific registered asset (quantity 1). Such a line bypasses
    // the quantity stock engine — the asset's location history tracks it instead
    // (see asset_movement_tracking.sql).
    private String assetCode;

    // Non-stock items only — the specific batch (see StockBatch#description) the user picked
    // to issue from. Not persisted on intra_project_issue_item itself (recorded via
    // stock_action_allocation); only used at issue time to route the draw.
    private UUID stockBatchId;
}
