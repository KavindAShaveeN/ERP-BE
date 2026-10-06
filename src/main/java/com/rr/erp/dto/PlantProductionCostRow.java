package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** The cost of one finished-goods batch a production record opened — production cost per finished product. */
@Getter
@Setter
public class PlantProductionCostRow {

    private String projectCode;
    private String productionCode;
    private LocalDate productionDate;
    private String itemCode;
    private String batchCode;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal totalCost;
}
