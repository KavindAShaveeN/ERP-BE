package com.rr.erp.controller;

import com.rr.erp.entity.AssetPackItem;
import com.rr.erp.service.AssetPackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets/{assetCode}/pack-items")
@CrossOrigin(origins = "*")
public class AssetPackController {

    private final AssetPackService service;

    public AssetPackController(AssetPackService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<AssetPackItem>> list(@PathVariable String assetCode) {
        return ResponseEntity.ok(service.getByAsset(assetCode));
    }

    @PostMapping
    public ResponseEntity<List<AssetPackItem>> add(
            @PathVariable String assetCode, @RequestBody AssetPackItem item) {
        return ResponseEntity.ok(service.add(assetCode, item));
    }

    @PutMapping("/{id}")
    public ResponseEntity<List<AssetPackItem>> update(
            @PathVariable String assetCode, @PathVariable Long id, @RequestBody AssetPackItem item) {
        return ResponseEntity.ok(service.update(assetCode, id, item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<List<AssetPackItem>> delete(
            @PathVariable String assetCode, @PathVariable Long id) {
        return ResponseEntity.ok(service.delete(assetCode, id));
    }
}
