package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class StockReturn {

    private UUID stockReturnId;
    private String stockReturnCode;

    private String fromProjectCode;
    private String toProjectCode;

    private UUID ginId;

    private String returnType;
    private String poCode;
    private String supplierCode;

    private String reason;
    private String remark;

    private LocalDateTime returnDate;
    private String returnBy;

    private LocalDateTime approvedDate;
    private String approvedBy;

    private Boolean isApproved;

    // Employee code of whoever receives the goods (internal returns only), and the transporting
    // vehicle's asset code (Vehicle asset master) with a free-text mirror of its registration
    // number — same as GIN's received_by / vehicle_asset_code / vehicle_no.
    private String receivedBy;
    private String vehicleAssetCode;
    private String vehicleNo;

    // The asset this return's items were issued for (e.g. spare parts/consumables returned
    // that had been drawn for one specific vehicle/plant item) — a single asset for the
    // whole return, not per line.
    private String forAssetCode;

    // Invoice number and delivery note number for this return.
    private String invoiceNumber;
    private String deliveryNoteNumber;

    // Security's confirmation that the returned goods actually left/arrived through the
    // gate — same fourth-stage pattern as GIN/GRN.
    private String gateVerifiedBy;
    private LocalDateTime gateVerifiedDate;
    private Boolean isGateVerified;

    // The receiving project's security confirming these returned goods actually arrived at
    // their gate — recorded before a GRN exists for this return. Once a GRN is created for
    // it, this is copied onto the GRN's own gate_verified_* columns.
    private String arrivalGateVerifiedBy;
    private LocalDateTime arrivalGateVerifiedDate;
    private Boolean isArrivalGateVerified;

    private List<StockReturnItem> items;
}