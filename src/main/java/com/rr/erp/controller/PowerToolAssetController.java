package com.rr.erp.controller;

import com.rr.erp.entity.Asset;
import com.rr.erp.service.PowerToolAssetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets/power-tools")
@CrossOrigin(origins = "*")
public class PowerToolAssetController {

    private final PowerToolAssetService powerToolAssetService;

    public PowerToolAssetController(PowerToolAssetService powerToolAssetService) {
        this.powerToolAssetService = powerToolAssetService;
    }

    @PostMapping
    public ResponseEntity<Asset> createPowerToolAsset(@Valid @RequestBody Asset request) {
        Asset created = powerToolAssetService.createPowerToolAsset(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Asset>> getAllPowerToolAssets() {
        return ResponseEntity.ok(powerToolAssetService.getAllPowerToolAssets());
    }

    @GetMapping("/{assetCode}")
    public ResponseEntity<Asset> getPowerToolAsset(@PathVariable String assetCode) {
        return ResponseEntity.ok(powerToolAssetService.getPowerToolAsset(assetCode));
    }

    @PutMapping("/{assetCode}")
    public ResponseEntity<Asset> updatePowerToolAsset(
            @PathVariable String assetCode,
            @Valid @RequestBody Asset request
    ) {
        return ResponseEntity.ok(powerToolAssetService.updatePowerToolAsset(assetCode, request));
    }
}
