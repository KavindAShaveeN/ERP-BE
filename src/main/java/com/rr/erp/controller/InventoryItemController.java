package com.rr.erp.controller;

import com.rr.erp.entity.InventoryItem;
import com.rr.erp.service.InventoryItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory_item")
@CrossOrigin(origins = "*")
public class InventoryItemController {

    private final InventoryItemService inventoryItemService;

    public InventoryItemController(
            InventoryItemService inventoryItemService
    ) {
        this.inventoryItemService = inventoryItemService;
    }

    // ==================================
    // POST /api/inventory_item/
    // ==================================
    @PostMapping("/")
    public ResponseEntity<InventoryItem> createInventoryItem(
            @RequestBody InventoryItem inventoryItem
    ) {

        InventoryItem createdInventoryItem =
                inventoryItemService.createInventoryItem(
                        inventoryItem
                );

        return ResponseEntity.ok(createdInventoryItem);
    }

    // ==================================
    // GET /api/inventory_item/all
    // ==================================
    @GetMapping("/all")
    public ResponseEntity<List<InventoryItem>>
    getAllInventoryItems() {

        return ResponseEntity.ok(
                inventoryItemService.getAllInventoryItems()
        );
    }

    // ==================================
    // GET /api/inventory_item/{id}
    // ==================================
    @GetMapping("/{inventoryItemId}")
    public ResponseEntity<InventoryItem> getInventoryItemById(
            @PathVariable Integer inventoryItemId
    ) {

        return ResponseEntity.ok(
                inventoryItemService.getInventoryItemById(
                        inventoryItemId
                )
        );
    }

    // ==================================
    // PUT /api/inventory_item/{id}
    // ==================================
    @PutMapping("/{inventoryItemId}")
    public ResponseEntity<InventoryItem> updateInventoryItem(
            @PathVariable Integer inventoryItemId,
            @RequestBody InventoryItem inventoryItem
    ) {

        return ResponseEntity.ok(
                inventoryItemService.updateInventoryItem(
                        inventoryItemId,
                        inventoryItem
                )
        );
    }

    // ==================================
    // DELETE /api/inventory_item/{id}
    // ==================================
    @DeleteMapping("/{inventoryItemId}")
    public ResponseEntity<Void> deleteInventoryItem(
            @PathVariable Integer inventoryItemId
    ) {

        inventoryItemService.deleteInventoryItem(
                inventoryItemId
        );

        return ResponseEntity.noContent().build();
    }
}