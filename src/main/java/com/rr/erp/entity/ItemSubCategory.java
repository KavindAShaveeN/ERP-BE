package com.rr.erp.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemSubCategory {

    private Long ItemSubCategoryId;

    @NotBlank(message = "Sub category code is required")
    private String ItemSubCategoryCode;

    @NotBlank(message = "Sub category name is required")
    private String ItemSubCategoryName;
    private Long ItemCategoryId;

    /** Highest sequence number already used for this type code outside this system (e.g.
     * from the legacy asset register), so newly generated codes continue that numbering
     * instead of restarting at 1. Zero for ordinary, non-legacy sub categories. */
    private Integer startingSequence;
}
