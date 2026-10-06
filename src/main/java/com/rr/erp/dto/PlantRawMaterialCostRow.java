package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Total consumption and cost of one raw material at one plant, across approved productions. */
@Getter
@Setter
public class PlantRawMaterialCostRow {

    private String projectCode;
    private String itemCode;
    private Integer productionCount;
    private BigDecimal totalQuantity;
    private BigDecimal totalCost;
    private BigDecimal avgUnitCost;
    private BigDecimal minUnitCost;
    private BigDecimal maxUnitCost;
}
