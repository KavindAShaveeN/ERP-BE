package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ThreePService {

    private UUID threePServiceId;

    private UUID jobCardId;

    private String item;

    private String serialNumber;

    /** Set when the despatched item is a registered asset (mutually exclusive with itemCode). */
    private String assetCode;

    /** Set when the despatched item is a stock item (mutually exclusive with assetCode). */
    private String itemCode;

    private BigDecimal quantity;

    private LocalDateTime issuedDate;

    private String status;

    private String serviceProviderName;

    /** supplier.suppliercode of the provider; set when issued DIRECT or from the service PO's supplier. */
    private String serviceProviderCode;

    /** Rows saved in one batch share this id (one purchasing request). */
    private UUID requestGroupId;

    /** DIRECT (issued straight to the provider) or PURCHASING (requested via the purchasing department). */
    private String issueMode;

    /** What service is needed on this item. */
    private String description;

    private LocalDateTime receivedDate;

    private String remarks;

    private BigDecimal serviceCharge;

    /** The purchase order (po.po_code) this service was raised against, if any. */
    private String poCode;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** Read-only - joined from job_card. */
    private String jobCardCode;

    /** Read-only - joined from job_card. */
    private String projectCode;
}