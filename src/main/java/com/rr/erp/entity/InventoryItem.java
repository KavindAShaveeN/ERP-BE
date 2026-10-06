package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InventoryItem {

    private Integer inventoryItemId;
    private Integer itemCodeId;
    private Integer uomId;

    private BigDecimal quantityOnHand;
    private BigDecimal maximumStockLevel;
    private BigDecimal averageUnitPrice;
    private BigDecimal lastPurchasePrice;

    private String remarks;
    private Boolean isActive;
}
