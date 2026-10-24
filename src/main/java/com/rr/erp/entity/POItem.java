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
    private String remark;
    private String supplierName;

    private Integer uomId;
    private Integer quantity;

    private BigDecimal unitPrice;
    private BigDecimal amount;

    /** Not a column — the job card 3P service row this line is billing (service POs only). */
    private UUID threePServiceId;
}