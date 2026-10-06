package com.rr.erp.controller;

import com.rr.erp.entity.ConsumableItem;
import com.rr.erp.service.ConsumableItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consumable-items")
@CrossOrigin(origins = "*")
public class ConsumableItemController {

    private final ConsumableItemService consumableItemService;

    public ConsumableItemController(ConsumableItemService consumableItemService) {
        this.consumableItemService = consumableItemService;
    }

    @PostMapping
    public ResponseEntity<ConsumableItem> createConsumableItem(@Valid @RequestBody ConsumableItem request) {
        ConsumableItem saved = consumableItemService.addConsumableItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<ConsumableItem>> getAllConsumableItems() {
        return ResponseEntity.ok(consumableItemService.getAllConsumableItems());
    }

    @GetMapping("/{consumableItemId}")
    public ResponseEntity<ConsumableItem> getConsumableItemById(@PathVariable Long consumableItemId) {
        return ResponseEntity.ok(consumableItemService.getConsumableItemById(consumableItemId));
    }

    @PutMapping("/{consumableItemId}")
    public ResponseEntity<ConsumableItem> updateConsumableItem(
            @PathVariable Long consumableItemId,
            @Valid @RequestBody ConsumableItem request
    ) {
        ConsumableItem updated = consumableItemService.updateConsumableItem(consumableItemId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{consumableItemId}")
    public ResponseEntity<Void> deleteConsumableItem(@PathVariable Long consumableItemId) {
        consumableItemService.deleteConsumableItem(consumableItemId);
        return ResponseEntity.noContent().build();
    }
}
