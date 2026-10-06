package com.rr.erp.entity;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GRN {

    private UUID grnId;

    @NotNull(message = "GRN code is required")
    private String grnCode;

    private String fromProjectCode;

    @NotNull(message = "Supplier GRN status is required")
    private Boolean isSupplierGRN;

    private String supplierCode;

    private LocalDate invoiceDate;

    // Supplier's invoice number and delivery note number for this receipt.
    private String invoiceNumber;

    private String deliveryNoteNumber;

    @NotNull(message = "Destination project code is required")
    private String toProjectCode;

    private LocalDateTime checkedDate;

    private String checkedBy;

    private LocalDateTime approvedDate;

    private String approvedBy;

    @NotNull(message = "GRN date is required")
    private LocalDate grnDate;

    private String poCode;

    private Boolean isApproved = false;

    private UUID ginId;

    private UUID stockReturnId;

    // The transporting vehicle's asset code (Vehicle asset master), and a free-text mirror of
    // its registration number for display — drives an automatic location-history entry when
    // this GRN is approved and stock is actually received.
    private String vehicleAssetCode;

    private String vehicleNo;

    // Security's confirmation that the incoming goods/vehicle actually arrived through the
    // gate — the first stage a receiving project's security records, ahead of checkedBy.
    private String gateVerifiedBy;

    private LocalDateTime gateVerifiedDate;

    private Boolean isGateVerified = false;

    @Valid
    @NotEmpty(message = "At least one GRN item is required")
    private List<GRNItem> items = new ArrayList<>();
}
