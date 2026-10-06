package com.rr.erp.controller;

import com.rr.erp.dto.PagedResponse;
import com.rr.erp.entity.GRN;
import com.rr.erp.service.GRNService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/grn")
@CrossOrigin(origins = "*")
public class GRNController {

    private final GRNService grnService;

    public GRNController(GRNService grnService) {
        this.grnService = grnService;
    }

    @PostMapping
    public ResponseEntity<GRN> createGRN(
            @Valid @RequestBody GRN grn
    ) {

        GRN createdGRN = grnService.createGRN(grn);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdGRN);
    }

    @GetMapping("/created/{fromProjectCode}")
    public ResponseEntity<PagedResponse<GRN>> getByFromProjectCode(
            @PathVariable String fromProjectCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                grnService.getByFromProjectCode(fromProjectCode, page, size)
        );
    }

    @GetMapping("/incoming/{toProjectCode}")
    public ResponseEntity<PagedResponse<GRN>> getByToProjectCode(
            @PathVariable String toProjectCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                grnService.getByToProjectCode(toProjectCode, page, size)
        );
    }

    @GetMapping("/supplierGRN")
    public ResponseEntity<PagedResponse<GRN>> getSupplierGRNs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                grnService.getSupplierGRNs(page, size)
        );
    }

    // Every GRN across every project and type — used by HQ-wide reports.
    @GetMapping("/all")
    public ResponseEntity<PagedResponse<GRN>> getAllGrns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                grnService.getAllGrns(page, size)
        );
    }

    @PutMapping("/{grnId}")
    public ResponseEntity<GRN> updateGRN(
            @PathVariable UUID grnId,
            @Valid @RequestBody GRN grn
    ) {

        return ResponseEntity.ok(
                grnService.updateGRN(grnId, grn)
        );
    }
}
