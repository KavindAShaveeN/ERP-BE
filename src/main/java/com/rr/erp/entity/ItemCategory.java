package com.rr.erp.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemCategory {

    private Long itemCategoryId;

    private Integer itemTypeId;

    @NotBlank(message = "Category code is required")
    private String itemCategoryCode;

    @NotBlank(message = "Category name is required")
    private String itemCategoryName;

}
