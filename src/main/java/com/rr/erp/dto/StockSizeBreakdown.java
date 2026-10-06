package com.rr.erp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * How much of one dimensional item's stock (see com.rr.erp.util.DimensionalItems), at one
 * project, currently sits at one exact size — e.g. "6m x8 bars, 1m x4 pieces, 2m x1 piece"
 * for the same item code is three of these. Powers GET
 * /api/stock-batches/{projectCode}/{itemCode}/sizes.
 */
@Getter
@Setter
@NoArgsConstructor
public class StockSizeBreakdown {

    private BigDecimal lengthM;
    private BigDecimal widthM;
    private BigDecimal totalQtyRemaining;
    private Integer totalPieceCountRemaining;
}
