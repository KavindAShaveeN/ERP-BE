package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class StockAdjustmentItem {

    private UUID stockAdjustmentItemId;

    private String itemCode;
    private String description;

    private Integer uomId;

    private BigDecimal adjustmentQuantity;

    private BigDecimal unitPrice;
    private BigDecimal adjustmentValue;

    private String remarks;

    // Dimensional items only (see com.rr.erp.util.DimensionalItems) — the size of the
    // bars/pieces being found (positive adjustment) or written off (negative adjustment).
    private BigDecimal lengthM;
    private BigDecimal widthM;

    // Set for a registered asset line: quantity is +1 (found) or -1 (written off) and stock is untouched.
    private String assetCode;

    // Non-stock items only, negative (write-off) adjustments — the specific batch (see
    // StockBatch#description) the user picked to write off, rather than a blind FIFO walk.
    // Not persisted on stock_adjustment_item itself (recorded via stock_action_allocation).
    private UUID stockBatchId;
}
