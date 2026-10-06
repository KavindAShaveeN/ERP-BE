package com.rr.erp.controller;

import com.rr.erp.dto.PlantProductionReversalRequest;
import com.rr.erp.entity.PlantProduction;
import com.rr.erp.service.PlantProductionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/plant-production")
@CrossOrigin(origins = "*")
public class PlantProductionController {

    private final PlantProductionService productionService;

    public PlantProductionController(PlantProductionService productionService) {
        this.productionService = productionService;
    }

    @PostMapping
    public ResponseEntity<PlantProduction> createProduction(@Valid @RequestBody PlantProduction production) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productionService.createProduction(production));
    }

    @GetMapping("/all")
    public ResponseEntity<List<PlantProduction>> getAllProductions() {
        return ResponseEntity.ok(productionService.getAllProductions());
    }

    @GetMapping("/by-project/{projectCode}")
    public ResponseEntity<List<PlantProduction>> getProductionsByProjectCode(@PathVariable String projectCode) {
        return ResponseEntity.ok(productionService.getProductionsByProjectCode(projectCode));
    }

    @GetMapping("/{productionId}")
    public ResponseEntity<PlantProduction> getProductionById(@PathVariable UUID productionId) {
        return ResponseEntity.ok(productionService.getProductionById(productionId));
    }

    @PutMapping("/{productionId}")
    public ResponseEntity<PlantProduction> updateProduction(
            @PathVariable UUID productionId,
            @Valid @RequestBody PlantProduction production
    ) {
        return ResponseEntity.ok(productionService.updateProduction(productionId, production));
    }

    @PostMapping("/{productionId}/reverse")
    public ResponseEntity<PlantProduction> reverseProduction(
            @PathVariable UUID productionId,
            @RequestBody PlantProductionReversalRequest request
    ) {
        return ResponseEntity.ok(
                productionService.reverseProduction(productionId, request.getReversedBy(), request.getReason())
        );
    }
}
