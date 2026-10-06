package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One row in a project store item's transaction history — a PO, GRN, GIN, intra-project issue
 * (or its return), stock return, stock adjustment, or plant production line that touched (or,
 * for PO, will touch) a given project + item code. Used to reconstruct "how did the balance
 * get here" alongside the authoritative {@code quantity_on_hand} column, which this never
 * overrides.
 */
@Getter
@Setter
public class StockMovementResponse {

    /** PO | GRN | GIN | INTRA_ISSUE | INTRA_ISSUE_RETURN | STOCK_RETURN | ADJUSTMENT |
     * PLANT_PRODUCTION (finished goods produced) | PLANT_PRODUCTION_CONSUMPTION (raw
     * material consumed) | JOB_ISSUE (job card issue) | JOB_ISSUE_RETURN | FUEL_ISSUE */
    private String type;

    private String docCode;
    private LocalDate date;

    /** The project this row is being reported for. */
    private String projectCode;
    private String projectName;

    /** The other side of a transfer, when there is one (nullable). */
    private String counterpartyProjectCode;
    private String counterpartyProjectName;

    /** IN | OUT | NEUTRAL (NEUTRAL for PO — it doesn't move stock on its own). */
    private String direction;

    private BigDecimal quantity;
    private Integer uomId;
    private Boolean isApproved;
    private String remarks;

    /** Supplier side of a PO / supplier GRN / supplier stock return — null for a purely
     * inter-project movement (GIN, intra-project issue, internal transfer, adjustment). */
    private String supplierCode;
    private String supplierName;

    private BigDecimal unitPrice;
    /** Line total — quantity × unit price (or the document's own stored amount/value). */
    private BigDecimal amount;
}
