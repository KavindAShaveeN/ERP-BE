package com.rr.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * One resulting size produced by cutting an issued dimensional-item batch (see
 * com.rr.erp.util.DimensionalItems) and returning the offcuts — e.g. issuing 2x6m bars
 * might come back as one CutReturnResultLine for "1m x4" and another for "2m x1".
 * Passed into StockBatchService#recordCutReturn.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CutReturnResultLine {

    private BigDecimal lengthM;
    private BigDecimal widthM;
    private Integer pieceCount;
    private BigDecimal unitCost;
}
