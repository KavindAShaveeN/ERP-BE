package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The main-store issue-planning grid: one row per item, one column per pending MR, plus the
 * stock position of each item in the store being planned. See IssuePlanningService.
 */
@Getter
@Setter
public class IssuePlanGridResponse {

    private String storeCode;
    private List<MrColumn> mrs = new ArrayList<>();
    private List<ItemRow> items = new ArrayList<>();

    @Getter
    @Setter
    public static class MrColumn {
        private UUID mrId;
        private String mrCode;
        private String requestingProjectCode;
        private String requestingProjectName;
        private LocalDateTime requestedDate;
        /** The earliest required date among this MR's pending lines. */
        private LocalDate requiredDate;
        /** Urgent beats Normal; used by auto-allocate. */
        private String priority;
    }

    @Getter
    @Setter
    public static class ItemRow {
        private String itemCode;
        private String description;
        private Integer uomId;
        private String uomName;
        private BigDecimal onHand = BigDecimal.ZERO;
        /** Held for MR lines by active allocations (all MRs, including those in this grid). */
        private BigDecimal reserved = BigDecimal.ZERO;
        /** onHand - reserved. */
        private BigDecimal available = BigDecimal.ZERO;
        /** Issued from this store by authorized GINs that have no GRN yet. */
        private BigDecimal inTransit = BigDecimal.ZERO;
        /** Ordered on active, approved POs for this store minus what approved GRNs received. */
        private BigDecimal incomingPo = BigDecimal.ZERO;
        private BigDecimal reorderLevel = BigDecimal.ZERO;
        private List<Cell> cells = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Cell {
        private UUID mrId;
        private UUID mrItemId;
        private String size;
        private BigDecimal requestedQty;
        private BigDecimal issuedQty;
        /** requested - issued. */
        private BigDecimal remainingQty;
        /** Remaining quantity that was already forwarded to Purchasing (agreed forward plan). */
        private BigDecimal forwardedQty;
        /** remaining - forwarded: the most that can still be allocated from this store. */
        private BigDecimal plannableQty;
        /** Currently reserved for this line (active allocations). */
        private BigDecimal allocatedQty;
        /** The requesting site's own balance of this item. */
        private BigDecimal siteBalance;
        /** Issued from this store to the requesting site, not yet received by it. */
        private BigDecimal siteInTransit;
        private String priority;
        private LocalDate requiredDate;
        private String lineStatus;
    }
}
