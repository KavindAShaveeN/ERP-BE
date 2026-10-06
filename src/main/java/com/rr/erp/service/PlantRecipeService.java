package com.rr.erp.service;

import com.rr.erp.dto.PlantRecipeCalculationResponse;
import com.rr.erp.entity.PlantProductionInput;
import com.rr.erp.entity.PlantProductionOutput;
import com.rr.erp.entity.PlantRecipe;
import com.rr.erp.entity.PlantRecipeInput;
import com.rr.erp.entity.PlantRecipeOutput;
import com.rr.erp.repository.PlantRecipeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PlantRecipeService {

    private final PlantRecipeRepository recipeRepository;

    public PlantRecipeService(PlantRecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @Transactional
    public PlantRecipe createRecipe(PlantRecipe recipe) {

        validateRecipe(recipe);
        applyUomConversions(recipe);

        return recipeRepository.insertRecipe(recipe);
    }

    @Transactional
    public PlantRecipe updateRecipe(UUID recipeId, PlantRecipe recipe) {

        validateRecipe(recipe);
        applyUomConversions(recipe);

        int updatedRows = recipeRepository.updateRecipe(recipeId, recipe);

        if (updatedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found: " + recipeId);
        }

        recipeRepository.deleteInputsByRecipeId(recipeId);
        recipeRepository.deleteOutputsByRecipeId(recipeId);
        recipeRepository.insertInputs(recipeId, recipe.getInputs());
        recipeRepository.insertOutputs(recipeId, recipe.getOutputs());

        return recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found after update"));
    }

    public List<PlantRecipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    public List<PlantRecipe> getRecipesByProjectCode(String projectCode) {
        return recipeRepository.findByProjectCode(projectCode);
    }

    public PlantRecipe getRecipeById(UUID recipeId) {
        return recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found: " + recipeId));
    }

    /**
     * Scales every input and output line of a recipe against the quantity
     * entered for its primary output — e.g. entering 5 m3 of a concrete grade
     * whose recipe is defined per 1 m3 scales every cement/sand/aggregate/water
     * input, and every other by-product output, by a factor of 5. The result
     * seeds a new production record's editable input/output tables; the user
     * can still adjust every quantity before submitting.
     */
    public PlantRecipeCalculationResponse calculate(UUID recipeId, BigDecimal enteredQuantity) {

        if (enteredQuantity == null || enteredQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
        }

        PlantRecipe recipe = getRecipeById(recipeId);

        PlantRecipeOutput primary = recipe.getOutputs().stream()
                .filter(output -> Boolean.TRUE.equals(output.getIsPrimary()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Recipe " + recipeId + " has no primary output defined"
                ));

        BigDecimal factor = enteredQuantity.divide(primary.getQuantity(), 6, RoundingMode.HALF_UP);

        List<PlantProductionInput> inputs = new ArrayList<>();
        for (PlantRecipeInput line : recipe.getInputs()) {

            BigDecimal plannedQuantity = line.getQuantity().multiply(factor).setScale(3, RoundingMode.HALF_UP);
            BigDecimal conversionFactor = line.getConversionFactor();
            BigDecimal stockEquivalentQty = conversionFactor != null && conversionFactor.compareTo(BigDecimal.ZERO) > 0
                    ? plannedQuantity.divide(conversionFactor, 6, RoundingMode.HALF_UP)
                    : plannedQuantity;

            PlantProductionInput input = new PlantProductionInput(
                    null, null, line.getItemCode(), line.getUomId(), plannedQuantity, plannedQuantity,
                    conversionFactor, stockEquivalentQty
            );
            inputs.add(input);
        }

        List<PlantProductionOutput> outputs = new ArrayList<>();
        for (PlantRecipeOutput line : recipe.getOutputs()) {

            BigDecimal plannedQuantity = line.getQuantity().multiply(factor).setScale(3, RoundingMode.HALF_UP);
            BigDecimal conversionFactor = line.getConversionFactor();
            BigDecimal stockEquivalentQty = conversionFactor != null && conversionFactor.compareTo(BigDecimal.ZERO) > 0
                    ? plannedQuantity.divide(conversionFactor, 6, RoundingMode.HALF_UP)
                    : plannedQuantity;

            PlantProductionOutput output = new PlantProductionOutput(
                    null, null, line.getItemCode(), line.getUomId(), plannedQuantity, plannedQuantity,
                    Boolean.TRUE.equals(line.getIsWaste()), conversionFactor, stockEquivalentQty
            );
            outputs.add(output);
        }

        return new PlantRecipeCalculationResponse(inputs, outputs);
    }

    private void validateRecipe(PlantRecipe recipe) {

        long primaryCount = recipe.getOutputs().stream()
                .filter(output -> Boolean.TRUE.equals(output.getIsPrimary()))
                .count();

        if (primaryCount != 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "A recipe must have exactly one primary output"
            );
        }
    }

    /**
     * A recipe line's conversionFactor is entered by the user only when its working unit
     * (uomId, e.g. KG at the mixer) differs from the item's stock UOM: "1 unit of the item's
     * stock UOM = conversionFactor units of uomId". stockEquivalentQty (the amount actually
     * moved against stock) is computed and persisted here, exactly like GRNService applies
     * PO UOM conversions -- when no factor is supplied, the line is assumed to already be in
     * the item's stock UOM, so the equivalent quantity is just the quantity itself.
     */
    private void applyUomConversions(PlantRecipe recipe) {

        for (PlantRecipeInput input : recipe.getInputs()) {
            if (input.getConversionFactor() == null || input.getConversionFactor().compareTo(BigDecimal.ZERO) <= 0) {
                input.setConversionFactor(null);
                input.setStockEquivalentQty(input.getQuantity());
            } else {
                input.setStockEquivalentQty(
                        input.getQuantity().divide(input.getConversionFactor(), 6, RoundingMode.HALF_UP)
                );
            }
        }

        for (PlantRecipeOutput output : recipe.getOutputs()) {
            if (output.getConversionFactor() == null || output.getConversionFactor().compareTo(BigDecimal.ZERO) <= 0) {
                output.setConversionFactor(null);
                output.setStockEquivalentQty(output.getQuantity());
            } else {
                output.setStockEquivalentQty(
                        output.getQuantity().divide(output.getConversionFactor(), 6, RoundingMode.HALF_UP)
                );
            }
        }
    }
}
