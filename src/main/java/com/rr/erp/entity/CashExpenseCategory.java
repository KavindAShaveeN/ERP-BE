package com.rr.erp.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CashExpenseCategory {

    private UUID categoryId;

    @NotBlank(message = "Category code is required")
    @Size(max = 30, message = "Category code must be at most 30 characters")
    private String categoryCode;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must be at most 100 characters")
    private String categoryName;

    private Boolean isActive;
}
