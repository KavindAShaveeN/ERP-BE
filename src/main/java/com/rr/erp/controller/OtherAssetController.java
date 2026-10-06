package com.rr.erp.controller;

import com.rr.erp.entity.Asset;
import com.rr.erp.service.CommonAssetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Assets of class "Other" (containers, tanks, pontoons, household appliances, ...) — these have
 * no class-specific detail, so only the common asset fields are stored. */
@RestController
@RequestMapping("/api/assets/other")
@CrossOrigin(origins = "*")
public class OtherAssetController {

    private static final String ASSET_CLASS = "Other";

    private final CommonAssetService commonAssetService;

    public OtherAssetController(CommonAssetService commonAssetService) {
        this.commonAssetService = commonAssetService;
    }

    @PostMapping
    public ResponseEntity<Asset> createOtherAsset(@Valid @RequestBody Asset request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commonAssetService.create(ASSET_CLASS, request));
    }

    @GetMapping
    public ResponseEntity<List<Asset>> getAllOtherAssets() {
        return ResponseEntity.ok(commonAssetService.getAll(ASSET_CLASS));
    }

    @GetMapping("/{assetCode}")
    public ResponseEntity<Asset> getOtherAsset(@PathVariable String assetCode) {
        return ResponseEntity.ok(commonAssetService.get(ASSET_CLASS, assetCode));
    }

    @PutMapping("/{assetCode}")
    public ResponseEntity<Asset> updateOtherAsset(
            @PathVariable String assetCode,
            @Valid @RequestBody Asset request
    ) {
        return ResponseEntity.ok(commonAssetService.update(ASSET_CLASS, assetCode, request));
    }
}
