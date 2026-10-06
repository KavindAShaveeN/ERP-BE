package com.rr.erp.controller;

import com.rr.erp.entity.ItemBrand;
import com.rr.erp.service.ItemBrandService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/item/brands")
@CrossOrigin(origins = "*")
public class ItemBrandController {

    private final ItemBrandService brandService;

    public ItemBrandController(ItemBrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping
    public ResponseEntity<ItemBrand> addBrand(
            @Valid @RequestBody ItemBrand request
    ) {
        ItemBrand saved = brandService.addBrand(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @GetMapping
    public ResponseEntity<List<ItemBrand>> getAllBrands() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemBrand> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody ItemBrand request
    ) {
        ItemBrand updated = brandService.updateBrand(id, request);

        return ResponseEntity.ok(updated);
    }
}
