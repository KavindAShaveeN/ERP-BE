package com.rr.erp.controller;

import com.rr.erp.entity.ItemType;
import com.rr.erp.service.ItemTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/item-types")
@CrossOrigin(origins = "*")
public class ItemTypeController {

    private final ItemTypeService itemTypeService;

    public ItemTypeController(ItemTypeService itemTypeService) {
        this.itemTypeService = itemTypeService;
    }

    @GetMapping
    public ResponseEntity<List<ItemType>> getAllItemTypes() {
        return ResponseEntity.ok(itemTypeService.getAllItemTypes());
    }
}
