package com.rr.erp.controller;

import com.rr.erp.dto.AssetDocumentRemark;
import com.rr.erp.dto.AssetDocumentRemarkRequest;
import com.rr.erp.dto.OperatorAssetResponse;
import com.rr.erp.dto.PagedResponse;
import com.rr.erp.entity.Asset;
import com.rr.erp.service.AssetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@CrossOrigin(origins = "*")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    /* Registered asset codes across all classes — a cheap way to tell which asset codes
     * already have an asset, without loading any asset records. Optional search / assetClass /
     * status filters narrow it to the assets matching a list view (e.g. "select all matching"). */
    @GetMapping("/codes")
    public ResponseEntity<List<String>> getAssetCodes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String assetClass,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(assetService.getMatchingAssetCodes(search, assetClass, status));
    }

    /* GET /api/assets/by-operator/{operatorEmployeeCode}
     * Assets currently operated by this employee (their latest active operator entry). */
    @GetMapping("/by-operator/{operatorEmployeeCode}")
    public ResponseEntity<List<OperatorAssetResponse>> getAssetsByOperator(@PathVariable String operatorEmployeeCode) {
        return ResponseEntity.ok(assetService.getAssetsByOperator(operatorEmployeeCode));
    }

    /* GET /api/assets/page?page=0&size=50&search=excavator&assetClass=Machinery&status=Active
     * Common fields only; search matches asset code, description or serial number. */
    @GetMapping("/page")
    public ResponseEntity<PagedResponse<Asset>> getAssetPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String assetClass,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(assetService.getAssetPage(search, assetClass, status, page, size));
    }

    /* The free-text remark in an asset's Documents section. */
    @GetMapping("/{assetCode}/document-remark")
    public ResponseEntity<AssetDocumentRemark> getDocumentRemark(@PathVariable String assetCode) {
        return ResponseEntity.ok(assetService.getDocumentRemark(assetCode));
    }

    @PutMapping("/{assetCode}/document-remark")
    public ResponseEntity<AssetDocumentRemark> saveDocumentRemark(
            @PathVariable String assetCode,
            @RequestBody AssetDocumentRemarkRequest request
    ) {
        return ResponseEntity.ok(assetService.saveDocumentRemark(assetCode, request.remark()));
    }

    /*
     * Common-fields-only lookup used to discover an asset's class before fetching
     * full class-specific detail from e.g. /api/assets/vehicles/{assetCode}.
     * More specific controller mappings (e.g. /api/assets/vehicles) take precedence
     * over this {assetCode} path variable for their own literal path segments.
     */
    @GetMapping("/{assetCode}")
    public ResponseEntity<Asset> getAsset(@PathVariable String assetCode) {
        return ResponseEntity.ok(assetService.getAsset(assetCode));
    }
}
