package com.rr.erp.controller;

import com.rr.erp.entity.Asset;
import com.rr.erp.service.PlantEquipmentAssetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets/plant-equipment")
@CrossOrigin(origins = "*")
public class PlantEquipmentAssetController {

    private final PlantEquipmentAssetService plantEquipmentAssetService;

    public PlantEquipmentAssetController(PlantEquipmentAssetService plantEquipmentAssetService) {
        this.plantEquipmentAssetService = plantEquipmentAssetService;
    }

    @PostMapping
    public ResponseEntity<Asset> createPlantEquipmentAsset(@Valid @RequestBody Asset request) {
        Asset created = plantEquipmentAssetService.createPlantEquipmentAsset(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Asset>> getAllPlantEquipmentAssets() {
        return ResponseEntity.ok(plantEquipmentAssetService.getAllPlantEquipmentAssets());
    }

    @GetMapping("/{assetCode}")
    public ResponseEntity<Asset> getPlantEquipmentAsset(@PathVariable String assetCode) {
        return ResponseEntity.ok(plantEquipmentAssetService.getPlantEquipmentAsset(assetCode));
    }

    @PutMapping("/{assetCode}")
    public ResponseEntity<Asset> updatePlantEquipmentAsset(
            @PathVariable String assetCode,
            @Valid @RequestBody Asset request
    ) {
        return ResponseEntity.ok(plantEquipmentAssetService.updatePlantEquipmentAsset(assetCode, request));
    }
}
