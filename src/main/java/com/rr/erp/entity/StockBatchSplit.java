package com.rr.erp.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * One cut-return transformation: the pieces of a source batch that were consumed by
 * cutting, and the resulting new batch (a distinct new size) created from that cut. One
 * cut-return event with multiple resulting sizes (e.g. 1m x4 AND 2m x1 from the same
 * source bars) produces one row per resulting size, all sharing the same
 * intraProjectIssueReturnId + sourceStockBatchId. See stock_batch_dimensional_tracking.sql.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockBatchSplit {

    private UUID stockBatchSplitId;
    private UUID intraProjectIssueReturnId;
    private UUID sourceStockBatchId;
    private Integer sourcePieceCountConsumed;
    private BigDecimal sourceQtyConsumed;
    private UUID resultStockBatchId;
    private BigDecimal resultQtyProduced;
    private BigDecimal wastageQty;
}
