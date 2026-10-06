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
public class PlantRecipeInput {

    private UUID recipeInputId;

    private UUID recipeId;

    @NotBlank(message = "Item code is required")
    private String itemCode;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    @NotNull(message = "UOM ID is required")
    private Integer uomId;

    // Set when this line is worked out in a different unit than the item's stock UOM
    // (e.g. cement weighed in KG at the mixer while stock is tracked in bags): "1 unit of
    // the item's stock UOM = conversionFactor units of this line's uomId". Null/1 when the
    // line is already entered in the item's stock UOM (see PlantRecipeService).
    private BigDecimal conversionFactor;

    // quantity / conversionFactor, computed and persisted at save time -- the amount this
    // line actually represents in the item's stock UOM. Equal to quantity when no conversion
    // applies. This, not quantity, is what production debits/credits against stock.
    private BigDecimal stockEquivalentQty;
}
