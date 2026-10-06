package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * One input or output line of one production record, flattened for reporting.
 * The same shape backs several of the requested Plant reports — daily
 * production, raw-material consumption, finished-goods production, recipe
 * quantity vs. actual (planned vs. actual here is the variance), and
 * wastage/production loss — which differ only in which filters/lineType are
 * applied, not in the underlying query.
 */
@Getter
@Setter
public class PlantProductionLineRow {

    private UUID productionId;
    private String productionCode;
    private LocalDate productionDate;
    private String status;
    private String projectCode;
    private UUID recipeId;
    private String recipeName;

    // "INPUT" or "OUTPUT"
    private String lineType;
    private String itemCode;
    private Integer uomId;
    private BigDecimal plannedQuantity;
    // consumedQuantity for an INPUT line, producedQuantity for an OUTPUT line.
    private BigDecimal actualQuantity;
    // Always false for INPUT lines.
    private Boolean isWaste;
}
