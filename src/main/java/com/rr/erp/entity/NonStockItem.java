package com.rr.erp.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NonStockItem {

    private Long nonStockItemId;

    @NotNull(message = "Item code id is required")
    private Long itemCodeId;

    @NotNull(message = "UOM id is required")
    private Integer uomId;

    private Boolean isActive;

    private String remarks;
}
