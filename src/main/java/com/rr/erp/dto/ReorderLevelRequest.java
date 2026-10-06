package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ReorderLevelRequest {
    private String projectCode;
    private String itemCode;
    private BigDecimal reorderLevel;
    private BigDecimal reorderQuantity;
}
