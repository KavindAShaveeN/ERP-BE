package com.rr.erp.controller;

import com.rr.erp.entity.AssetCouple;
import com.rr.erp.service.AssetCoupleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets/{assetCode}/couples")
@CrossOrigin(origins = "*")
public class AssetCoupleController {

    private final AssetCoupleService service;

    public AssetCoupleController(AssetCoupleService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<AssetCouple>> list(@PathVariable String assetCode) {
        return ResponseEntity.ok(service.getByAsset(assetCode));
    }

    @PostMapping
    public ResponseEntity<List<AssetCouple>> add(
            @PathVariable String assetCode, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.add(assetCode, body.get("coupledAssetCode")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<List<AssetCouple>> remove(
            @PathVariable String assetCode, @PathVariable Long id) {
        return ResponseEntity.ok(service.remove(assetCode, id));
    }
}
