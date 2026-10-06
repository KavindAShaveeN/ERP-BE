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

    private LocalDateTime receivedDate;

    private String remarks;

    private BigDecimal serviceCharge;

    /** The purchase order (po.po_code) this service was raised against, if any. */
    private String poCode;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}