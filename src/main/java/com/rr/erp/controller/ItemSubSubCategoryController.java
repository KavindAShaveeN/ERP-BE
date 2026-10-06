package com.rr.erp.controller;

import com.rr.erp.entity.ItemSubSubCategory;
import com.rr.erp.service.ItemSubSubCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/item/sub-sub-categories")
@CrossOrigin(origins = "*")
public class ItemSubSubCategoryController {

    private final ItemSubSubCategoryService subSubCategoryService;

    public ItemSubSubCategoryController(ItemSubSubCategoryService subSubCategoryService) {
        this.subSubCategoryService = subSubCategoryService;
    }

    @PostMapping
    public ResponseEntity<ItemSubSubCategory> addSubSubCategory(
            @Valid @RequestBody ItemSubSubCategory request
    ) {
        ItemSubSubCategory saved = subSubCategoryService.addSubSubCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @GetMapping
    public ResponseEntity<List<ItemSubSubCategory>> getAllSubSubCategories() {
        return ResponseEntity.ok(subSubCategoryService.getAllSubSubCategories());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemSubSubCategory> updateSubSubCategory(
            @PathVariable Long id,
            @Valid @RequestBody ItemSubSubCategory request
    ) {
        ItemSubSubCategory updated = subSubCategoryService.updateSubSubCategory(id, request);

        return ResponseEntity.ok(updated);
    }
}
