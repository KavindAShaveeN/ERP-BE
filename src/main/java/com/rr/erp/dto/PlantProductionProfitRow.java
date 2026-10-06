package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Cost and realised profit of one approved production. Revenue is only what has
 * actually been sold — the production's finished goods issued on "Outside Sell"
 * GINs, at the price recorded on each GIN item; unsold stock is not counted as profit.
 */
@Getter
@Setter
public class PlantProductionProfitRow {

    private UUID productionId;
    private String projectCode;
    private String productionCode;
    private LocalDate productionDate;
    private BigDecimal materialCost;
    private BigDecimal otherExpenses;
    private BigDecimal quantityProduced;
    private BigDecimal quantitySold;
    private BigDecimal revenue;
    /** Cost (at this production's unit cost) of the quantity sold. */
    private BigDecimal costOfSold;
}
