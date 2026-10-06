package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** One plant's financial performance across approved productions in the period. */
@Getter
@Setter
public class PlantFinanceSummaryRow {

    private String projectCode;
    private Integer productionCount;
    private BigDecimal materialCost;
    private BigDecimal otherExpenses;
    private BigDecimal quantityProduced;
    private BigDecimal wasteQuantity;
    private BigDecimal quantitySold;
    private BigDecimal revenue;
    private BigDecimal costOfSold;
}
