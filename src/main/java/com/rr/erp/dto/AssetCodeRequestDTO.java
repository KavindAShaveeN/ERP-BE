package com.rr.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetCodeRequestDTO {

    @NotNull(message = "Item category ID is required")
    private Long itemCategoryId;

    @NotNull(message = "Item sub category ID is required")
    private Long itemSubCategoryId;

    @NotBlank(message = "Asset code is required")
    @Size(max = 100, message = "Asset code cannot exceed 100 characters")
    private String assetCodeCode;
    private Long itemCodeId;
}
