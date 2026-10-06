package com.rr.erp.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemSubSubCategory {

    private Long ItemSubSubCategoryId;

    @NotBlank(message = "Sub sub category code is required")
    private String itemSubSubCategoryCode;

    @NotBlank(message = "Sub sub category name is required")
    private String itemSubSubCategoryName;

    @NotNull(message = "Sub category id is required")
    private Long itemSubCategoryId;
}
