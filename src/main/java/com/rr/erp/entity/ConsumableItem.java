package com.rr.erp.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ConsumableItem {

    private Long consumableItemId;

    @NotNull(message = "Item code id is required")
    private Long itemCodeId;

    @NotNull(message = "UOM id is required")
    private Integer uomId;

    @NotNull(message = "Unit price is required")
    @PositiveOrZero(message = "Unit price cannot be negative")
    private BigDecimal unitPrice;

    @NotNull(message = "Current stock is required")
    @PositiveOrZero(message = "Current stock cannot be negative")
    private Integer currentStock;

    private Boolean isActive;

    private String remarks;

    private String partNumber;
}
