package com.rr.erp.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A lot's permanent identity. Created ONLY when stock genuinely enters the
 * system with no prior batch to attribute it to — a supplier GRN line, or a
 * positive stock adjustment. batch_code, origin_date and unit_cost never
 * change once created. Not scoped to a project — see {@link StockBatchLocation}
 * for where a batch's remaining quantity currently sits.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockBatch {

    private UUID stockBatchId;
    private String batchCode;
    private String itemCode;
    private LocalDate originDate;
    private String sourceType;
    private UUID sourceId;
    private String sourceReference;
    private BigDecimal originalQty;
    private BigDecimal unitCost;

    // Dimensional items only (see com.rr.erp.util.DimensionalItems) — the size and bar/piece
    // count this batch was opened with (at GRN time: the supplier-received size; at
    // cut-return time: a new size born from cutting). widthM is only populated for
    // area-tracked (sheet) items. See stock_batch_dimensional_tracking.sql.
    private BigDecimal lengthValue;
    private BigDecimal widthValue;
    private Integer pieceCount;

    // The GRN line's own description at the moment this batch was opened — lets several
    // different physical items sharing one item_code (mainly non-stock items) be told
    // apart once in stock. See stock_batch_description.sql.
    private String description;

    // Expiry date for special items that expire — copied from the GRN line at the moment this
    // batch was opened (cut-return batches inherit it from their source batch). Null for
    // everything else. See stock_batch_expiry_date.sql.
    private LocalDate expiryDate;
}
