package com.rr.erp.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlantRecipe {

    private UUID recipeId;

    @NotBlank(message = "Recipe code is required")
    private String recipeCode;

    @NotBlank(message = "Recipe name is required")
    private String recipeName;

    // The plant is simply the project this recipe belongs to — there is no
    // separate plant registration; the signed-in user's active project is
    // the plant (see useActiveProject on the frontend).
    @NotBlank(message = "Project code is required")
    private String projectCode;

    // Concrete grade or other product-type distinguisher, where applicable.
    private String productType;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    private Boolean isActive = true;

    private String createdBy;

    private LocalDateTime createdDate;

    @Valid
    @NotEmpty(message = "At least one raw material input is required")
    private List<PlantRecipeInput> inputs = new ArrayList<>();

    @Valid
    @NotEmpty(message = "At least one finished-good output is required")
    private List<PlantRecipeOutput> outputs = new ArrayList<>();
}
