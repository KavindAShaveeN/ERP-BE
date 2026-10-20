package com.rr.erp.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.rr.erp.entity.ItemOptionalThree;
import com.rr.erp.service.ItemOptionalThreeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/item/optional-threes")
@CrossOrigin(origins = "*")
public class ItemOptionalThreeController {

    private final ItemOptionalThreeService optionalThreeService;

    public ItemOptionalThreeController(ItemOptionalThreeService optionalThreeService) {
        this.optionalThreeService = optionalThreeService;
    }

    @PreAuthorize("@perm.has('ITEM.CREATE')")
    @PostMapping
    public ResponseEntity<ItemOptionalThree> addOptionalThree(
            @Valid @RequestBody ItemOptionalThree request
    ) {
        ItemOptionalThree saved = optionalThreeService.addOptionalThree(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @GetMapping
    public ResponseEntity<List<ItemOptionalThree>> getAllOptionalThrees() {
        return ResponseEntity.ok(optionalThreeService.getAllOptionalThrees());
    }

    @PreAuthorize("@perm.has('ITEM.EDIT')")
    @PutMapping("/{id}")
    public ResponseEntity<ItemOptionalThree> updateOptionalThree(
            @PathVariable Long id,
            @Valid @RequestBody ItemOptionalThree request
    ) {
        ItemOptionalThree updated = optionalThreeService.updateOptionalThree(id, request);

        return ResponseEntity.ok(updated);
    }
}
