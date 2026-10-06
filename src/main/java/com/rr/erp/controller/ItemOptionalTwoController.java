package com.rr.erp.controller;

import com.rr.erp.entity.ItemOptionalTwo;
import com.rr.erp.service.ItemOptionalTwoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/item/optional-twos")
@CrossOrigin(origins = "*")
public class ItemOptionalTwoController {

    private final ItemOptionalTwoService optionalTwoService;

    public ItemOptionalTwoController(ItemOptionalTwoService optionalTwoService) {
        this.optionalTwoService = optionalTwoService;
    }

    @PostMapping
    public ResponseEntity<ItemOptionalTwo> addOptionalTwo(
            @Valid @RequestBody ItemOptionalTwo request
    ) {
        ItemOptionalTwo saved = optionalTwoService.addOptionalTwo(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @GetMapping
    public ResponseEntity<List<ItemOptionalTwo>> getAllOptionalTwos() {
        return ResponseEntity.ok(optionalTwoService.getAllOptionalTwos());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemOptionalTwo> updateOptionalTwo(
            @PathVariable Long id,
            @Valid @RequestBody ItemOptionalTwo request
    ) {
        ItemOptionalTwo updated = optionalTwoService.updateOptionalTwo(id, request);

        return ResponseEntity.ok(updated);
    }
}
