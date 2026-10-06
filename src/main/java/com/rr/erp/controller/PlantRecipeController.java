package com.rr.erp.controller;

import com.rr.erp.dto.PlantRecipeCalculationRequest;
import com.rr.erp.dto.PlantRecipeCalculationResponse;
import com.rr.erp.entity.PlantRecipe;
import com.rr.erp.service.PlantRecipeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/plant-recipe")
@CrossOrigin(origins = "*")
public class PlantRecipeController {

    private final PlantRecipeService recipeService;

    public PlantRecipeController(PlantRecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping
    public ResponseEntity<PlantRecipe> createRecipe(@Valid @RequestBody PlantRecipe recipe) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(recipeService.createRecipe(recipe));
    }

    @GetMapping("/all")
    public ResponseEntity<List<PlantRecipe>> getAllRecipes() {
        return ResponseEntity.ok(recipeService.getAllRecipes());
    }

    @GetMapping("/by-project/{projectCode}")
    public ResponseEntity<List<PlantRecipe>> getRecipesByProjectCode(@PathVariable String projectCode) {
        return ResponseEntity.ok(recipeService.getRecipesByProjectCode(projectCode));
    }

    @GetMapping("/{recipeId}")
    public ResponseEntity<PlantRecipe> getRecipeById(@PathVariable UUID recipeId) {
        return ResponseEntity.ok(recipeService.getRecipeById(recipeId));
    }

    @PutMapping("/{recipeId}")
    public ResponseEntity<PlantRecipe> updateRecipe(
            @PathVariable UUID recipeId,
            @Valid @RequestBody PlantRecipe recipe
    ) {
        return ResponseEntity.ok(recipeService.updateRecipe(recipeId, recipe));
    }

    @PostMapping("/calculate")
    public ResponseEntity<PlantRecipeCalculationResponse> calculate(
            @RequestBody PlantRecipeCalculationRequest request
    ) {
        return ResponseEntity.ok(
                recipeService.calculate(request.getRecipeId(), request.getQuantity())
        );
    }
}
