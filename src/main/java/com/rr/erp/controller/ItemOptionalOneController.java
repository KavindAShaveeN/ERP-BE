package com.rr.erp.controller;

import com.rr.erp.entity.ItemOptionalOne;
import com.rr.erp.service.ItemOptionalOneService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/item/optional-ones")
@CrossOrigin(origins = "*")
public class ItemOptionalOneController {

    private final ItemOptionalOneService optionalOneService;

    public ItemOptionalOneController(ItemOptionalOneService optionalOneService) {
        this.optionalOneService = optionalOneService;
    }

    @PostMapping
    public ResponseEntity<ItemOptionalOne> addOptionalOne(
            @Valid @RequestBody ItemOptionalOne request
    ) {
        ItemOptionalOne saved = optionalOneService.addOptionalOne(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @GetMapping
    public ResponseEntity<List<ItemOptionalOne>> getAllOptionalOnes() {
        return ResponseEntity.ok(optionalOneService.getAllOptionalOnes());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemOptionalOne> updateOptionalOne(
            @PathVariable Long id,
            @Valid @RequestBody ItemOptionalOne request
    ) {
        ItemOptionalOne updated = optionalOneService.updateOptionalOne(id, request);

        return ResponseEntity.ok(updated);
    }
}
