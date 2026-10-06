package com.rr.erp.controller;

import com.rr.erp.entity.AssetSparePart;
import com.rr.erp.service.AssetSparePartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AssetSparePartController {

    private final AssetSparePartService service;

    public AssetSparePartController(AssetSparePartService service) {
        this.service = service;
    }

    // GET /api/assets/{assetCode}/spare-parts
    @GetMapping("/assets/{assetCode}/spare-parts")
    public ResponseEntity<List<AssetSparePart>> list(@PathVariable String assetCode) {
        return ResponseEntity.ok(service.getByAsset(assetCode));
    }

    // PUT /api/assets/{assetCode}/spare-parts — replace the whole list
    @PutMapping("/assets/{assetCode}/spare-parts")
    public ResponseEntity<List<AssetSparePart>> replaceAll(
            @PathVariable String assetCode,
            @RequestBody List<AssetSparePart> parts) {
        return ResponseEntity.ok(service.replaceAll(assetCode, parts));
    }

    // POST /api/assets/{assetCode}/spare-parts
    @PostMapping("/assets/{assetCode}/spare-parts")
    public ResponseEntity<List<AssetSparePart>> add(
            @PathVariable String assetCode,
            @RequestBody AssetSparePart part) {
        return ResponseEntity.ok(service.add(assetCode, part));
    }

    // PUT /api/assets/{assetCode}/spare-parts/{id}
    @PutMapping("/assets/{assetCode}/spare-parts/{id}")
    public ResponseEntity<List<AssetSparePart>> update(
            @PathVariable String assetCode,
            @PathVariable Long id,
            @RequestBody AssetSparePart part) {
        return ResponseEntity.ok(service.update(assetCode, id, part));
    }

    // DELETE /api/assets/{assetCode}/spare-parts/{id}
    @DeleteMapping("/assets/{assetCode}/spare-parts/{id}")
    public ResponseEntity<List<AssetSparePart>> delete(
            @PathVariable String assetCode,
            @PathVariable Long id) {
        return ResponseEntity.ok(service.delete(assetCode, id));
    }

    // GET /api/item-codes/{itemCodeId}/assets — which assets use this item as a spare part
    @GetMapping("/item-codes/{itemCodeId}/assets")
    public ResponseEntity<List<AssetSparePart>> assetsUsingItem(@PathVariable Long itemCodeId) {
        return ResponseEntity.ok(service.getByItemCode(itemCodeId));
    }
}
