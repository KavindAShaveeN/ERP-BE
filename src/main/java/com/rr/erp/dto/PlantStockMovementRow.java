package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One project-scoped (plant-scoped) stock movement — a raw-material debit or a finished-good credit, both sourced from PLANT_PRODUCTION. */
@Getter
@Setter
public class PlantStockMovementRow {

    private String projectCode;
    private String itemCode;
    // "DEBIT" (raw material consumed) or "CREDIT" (finished good produced)
    private String movementType;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private LocalDate movementDate;
    private String productionCode;
}
