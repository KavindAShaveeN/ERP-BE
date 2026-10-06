package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Opening/consumed/produced/closing quantity for one item at one plant over a
 * date range. closingQuantity is the item's real, current project_store
 * balance; openingQuantity is derived as closing - produced + consumed within
 * the range. This is exact when the item's only movements at this plant are
 * plant-production ones; if GRN/GIN also moved this item during the same
 * range, the opening figure absorbs that difference too, since the schema
 * keeps current on-hand balances rather than a fully dated stock ledger to
 * replay from (the same constraint every other stock report in this system
 * already works within).
 */
@Getter
@Setter
public class PlantStockSummaryRow {

    private String itemCode;
    private BigDecimal openingQuantity;
    private BigDecimal consumedQuantity;
    private BigDecimal producedQuantity;
    private BigDecimal closingQuantity;
}
