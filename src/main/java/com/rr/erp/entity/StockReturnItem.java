package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class StockReturnItem implements PackCarrier {

    private UUID stockReturnItemId;
    private String itemCode;
    private String description;
    private String size;
    private Integer uomId;
    private BigDecimal quantity;
    private String remarks;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String assetCode;

    // Ticked/unticked pack items of the asset on this line; stored in asset_pack_transaction.
    private java.util.List<PackSelection> packItems;

    // Non-stock items only — the specific batch (see StockBatch#description) the user picked
    // to return, instead of a blind FIFO draw. Not persisted on stock_return_item itself
    // (recorded via stock_action_allocation); only used at apply time to route the draw.
    private UUID stockBatchId;
}