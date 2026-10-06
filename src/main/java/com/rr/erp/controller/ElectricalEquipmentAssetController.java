package com.rr.erp.controller;

import com.rr.erp.entity.Asset;
import com.rr.erp.service.ElectricalEquipmentAssetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets/electrical-equipment")
@CrossOrigin(origins = "*")
public class ElectricalEquipmentAssetController {

    private final ElectricalEquipmentAssetService electricalEquipmentAssetService;

    public ElectricalEquipmentAssetController(ElectricalEquipmentAssetService electricalEquipmentAssetService) {
        this.electricalEquipmentAssetService = electricalEquipmentAssetService;
    }

    @PostMapping
    public ResponseEntity<Asset> createElectricalEquipmentAsset(@Valid @RequestBody Asset request) {
        Asset created = electricalEquipmentAssetService.createElectricalEquipmentAsset(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Asset>> getAllElectricalEquipmentAssets() {
        return ResponseEntity.ok(electricalEquipmentAssetService.getAllElectricalEquipmentAssets());
    }

    @GetMapping("/{assetCode}")
    public ResponseEntity<Asset> getElectricalEquipmentAsset(@PathVariable String assetCode) {
        return ResponseEntity.ok(electricalEquipmentAssetService.getElectricalEquipmentAsset(assetCode));
    }

    @PutMapping("/{assetCode}")
    public ResponseEntity<Asset> updateElectricalEquipmentAsset(
            @PathVariable String assetCode,
            @Valid @RequestBody Asset request
    ) {
        return ResponseEntity.ok(electricalEquipmentAssetService.updateElectricalEquipmentAsset(assetCode, request));
    }
}
