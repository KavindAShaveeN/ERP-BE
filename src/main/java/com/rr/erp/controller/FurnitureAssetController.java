package com.rr.erp.controller;

import com.rr.erp.entity.Asset;
import com.rr.erp.service.FurnitureAssetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets/furniture")
@CrossOrigin(origins = "*")
public class FurnitureAssetController {

    private final FurnitureAssetService furnitureAssetService;

    public FurnitureAssetController(FurnitureAssetService furnitureAssetService) {
        this.furnitureAssetService = furnitureAssetService;
    }

    @PostMapping
    public ResponseEntity<Asset> createFurnitureAsset(@Valid @RequestBody Asset request) {
        Asset created = furnitureAssetService.createFurnitureAsset(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Asset>> getAllFurnitureAssets() {
        return ResponseEntity.ok(furnitureAssetService.getAllFurnitureAssets());
    }

    @GetMapping("/{assetCode}")
    public ResponseEntity<Asset> getFurnitureAsset(@PathVariable String assetCode) {
        return ResponseEntity.ok(furnitureAssetService.getFurnitureAsset(assetCode));
    }

    @PutMapping("/{assetCode}")
    public ResponseEntity<Asset> updateFurnitureAsset(
            @PathVariable String assetCode,
            @Valid @RequestBody Asset request
    ) {
        return ResponseEntity.ok(furnitureAssetService.updateFurnitureAsset(assetCode, request));
    }
}
