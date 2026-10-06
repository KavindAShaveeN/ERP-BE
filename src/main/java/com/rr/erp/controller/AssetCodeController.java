package com.rr.erp.controller;


import com.rr.erp.dto.AssetCodeRequestDTO;
import com.rr.erp.entity.AssetCode;
import com.rr.erp.service.AssetCodeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asset-codes")
@CrossOrigin(origins = "*")
public class AssetCodeController {

    private final AssetCodeService assetCodeService;

    public AssetCodeController(AssetCodeService assetCodeService) {
        this.assetCodeService = assetCodeService;
    }


    @PostMapping
    public ResponseEntity<AssetCode> createAssetCode(
            @Valid @RequestBody AssetCodeRequestDTO request
    ) {

        AssetCode createdAssetCode =
                assetCodeService.createAssetCode(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdAssetCode);
    }

    @GetMapping
    public ResponseEntity<List<AssetCode>> getAllAssetCodes() {
        return ResponseEntity.ok(assetCodeService.getAllAssetCodes());
    }

    /*
     * GET /api/asset-codes/AS-40-03-VOL-0001
     */
    @GetMapping("/{assetCodeCode}")
    public ResponseEntity<AssetCode> getAssetCode(
            @PathVariable String assetCodeCode
    ) {

        AssetCode assetCode =
                assetCodeService.getAssetCode(assetCodeCode);

        return ResponseEntity.ok(assetCode);
    }

    /*
     * DELETE /api/asset-codes/AS-40-03-VOL-0001
     */
    @DeleteMapping("/{assetCodeCode}")
    public ResponseEntity<Void> deleteAssetCode(
            @PathVariable String assetCodeCode
    ) {

        assetCodeService.deleteAssetCode(assetCodeCode);

        return ResponseEntity.noContent().build();
    }
}