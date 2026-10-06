package com.rr.erp.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemCode {

    private Long itemCodeId;

    @NotBlank(message = "Item code is required")
    private String itemCodeCode;

    @NotBlank(message = "Item code name is required")
    private String itemCodeName;

    @NotNull(message = "Category id is required")
    private Long itemCategoryId;
    @NotNull(message = "Sub category id is required")
    private Long itemSubCategoryId;
    private Long itemSubSubCategoryId;
    private Long itemBrandId;
    private Long itemModelId;
    private Long itemOptionalOneId;
    private Long itemOptionalTwoId;
    private Long itemOptionalThreeId;
}
