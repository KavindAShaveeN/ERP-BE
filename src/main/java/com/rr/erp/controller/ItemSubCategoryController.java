package com.rr.erp.controller;

import com.rr.erp.entity.ItemSubCategory;
import com.rr.erp.service.ItemSubCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/item/sub-categories")
@CrossOrigin(origins = "*")
public class ItemSubCategoryController {

    private final ItemSubCategoryService subCategoryService;

    public ItemSubCategoryController(ItemSubCategoryService subCategoryService) {
        this.subCategoryService = subCategoryService;
    }

    @PostMapping
    public ResponseEntity<ItemSubCategory> addSubCategory(
            @Valid @RequestBody ItemSubCategory request
    ) {
        ItemSubCategory saved = subCategoryService.addSubCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @GetMapping
    public ResponseEntity<List<ItemSubCategory>> getAllSubCategories() {
        return ResponseEntity.ok(subCategoryService.getAllSubCategories());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemSubCategory> updateSubCategory(
            @PathVariable Long id,
            @Valid @RequestBody ItemSubCategory request
    ) {
        ItemSubCategory updated = subCategoryService.updateSubCategory(id, request);

        return ResponseEntity.ok(updated);
    }
}
