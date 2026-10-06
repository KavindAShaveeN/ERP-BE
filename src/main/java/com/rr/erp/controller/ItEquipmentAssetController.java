package com.rr.erp.controller;

import com.rr.erp.entity.Asset;
import com.rr.erp.service.ItEquipmentAssetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets/it-equipment")
@CrossOrigin(origins = "*")
public class ItEquipmentAssetController {

    private final ItEquipmentAssetService itEquipmentAssetService;

    public ItEquipmentAssetController(ItEquipmentAssetService itEquipmentAssetService) {
        this.itEquipmentAssetService = itEquipmentAssetService;
    }

    @PostMapping
    public ResponseEntity<Asset> createItEquipmentAsset(@Valid @RequestBody Asset request) {
        Asset created = itEquipmentAssetService.createItEquipmentAsset(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Asset>> getAllItEquipmentAssets() {
        return ResponseEntity.ok(itEquipmentAssetService.getAllItEquipmentAssets());
    }

    @GetMapping("/{assetCode}")
    public ResponseEntity<Asset> getItEquipmentAsset(@PathVariable String assetCode) {
        return ResponseEntity.ok(itEquipmentAssetService.getItEquipmentAsset(assetCode));
    }

    @PutMapping("/{assetCode}")
    public ResponseEntity<Asset> updateItEquipmentAsset(
            @PathVariable String assetCode,
            @Valid @RequestBody Asset request
    ) {
        return ResponseEntity.ok(itEquipmentAssetService.updateItEquipmentAsset(assetCode, request));
    }
}
