package com.rr.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * One batch as seen from a single project's store — the batch's fixed
 * identity (code, origin date, cost) joined to how much of it currently
 * sits at that project. What GET /api/stock-batches/{projectCode}/{itemCode} returns.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockBatchView {

    private UUID stockBatchId;
    private String batchCode;
    private String itemCode;
    private LocalDate originDate;
    private String sourceType;
    private String sourceReference;
    private BigDecimal unitCost;
    private String projectCode;
    private BigDecimal qtyRemaining;

    // Dimensional items only. Named lengthM/widthM (not lengthValue/widthValue, unlike the
    // StockBatch entity) to match the existing frontend StockBatch TS interface's field
    // naming (ERPRR/src/types/lookups.ts) so the JSON shape needs no frontend rename.
    private BigDecimal lengthM;
    private BigDecimal widthM;
    private Integer pieceCount;
    private Integer pieceCountRemaining;

    // See StockBatch#description — mainly used to tell non-stock items sharing one
    // item_code apart when picking which batch to issue from.
    private String description;

    // See StockBatch#expiryDate — null for items that don't expire.
    private LocalDate expiryDate;
}
