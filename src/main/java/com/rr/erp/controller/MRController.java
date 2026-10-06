package com.rr.erp.controller;

import com.rr.erp.dto.PagedResponse;
import com.rr.erp.entity.MR;
import com.rr.erp.service.MRService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/material-requests")
@CrossOrigin(origins = "*")
public class MRController {

    private final MRService mrService;

    public MRController(MRService mrService)
    {
        this.mrService = mrService;
    }

    @GetMapping("/all")
    public ResponseEntity<PagedResponse<MR>> getAllMaterialRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        PagedResponse<MR> materialRequests =
                mrService.getAllMaterialRequests(page, size);

        return ResponseEntity.ok(materialRequests);
    }

    // Every MR regardless of status — used by HQ-wide report views.
    @GetMapping("/report")
    public ResponseEntity<PagedResponse<MR>> getAllMaterialRequestsForReport(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        PagedResponse<MR> materialRequests =
                mrService.getAllMaterialRequestsForReport(page, size);

        return ResponseEntity.ok(materialRequests);
    }
    @PostMapping
    public ResponseEntity<MR> createMaterialRequest(
            @Valid @RequestBody MR mr
    )
    {

        MR createdMR = mrService.createMaterialRequest(mr);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdMR);
    }

    @GetMapping("/incoming/{destinationProjectCode}")
    public ResponseEntity<PagedResponse<MR>> getIncomingMaterialRequests(
            @PathVariable String destinationProjectCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    )
    {

        PagedResponse<MR> materialRequests =
                mrService.getByDestinationProjectCode(
                        destinationProjectCode,
                        page,
                        size
                );

        return ResponseEntity.ok(materialRequests);
    }

    @PutMapping("/{mrId}")
    public ResponseEntity<MR> updateMaterialRequest(
            @PathVariable UUID mrId,
            @Valid @RequestBody MR mr
    )
    {

        MR updatedMR =
                mrService.updateMaterialRequest(mrId, mr);

        return ResponseEntity.ok(updatedMR);
    }
    @GetMapping("/created/{requestingProjectCode}")
    public ResponseEntity<PagedResponse<MR>> getMaterialRequests(
            @PathVariable String requestingProjectCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        PagedResponse<MR> materialRequests =
                mrService.getByRequestingProjectCode(
                        requestingProjectCode,
                        page,
                        size
                );

        return ResponseEntity.ok(materialRequests);
    }

}