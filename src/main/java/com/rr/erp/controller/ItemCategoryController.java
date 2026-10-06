package com.rr.erp.controller;

import com.rr.erp.entity.ItemCategory;
import com.rr.erp.service.ItemCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/item/categories")
@CrossOrigin(origins = "*")
public class ItemCategoryController {

    private final ItemCategoryService categoryService;

    public ItemCategoryController(ItemCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<ItemCategory> addCategory(
            @Valid @RequestBody ItemCategory request
    )
    {
        ItemCategory saved = categoryService.addCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @GetMapping
    public ResponseEntity<List<ItemCategory>> getAllCategories()
    {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemCategory> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody ItemCategory request
    )
    {
        ItemCategory updated = categoryService.updateCategory(id, request);

        return ResponseEntity.ok(updated);
    }
}
