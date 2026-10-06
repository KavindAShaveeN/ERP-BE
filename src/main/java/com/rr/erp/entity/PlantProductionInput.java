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
public class PlantProductionInput {

    private UUID productionInputId;

    private UUID productionId;

    @NotBlank(message = "Item code is required")
    private String itemCode;

    @NotNull(message = "UOM ID is required")
    private Integer uomId;

    // Recipe-calculated suggestion; null for manual (no-recipe) production.
    private BigDecimal plannedQuantity;

    @NotNull(message = "Consumed quantity is required")
    @Positive(message = "Consumed quantity must be greater than zero")
    private BigDecimal consumedQuantity;

    // Editable at production time (recipe-seeded default, adjustable per batch since actual
    // density/weight can vary) -- see PlantRecipeInput#conversionFactor for the convention.
    private BigDecimal conversionFactor;

    // consumedQuantity / conversionFactor, computed and persisted at save time in the item's
    // stock UOM -- this, not consumedQuantity, is what is actually debited from stock
    // (see PlantProductionService#applyStockMovements).
    private BigDecimal stockEquivalentQty;
}
