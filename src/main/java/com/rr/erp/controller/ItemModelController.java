package com.rr.erp.controller;

import com.rr.erp.entity.ItemModel;
import com.rr.erp.service.ItemModelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/item/models")
@CrossOrigin(origins = "*")
public class ItemModelController {

    private final ItemModelService modelService;

    public ItemModelController(ItemModelService modelService) {
        this.modelService = modelService;
    }

    @PostMapping
    public ResponseEntity<ItemModel> addModel(
            @Valid @RequestBody ItemModel request
    ) {
        ItemModel saved = modelService.addModel(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @GetMapping
    public ResponseEntity<List<ItemModel>> getAllModels() {
        return ResponseEntity.ok(modelService.getAllModels());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemModel> updateModel(
            @PathVariable Long id,
            @Valid @RequestBody ItemModel request
    ) {
        ItemModel updated = modelService.updateModel(id, request);

        return ResponseEntity.ok(updated);
    }
}
