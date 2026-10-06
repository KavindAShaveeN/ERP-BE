package com.rr.erp.controller;

import com.rr.erp.entity.NonStockItem;
import com.rr.erp.service.NonStockItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/non-stock-items")
@CrossOrigin(origins = "*")
public class NonStockItemController {

    private final NonStockItemService nonStockItemService;

    public NonStockItemController(NonStockItemService nonStockItemService) {
        this.nonStockItemService = nonStockItemService;
    }

    @PostMapping
    public ResponseEntity<NonStockItem> createNonStockItem(@Valid @RequestBody NonStockItem request) {
        NonStockItem saved = nonStockItemService.addNonStockItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<NonStockItem>> getAllNonStockItems() {
        return ResponseEntity.ok(nonStockItemService.getAllNonStockItems());
    }

    @GetMapping("/{nonStockItemId}")
    public ResponseEntity<NonStockItem> getNonStockItemById(@PathVariable Long nonStockItemId) {
        return ResponseEntity.ok(nonStockItemService.getNonStockItemById(nonStockItemId));
    }

    @PutMapping("/{nonStockItemId}")
    public ResponseEntity<NonStockItem> updateNonStockItem(
            @PathVariable Long nonStockItemId,
            @Valid @RequestBody NonStockItem request
    ) {
        NonStockItem updated = nonStockItemService.updateNonStockItem(nonStockItemId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{nonStockItemId}")
    public ResponseEntity<Void> deleteNonStockItem(@PathVariable Long nonStockItemId) {
        nonStockItemService.deleteNonStockItem(nonStockItemId);
        return ResponseEntity.noContent().build();
    }
}
