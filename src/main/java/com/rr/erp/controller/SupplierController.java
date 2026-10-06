package com.rr.erp.controller;

import com.rr.erp.entity.Supplier;
import com.rr.erp.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin(origins = "*")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    public ResponseEntity<Supplier> addSupplier(
            @Valid @RequestBody Supplier supplierRequest
    )
    {
        Supplier savedSupplier =
                supplierService.addSupplier(supplierRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedSupplier);
    }
    @GetMapping
    public ResponseEntity<List<Supplier>> getAllSuppliers() {

        List<Supplier> suppliers =
                supplierService.getAllSuppliers();

        return ResponseEntity.ok(suppliers);
    }
    @PutMapping("/{supplierCode}")
    public ResponseEntity<Supplier> updateSupplier(
            @PathVariable String supplierCode,
            @Valid @RequestBody Supplier supplierRequest
    ) {

        Supplier updatedSupplier =
                supplierService.updateSupplier(
                        supplierCode,
                        supplierRequest
                );

        return ResponseEntity.ok(updatedSupplier);
    }
}