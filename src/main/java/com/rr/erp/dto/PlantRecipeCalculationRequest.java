package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class PlantRecipeCalculationRequest {

    private UUID recipeId;

    // The quantity entered for the recipe's primary output item; every other
    // input/output line scales by (quantity / primary output's recipe quantity).
    private BigDecimal quantity;
}
