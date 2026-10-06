package com.rr.erp.controller;

import com.rr.erp.entity.AssetOperatorHistory;
import com.rr.erp.service.AssetOperatorHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/asset-operator-history")
@CrossOrigin(origins = "*")
public class AssetOperatorHistoryController {

    private final AssetOperatorHistoryService assetOperatorHistoryService;

    public AssetOperatorHistoryController(AssetOperatorHistoryService assetOperatorHistoryService) {
        this.assetOperatorHistoryService = assetOperatorHistoryService;
    }

    // POST /api/asset-operator-history/
    @PostMapping("/")
    public ResponseEntity<AssetOperatorHistory> createAssetOperatorHistory(
            @RequestBody AssetOperatorHistory assetOperatorHistory) {

        assetOperatorHistoryService.createAssetOperatorHistory(assetOperatorHistory);

        return ResponseEntity.ok(assetOperatorHistory);
    }

    // GET /api/asset-operator-history/{assetCode}
    @GetMapping("/{assetCode}")
    public ResponseEntity<List<AssetOperatorHistory>> getAssetOperatorHistoryByAssetCode(
            @PathVariable String assetCode) {

        return ResponseEntity.ok(
                assetOperatorHistoryService.getAssetOperatorHistoryByAssetCode(assetCode)
        );
    }

    // GET /api/asset-operator-history/latest/{assetCode} — used to auto-fill the operator
    // employee on the Fuel Issue ("Received by") and Service Request forms for this asset.
    @GetMapping("/latest/{assetCode}")
    public ResponseEntity<AssetOperatorHistory> getLatestActiveOperator(
            @PathVariable String assetCode) {

        return assetOperatorHistoryService
                .findLatestActiveOperator(assetCode)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    // GET /api/asset-operator-history/by-operator/{operatorEmployeeCode} — every asset currently
    // operated by this employee, used to filter the asset picker on forms an operator fills in
    // themselves (e.g. Meter Reading).
    @GetMapping("/by-operator/{operatorEmployeeCode}")
    public ResponseEntity<List<String>> getAssetCodesForOperator(
            @PathVariable String operatorEmployeeCode) {

        return ResponseEntity.ok(
                assetOperatorHistoryService.findAssetCodesForOperator(operatorEmployeeCode)
        );
    }

    // PUT /api/asset-operator-history/{assetOperatorHistoryId} — corrects remarks/contact number
    // on an existing entry. The frontend only offers this for the latest entry of an asset.
    @PutMapping("/{assetOperatorHistoryId}")
    public ResponseEntity<String> updateAssetOperatorHistoryDetails(
            @PathVariable UUID assetOperatorHistoryId,
            @RequestBody AssetOperatorHistory assetOperatorHistory) {

        int result = assetOperatorHistoryService.updateAssetOperatorHistoryDetails(
                assetOperatorHistoryId,
                assetOperatorHistory.getRemarks(),
                assetOperatorHistory.getContactNumber()
        );

        if (result > 0) {
            return ResponseEntity.ok("Operator history entry updated successfully");
        }

        return ResponseEntity.notFound().build();
    }

    // DELETE /api/asset-operator-history/{assetOperatorHistoryId}
    @DeleteMapping("/{assetOperatorHistoryId}")
    public ResponseEntity<String> deleteAssetOperatorHistory(
            @PathVariable UUID assetOperatorHistoryId) {

        int result = assetOperatorHistoryService.deleteAssetOperatorHistory(assetOperatorHistoryId);

        if (result > 0) {
            return ResponseEntity.ok("Operator history entry deactivated successfully");
        }

        return ResponseEntity.notFound().build();
    }
}
