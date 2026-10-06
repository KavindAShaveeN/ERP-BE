package com.rr.erp.entity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class PO {

    private UUID poId;

    private String poCode;
    private LocalDate poDate;

    private String supplierCode;

    private String projectCode;
    private String billToProjectCode;

    /** The specific delivery site for this PO — one of the deliver-to project's
     * project_location rows. Editable independently of the project's locations list. */
    private String deliveryLocation;

    private String freight;

    private LocalDate orderDueDate;
    private String paymentTerm;
    private String paymentType;
    private Integer currencyId;
    private String supplierRefNo;

    private String VATRegNo;
    private String SVATNo;

    private Boolean ssclApplicable;
    private BigDecimal ssclPercentage;
    private BigDecimal ssclAmount;

    private BigDecimal vatPercentage;
    private BigDecimal vatAmount;

    private BigDecimal totalValue;

    private LocalDateTime requestedDate;
    private String requestedBy;

    private LocalDateTime approvedDate;
    private String approvedBy;

    private Boolean isApproved;
    private String approvalStatus;

    private String status;
    private String statusReason;
    private String statusChangedBy;
    private LocalDateTime statusChangedDate;

    private UUID mrId;

    private String mrRequestingProjectCode;

    private String remarks;

    private List<POItem> items;

    /**
     * Not a po-table column — backed by the po_material_request join table (see
     * POMaterialRequestRepository) and populated by POService on every read. A PO can
     * consolidate items from several MRs, in addition to (or instead of) the single
     * "originating" MR captured in mrId.
     */
    private List<UUID> relatedMrIds;

    /**
     * Not a stored column — Total Value + SSCL Amount + VAT Amount, computed on read so
     * it can never drift from its inputs.
     */
    public BigDecimal getFinalPoValue() {

        BigDecimal total = totalValue != null ? totalValue : BigDecimal.ZERO;
        BigDecimal sscl = ssclAmount != null ? ssclAmount : BigDecimal.ZERO;
        BigDecimal vat = vatAmount != null ? vatAmount : BigDecimal.ZERO;

        return total.add(sscl).add(vat);
    }
}
