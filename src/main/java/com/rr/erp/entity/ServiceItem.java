package com.rr.erp.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceItem {

    private Long serviceItemId;

    @NotNull(message = "Item code id is required")
    private Long itemCodeId;

    private Boolean isActive;

    private String remarks;
}
