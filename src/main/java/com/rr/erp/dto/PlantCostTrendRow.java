package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Quantity-weighted unit cost of one finished product at one plant in one month — for cost-over-time variance. */
@Getter
@Setter
public class PlantCostTrendRow {

    private String projectCode;
    private String itemCode;
    /** First day of the month the productions fall in. */
    private LocalDate month;
    private Integer productionCount;
    private BigDecimal quantity;
    private BigDecimal totalCost;
    private BigDecimal avgUnitCost;
}
