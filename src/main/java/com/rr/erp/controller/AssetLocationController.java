package com.rr.erp.controller;

import com.rr.erp.dto.AssetAtProjectResponse;
import com.rr.erp.entity.AssetLocation;
import com.rr.erp.service.AssetLocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/asset-location")
@CrossOrigin(origins = "*")
public class AssetLocationController {

    private final AssetLocationService assetLocationService;

    public AssetLocationController(
            AssetLocationService assetLocationService) {

        this.assetLocationService = assetLocationService;
    }

    // POST /api/asset-location/
    @PostMapping("/")
    public ResponseEntity<AssetLocation> createAssetLocation(
            @RequestBody AssetLocation assetLocation) {

        assetLocationService.createAssetLocation(assetLocation);

        return ResponseEntity.ok(assetLocation);
    }

    // GET /api/asset-location/at-project/{projectCode} — assets currently located at a project
    @GetMapping("/at-project/{projectCode}")
    public ResponseEntity<List<AssetAtProjectResponse>> getAssetsAtProject(
            @PathVariable String projectCode,
            @RequestParam(defaultValue = "false") boolean includeDisposed) {

        return ResponseEntity.ok(assetLocationService.getAssetsAtProject(projectCode, includeDisposed));
    }

    // GET /api/asset-location/{assetCode}
    @GetMapping("/{assetCode}")
    public ResponseEntity<List<AssetLocation>> getAssetLocationsByAssetCode(
            @PathVariable String assetCode) {

        return ResponseEntity.ok(
                assetLocationService
                        .getAssetLocationsByAssetCode(assetCode)
        );
    }

    // DELETE /api/asset-location/{assetLocationId}
    @DeleteMapping("/{assetLocationId}")
    public ResponseEntity<String> deleteAssetLocation(
            @PathVariable UUID assetLocationId) {

        int result = assetLocationService
                .deleteAssetLocation(assetLocationId);

        if (result > 0) {
            return ResponseEntity.ok(
                    "Asset location deactivated successfully"
            );
        }

        return ResponseEntity
                .notFound()
                .build();
    }
}
