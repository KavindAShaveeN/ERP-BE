package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class IntraProjectIssueReturnItem {

    private UUID intraProjectIssueReturnItemId;
    private UUID intraProjectIssueReturnId;

    /** The original issued line this return is against, used to compute outstanding quantity. */
    private UUID intraProjectIssueItemId;

    private String itemCode;
    private String description;
    private String size;
    private Integer uomId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String remarks;

    // Dimensional items only (see com.rr.erp.util.DimensionalItems) — the RESULT size being
    // returned. For an ordinary return this matches the source issue item's size; for a
    // cut-return (worker cut the issued bars and is returning offcuts of a new size) it
    // differs, which is exactly how IntraProjectIssueReturnService tells the two paths apart.
    private BigDecimal lengthM;
    private BigDecimal widthM;

    // Set when this line returns one specific registered asset (quantity 1) - copied from the
    // issued line it is against. Kept out of the quantity stock engine.
    private String assetCode;
}
