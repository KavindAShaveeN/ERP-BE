package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class GINItem implements PackCarrier {

    private UUID ginItemId;
    private UUID ginId;
    private String itemCode;
    private String description;
    private String size;
    private Integer uom;
    private BigDecimal quantity;
    private String remarks;
    private Integer issueItemTypeId;
    private BigDecimal unitPrice;
    private BigDecimal amount;

    // Dimensional items only (see com.rr.erp.util.DimensionalItems) — which exact size to
    // draw down FIFO from (e.g. the 6m bars, not the 1m offcuts).
    private BigDecimal lengthM;
    private BigDecimal widthM;

    // Non-stock items only — the specific batch (see StockBatch#description) the user picked
    // to issue from, e.g. "NS-001 — office chair, blue" rather than "NS-001 — office chair,
    // red". Not persisted on gin_item itself (which batch was drawn is already recorded via
    // stock_action_allocation); only used at issue time to route the draw to that exact batch
    // instead of a blind FIFO walk. Null for every other item type.
    private UUID stockBatchId;

    // Set when this line moves one specific registered asset (quantity 1). Such a line bypasses
    // the quantity stock engine — the asset's location history tracks it instead
    // (see asset_movement_tracking.sql).
    private String assetCode;
    // The MR line this GIN line fulfils (nullable). Lets one GIN cover several MRs going to the
    // same site; for a single-MR GIN the header's mrId still applies as before.
    private UUID mrItemId;

    // Ticked/unticked pack items of the asset on this line; stored in asset_pack_transaction.
    private java.util.List<PackSelection> packItems;
}