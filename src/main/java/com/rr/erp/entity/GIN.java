package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class GIN {

    private UUID ginId;
    private String ginCode;
    private Integer ginTypeId;
    private LocalDateTime issuedDate;
    private String issuedProjectCode;
    private String receivedProjectCode;
    private String receivedPerson;
    private String vehicleNo;
    // The dispatching vehicle's asset code (Vehicle asset master) — drives automatic
    // location-history entries when this GIN is issued. vehicleNo is kept in sync (mirroring
    // the vehicle's registration number) for backward-compatible search/display.
    private String vehicleAssetCode;
    // The asset this GIN's items were issued for (e.g. spare parts/consumables dispatched to
    // fit one specific vehicle/plant item) — a single asset for the whole GIN, not per line.
    private String forAssetCode;
    // Employee who issued this GIN from the store (employee_code).
    private String issuedBy;
    private String approvedBy;
    private LocalDateTime approvedDate;
    private Boolean isAuthorized;
    private LocalDate expectedReturnDate;
    private String receiverName;
    private String receiverNIC;
    private Integer  subContractorId;
    private UUID mrId;
    // Security's confirmation that the goods/vehicle on this GIN actually left through the
    // gate — a fourth actor stage alongside approvedBy, following the same pattern.
    private String gateVerifiedBy;
    private LocalDateTime gateVerifiedDate;
    private Boolean isGateVerified;
    // The receiving project's security confirming these goods/vehicle actually arrived at
    // their gate — recorded before a GRN exists for this GIN. Once a GRN is created for it,
    // this is copied onto the GRN's own gate_verified_* columns (see GRNService#createGRN).
    private String arrivalGateVerifiedBy;
    private LocalDateTime arrivalGateVerifiedDate;
    private Boolean isArrivalGateVerified;
    private List<GINItem> items;
}