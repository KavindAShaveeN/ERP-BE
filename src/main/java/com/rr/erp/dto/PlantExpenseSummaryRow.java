package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Total of the "other expenses" entered under one description at one plant. */
@Getter
@Setter
public class PlantExpenseSummaryRow {

    private String projectCode;
    private String description;
    private Integer entryCount;
    private BigDecimal totalAmount;
}
