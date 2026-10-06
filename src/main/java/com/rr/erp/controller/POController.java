package com.rr.erp.controller;

import com.rr.erp.dto.PagedResponse;
import com.rr.erp.entity.PO;
import com.rr.erp.service.POService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/po")
@RequiredArgsConstructor
@CrossOrigin
public class POController {

    private final POService service;


    @GetMapping("/created/{billToProjectCode}")
    public ResponseEntity<List<PO>> getCreatedPOs(
            @PathVariable String billToProjectCode
    ) {

        return ResponseEntity.ok(
                service.getCreatedPOs(billToProjectCode)
        );
    }

    @GetMapping("/destination/{projectCode}")
    public ResponseEntity<List<PO>> getPOsByDestinationProject(
            @PathVariable String projectCode
    ) {

        return ResponseEntity.ok(
                service.getPOsByDestinationProject(projectCode)
        );
    }

    // Every PO across every project — used by HQ-wide report views and the Purchase
    // Orders list. Paginated (bounded LIMIT/OFFSET per call) rather than loading the
    // whole table at once; the frontend pages through this via fetchAllPages when it
    // genuinely needs everything.
    @GetMapping("/all")
    public ResponseEntity<PagedResponse<PO>> getAllPurchaseOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                service.getAllPurchaseOrders(page, size)
        );
    }

    @PostMapping
    public ResponseEntity<PO> createPO(
            @RequestBody PO po
    ) {

        return ResponseEntity.ok(
                service.createPO(po)
        );
    }


    @PutMapping("/{poId}")
    public ResponseEntity<PO> updatePO(
            @PathVariable UUID poId,
            @RequestBody PO po
    ) {

        return ResponseEntity.ok(
                service.updatePO(
                        poId,
                        po
                )
        );
    }
}