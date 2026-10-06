package com.rr.erp.controller;

import com.rr.erp.dto.StockBatchView;
import com.rr.erp.dto.StockSizeBreakdown;
import com.rr.erp.entity.StockActionAllocation;
import com.rr.erp.service.StockBatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stock-batches")
@CrossOrigin(origins = "*")
public class StockBatchController {

    private final StockBatchService stockBatchService;

    public StockBatchController(StockBatchService stockBatchService) {
        this.stockBatchService = stockBatchService;
    }

    /** The full FIFO trail for one project/item — used by the Stock Inquiry batch breakdown. */
    @GetMapping("/{projectCode}/{itemCode}")
    public ResponseEntity<List<StockBatchView>> getBatchesForItem(
            @PathVariable String projectCode,
            @PathVariable String itemCode
    ) {

        return ResponseEntity.ok(
                stockBatchService.getBatchViewsForItem(projectCode, itemCode)
        );
    }

    /** Per-size stock breakdown for one project/item (e.g. "6m x8, 1m x4, 2m x1") — powers a project store's per-size stock view for dimensional items. */
    @GetMapping("/{projectCode}/{itemCode}/sizes")
    public ResponseEntity<List<StockSizeBreakdown>> getSizeBreakdownForItem(
            @PathVariable String projectCode,
            @PathVariable String itemCode
    ) {

        return ResponseEntity.ok(
                stockBatchService.getSizeBreakdownForItem(projectCode, itemCode)
        );
    }

    /** Which batches (and at what cost) one action drew from — used to show cost/value on a GIN. */
    @GetMapping("/allocations/{actionType}/{actionId}")
    public ResponseEntity<List<StockActionAllocation>> getAllocationsForAction(
            @PathVariable String actionType,
            @PathVariable UUID actionId
    ) {

        return ResponseEntity.ok(
                stockBatchService.getAllocationsForAction(actionType, actionId)
        );
    }
}
