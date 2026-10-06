package com.rr.erp.controller;

import com.rr.erp.dto.PagedResponse;
import com.rr.erp.entity.StockReturn;
import com.rr.erp.service.StockReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/return")
@RequiredArgsConstructor
@CrossOrigin
public class StockReturnController {

    private final StockReturnService service;

    @PostMapping
    public ResponseEntity<StockReturn> createStockReturn(
            @RequestBody StockReturn stockReturn
    ) {

        return ResponseEntity.ok(
                service.createStockReturn(stockReturn)
        );
    }


    @GetMapping("/created/{fromProjectCode}")
    public ResponseEntity<PagedResponse<StockReturn>>
    getCreatedReturns(
            @PathVariable String fromProjectCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                service.getCreatedReturns(
                        fromProjectCode,
                        page,
                        size
                )
        );
    }


    @GetMapping("/incoming/{toProjectCode}")
    public ResponseEntity<PagedResponse<StockReturn>>
    getIncomingReturns(
            @PathVariable String toProjectCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                service.getIncomingReturns(
                        toProjectCode,
                        page,
                        size
                )
        );
    }


    @GetMapping("/all")
    public ResponseEntity<PagedResponse<StockReturn>>
    getAllReturns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                service.getAllReturns(page, size)
        );
    }


    // PUT /api/return/incoming/{stockReturnId}
    @PutMapping("/incoming/{stockReturnId}")
    public ResponseEntity<StockReturn>
    updateStockReturn(
            @PathVariable UUID stockReturnId,
            @RequestBody StockReturn stockReturn
    ) {

        return ResponseEntity.ok(
                service.updateStockReturn(
                        stockReturnId,
                        stockReturn
                )
        );
    }

    // PUT /api/return/{stockReturnId}/gate-verify?gateVerifiedBy=NAME
    @PutMapping("/{stockReturnId}/gate-verify")
    public ResponseEntity<StockReturn> verifyStockReturnAtGate(
            @PathVariable UUID stockReturnId,
            @RequestParam String gateVerifiedBy
    ) {

        return ResponseEntity.ok(
                service.verifyStockReturnAtGate(stockReturnId, gateVerifiedBy)
        );
    }

    // PUT /api/return/{stockReturnId}/gate-verify-arrival?gateVerifiedBy=NAME
    @PutMapping("/{stockReturnId}/gate-verify-arrival")
    public ResponseEntity<StockReturn> verifyStockReturnArrivalAtGate(
            @PathVariable UUID stockReturnId,
            @RequestParam String gateVerifiedBy
    ) {

        return ResponseEntity.ok(
                service.verifyStockReturnArrivalAtGate(stockReturnId, gateVerifiedBy)
        );
    }
}