package com.rr.erp.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemBrand {

    private Long itemBrandId;

    @NotBlank(message = "Brand code is required")
    private String itemBrandCode;

    @NotBlank(message = "Brand name is required")
    private String itemBrandName;

    @NotNull(message = "Sub sub category id is required")
    private Long itemSubSubCategoryId;
}
