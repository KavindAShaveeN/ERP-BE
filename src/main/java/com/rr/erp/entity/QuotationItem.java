package com.rr.erp.entity;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class QuotationItem {

    private UUID quotationItemId;
    private UUID quotationId;
    private UUID quotationRequestItemId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String remark;
    private Boolean isSelected;
}
