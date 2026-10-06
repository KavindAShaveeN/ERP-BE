package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class POItem {

    private UUID poItemId;

    private String itemCode;
    private String description;
    private String partNo;
    private String supplierName;

    private Integer uomId;
    private Integer quantity;

    private BigDecimal unitPrice;
    private BigDecimal amount;
}