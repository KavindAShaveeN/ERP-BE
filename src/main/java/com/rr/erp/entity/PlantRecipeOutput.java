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
public class PlantRecipeOutput {

    private UUID recipeOutputId;

    private UUID recipeId;

    @NotBlank(message = "Item code is required")
    private String itemCode;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    @NotNull(message = "UOM ID is required")
    private Integer uomId;

    // Exactly one output per recipe must be primary — the quantity a produced
    // batch is scaled against (see PlantRecipeService#calculate).
    private Boolean isPrimary = false;

    // Waste/loss outputs don't add to available stock (see PlantProductionOutput's
    // equivalent flag) — kept separate from isPrimary so a waste line is never mistaken
    // for a sellable by-product.
    private Boolean isWaste = false;

    // See PlantRecipeInput#conversionFactor -- same "1 stock UOM = conversionFactor
    // units of uomId" convention, for a produced item worked out in a different unit
    // than its stock UOM (e.g. ready-mix judged in KG at the mixer, stocked in M3).
    private BigDecimal conversionFactor;

    // quantity / conversionFactor, in the item's stock UOM -- see PlantRecipeInput#stockEquivalentQty.
    private BigDecimal stockEquivalentQty;
}
