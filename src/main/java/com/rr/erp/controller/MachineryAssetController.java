package com.rr.erp.controller;

import com.rr.erp.entity.Asset;
import com.rr.erp.service.MachineryAssetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets/machinery")
@CrossOrigin(origins = "*")
public class MachineryAssetController {

    private final MachineryAssetService machineryAssetService;

    public MachineryAssetController(MachineryAssetService machineryAssetService) {
        this.machineryAssetService = machineryAssetService;
    }

    @PostMapping
    public ResponseEntity<Asset> createMachineryAsset(@Valid @RequestBody Asset request) {
        Asset created = machineryAssetService.createMachineryAsset(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Asset>> getAllMachineryAssets() {
        return ResponseEntity.ok(machineryAssetService.getAllMachineryAssets());
    }

    @GetMapping("/{assetCode}")
    public ResponseEntity<Asset> getMachineryAsset(@PathVariable String assetCode) {
        return ResponseEntity.ok(machineryAssetService.getMachineryAsset(assetCode));
    }

    @PutMapping("/{assetCode}")
    public ResponseEntity<Asset> updateMachineryAsset(
            @PathVariable String assetCode,
            @Valid @RequestBody Asset request
    ) {
        return ResponseEntity.ok(machineryAssetService.updateMachineryAsset(assetCode, request));
    }
}
