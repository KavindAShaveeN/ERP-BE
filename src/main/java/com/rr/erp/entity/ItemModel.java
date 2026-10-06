package com.rr.erp.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemModel {

    private Long itemModelId;

    @NotBlank(message = "Model code is required")
    private String itemModelCode;

    @NotBlank(message = "Model name is required")
    private String itemModelName;

    @NotNull(message = "Brand id is required")
    private Long ItemBrandId;
}
