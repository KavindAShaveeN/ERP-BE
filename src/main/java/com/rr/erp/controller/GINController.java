package com.rr.erp.controller;

import com.rr.erp.dto.PagedResponse;
import com.rr.erp.entity.GIN;
import com.rr.erp.service.GINService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gin")
@CrossOrigin(origins = "*")
public class GINController {

    private final GINService ginService;

    public GINController(GINService ginService) {
        this.ginService = ginService;
    }

    // GET /api/gin/created/{issuedProjectCode}
    @GetMapping("/created/{issuedProjectCode}")
    public ResponseEntity<PagedResponse<GIN>> getCreatedGin(
            @PathVariable String issuedProjectCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                ginService.getCreatedGin(issuedProjectCode, page, size)
        );
    }

    // GET /api/gin/incoming/{receivedProjectCode}
    @GetMapping("/incoming/{receivedProjectCode}")
    public ResponseEntity<PagedResponse<GIN>> getIncomingGin(
            @PathVariable String receivedProjectCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                ginService.getIncomingGin(receivedProjectCode, page, size)
        );
    }

    // GET /api/gin/all — every GIN across every project, for HQ-wide reports.
    @GetMapping("/all")
    public ResponseEntity<PagedResponse<GIN>> getAllGins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                ginService.getAllGins(page, size)
        );
    }

    // GET /api/gin/asset/{forAssetCode} — GINs whose items were issued for this asset.
    @GetMapping("/asset/{forAssetCode}")
    public ResponseEntity<PagedResponse<GIN>> getGinsByForAssetCode(
            @PathVariable String forAssetCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                ginService.getGinsByForAssetCode(forAssetCode, page, size)
        );
    }

    // GET /api/gin/received-by/{employeeCode} — GINs received by this employee.
    @GetMapping("/received-by/{employeeCode}")
    public ResponseEntity<PagedResponse<GIN>> getGinsByReceivedBy(
            @PathVariable String employeeCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                ginService.getGinsByReceivedBy(employeeCode, page, size)
        );
    }

    // POST /api/gin/
    @PostMapping("/")
    public ResponseEntity<GIN> createGin(
            @RequestBody GIN gin) {

        return ResponseEntity.ok(
                ginService.createGin(gin)
        );
    }

    // PUT /api/gin/{ginId}
    @PutMapping("/{ginId}")
    public ResponseEntity<GIN> updateGin(
            @PathVariable UUID ginId,
            @RequestBody GIN gin) {

        return ResponseEntity.ok(
                ginService.updateGin(ginId, gin)
        );
    }

    // PUT /api/gin/{ginId}/gate-verify?gateVerifiedBy=NAME
    @PutMapping("/{ginId}/gate-verify")
    public ResponseEntity<GIN> verifyGinAtGate(
            @PathVariable UUID ginId,
            @RequestParam String gateVerifiedBy) {

        return ResponseEntity.ok(
                ginService.verifyGinAtGate(ginId, gateVerifiedBy)
        );
    }

    // PUT /api/gin/{ginId}/gate-verify-arrival?gateVerifiedBy=NAME
    @PutMapping("/{ginId}/gate-verify-arrival")
    public ResponseEntity<GIN> verifyGinArrivalAtGate(
            @PathVariable UUID ginId,
            @RequestParam String gateVerifiedBy) {

        return ResponseEntity.ok(
                ginService.verifyGinArrivalAtGate(ginId, gateVerifiedBy)
        );
    }
}