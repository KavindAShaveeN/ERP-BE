package com.rr.erp.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ItemCodeResponse {
    private String itemCode;
    private String itemCodeName;
    private Integer uomId;
    private BigDecimal quantityOnHand;
    private BigDecimal reorderLevel;
    private BigDecimal reorderQuantity;
    private String binLocation;

    /** Lifetime total received directly into this project's store via a GRN (a supplier GRN
     * booked straight to this project, or an internal-transfer GRN). Does NOT include stock
     * that arrived via an incoming GIN transfer — see {@link #totalReceivedInterProject}. */
    private BigDecimal totalReceived;
    /** Lifetime total issued out of this project's store via an intra-project issue. */
    private BigDecimal totalIssuedIntra;
    /** Lifetime total issued out of this project's store via a GIN to another project. */
    private BigDecimal totalIssuedInterProject;
    /** Lifetime total received into this project's store via a GIN transfer from another
     * project (e.g. a supplier GRN booked centrally at HQ, then GIN'd out to this project). */
    private BigDecimal totalReceivedInterProject;
    /** Lifetime total produced into this project's store by an approved plant production run
     * (finished goods/output) — never goes through a GRN. */
    private BigDecimal totalProduced;
    /** Lifetime total consumed out of this project's store as raw material input to an
     * approved plant production run — never goes through a GIN or intra-project issue. */
    private BigDecimal totalConsumed;
    /** Lifetime total issued out of this project's store as a job card issue — debited the
     * moment it's created, with no separate approval step. Never goes through a GIN or
     * intra-project issue. */
    private BigDecimal totalJobIssued;
    /** Lifetime total returned into this project's store from a job card issue return. */
    private BigDecimal totalJobReturned;
    /** Lifetime total issued out of this project's store as fuel — debited once the receiver
     * confirms delivery. Never goes through a GIN or intra-project issue. */
    private BigDecimal totalFuelIssued;
}
