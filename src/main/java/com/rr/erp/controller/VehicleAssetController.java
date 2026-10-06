package com.rr.erp.controller;

import com.rr.erp.entity.Asset;
import com.rr.erp.service.VehicleAssetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets/vehicles")
@CrossOrigin(origins = "*")
public class VehicleAssetController {

    private final VehicleAssetService vehicleAssetService;

    public VehicleAssetController(VehicleAssetService vehicleAssetService) {
        this.vehicleAssetService = vehicleAssetService;
    }

    @PostMapping
    public ResponseEntity<Asset> createVehicleAsset(@Valid @RequestBody Asset request) {
        Asset created = vehicleAssetService.createVehicleAsset(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Asset>> getAllVehicleAssets() {
        return ResponseEntity.ok(vehicleAssetService.getAllVehicleAssets());
    }

    @GetMapping("/expiries")
    public ResponseEntity<List<com.rr.erp.dto.AssetExpiryEvent>> getExpiryEvents() {
        return ResponseEntity.ok(vehicleAssetService.getExpiryEvents());
    }

    @GetMapping("/{assetCode}")
    public ResponseEntity<Asset> getVehicleAsset(@PathVariable String assetCode) {
        return ResponseEntity.ok(vehicleAssetService.getVehicleAsset(assetCode));
    }

    @PutMapping("/{assetCode}")
    public ResponseEntity<Asset> updateVehicleAsset(
            @PathVariable String assetCode,
            @Valid @RequestBody Asset request
    ) {
        return ResponseEntity.ok(vehicleAssetService.updateVehicleAsset(assetCode, request));
    }
}
