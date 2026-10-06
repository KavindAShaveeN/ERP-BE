package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class FuelIssue {

    private UUID fuelIssueId;
    private String fuelIssueCode;
    // System-entered timestamp: set once, server-side, at creation. No longer accepted
    // from the client — kept only as an audit trail of when the record was entered.
    private LocalDateTime issuedDate;
    // The real/actual date the fuel was issued — user-entered and editable, and the date
    // used for all sorting/filtering/reporting.
    private LocalDateTime fuelIssueDate;
    private String projectCode;
    private String issuedBy;
    private String receivedBy;
    private String remarks;
    private String assetCode;
    // Holds the item_code_code of the fuel item drawn from stock (item_category 'F' / Fuel),
    // not a hardcoded "Diesel"/"Petrol" string.
    private String fuelType;
    private BigDecimal quantity;
    private Integer meterReading;
    private Boolean isIssued;
    private Boolean isReceived;
    private Boolean isActive;
    // Whether the asset's tank has actually been filled with this issue's fuel yet
    // (Not filled / Filled) — separate from isReceived, which only tracks whether the
    // receiver has confirmed delivery of the fuel.
    private Boolean isFilled;
}