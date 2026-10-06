package com.rr.erp.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlantProductionOutput {

    private UUID productionOutputId;

    private UUID productionId;

    @NotBlank(message = "Item code is required")
    private String itemCode;

    @NotNull(message = "UOM ID is required")
    private Integer uomId;

    // Recipe-calculated suggestion; null for manual (no-recipe) production.
    private BigDecimal plannedQuantity;

    @NotNull(message = "Produced quantity is required")
    @Positive(message = "Produced quantity must be greater than zero")
    private BigDecimal producedQuantity;

    // Waste/production-loss output: recorded here for reporting but never
    // credited into project_store — see PlantProductionService.
    private Boolean isWaste = false;

    // Editable at production time -- see PlantRecipeInput#conversionFactor for the convention.
    private BigDecimal conversionFactor;

    // producedQuantity / conversionFactor, computed and persisted at save time in the item's
    // stock UOM -- this, not producedQuantity, is what is actually credited into stock
    // (see PlantProductionService#applyStockMovements).
    private BigDecimal stockEquivalentQty;
}
