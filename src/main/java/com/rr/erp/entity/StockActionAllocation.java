package com.rr.erp.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * How much of one stock-reducing action — a GIN, an intra-project issue, a
 * negative stock adjustment, or the "from" side of a stock return — was
 * drawn from one {@link StockBatch}, and at what cost. The FIFO trail behind
 * an action's total cost. Named "action" rather than "issue" because a GIN
 * (Goods Issued Note) is only one of the four action types this covers.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockActionAllocation {

    private UUID stockActionAllocationId;
    private UUID stockBatchId;
    private String actionType;
    private UUID actionId;
    private UUID actionItemId;
    private String itemCode;
    private BigDecimal qtyTaken;
    private BigDecimal unitCost;
}
