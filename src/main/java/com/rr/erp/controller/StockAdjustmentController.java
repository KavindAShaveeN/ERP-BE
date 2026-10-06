package com.rr.erp.controller;

import com.rr.erp.dto.PagedResponse;
import com.rr.erp.entity.StockAdjustment;
import com.rr.erp.service.StockAdjustmentService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/adjustment")
@RequiredArgsConstructor
@CrossOrigin
public class StockAdjustmentController {

    private final StockAdjustmentService service;


    @PostMapping
    public ResponseEntity<StockAdjustment>
    createStockAdjustment(
            @RequestBody StockAdjustment adjustment
    ) {

        return ResponseEntity.ok(
                service.createStockAdjustment(
                        adjustment
                )
        );
    }



    @GetMapping("/created/{projectCode}")
    public ResponseEntity<PagedResponse<StockAdjustment>>
    getCreatedAdjustments(
            @PathVariable String projectCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                service.getCreatedAdjustments(
                        projectCode,
                        page,
                        size
                )
        );
    }



    @GetMapping("/all")
    public ResponseEntity<PagedResponse<StockAdjustment>>
    getAllAdjustments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                service.getAllAdjustments(page, size)
        );
    }



    @PutMapping("/{stockAdjustmentId}")
    public ResponseEntity<StockAdjustment>
    updateStockAdjustment(
            @PathVariable UUID stockAdjustmentId,
            @RequestBody StockAdjustment adjustment
    ) {

        return ResponseEntity.ok(
                service.updateStockAdjustment(
                        stockAdjustmentId,
                        adjustment
                )
        );
    }
}