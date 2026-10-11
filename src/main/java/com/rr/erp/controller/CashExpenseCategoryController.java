package com.rr.erp.controller;

import com.rr.erp.entity.CashExpenseCategory;
import com.rr.erp.service.CashExpenseCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cash_expense_categories")
@RequiredArgsConstructor
@CrossOrigin
public class CashExpenseCategoryController {

    private final CashExpenseCategoryService service;


    // ==================================================
    // GET /api/cash_expense_categories?isActive=true
    // ==================================================
    @GetMapping
    public ResponseEntity<List<CashExpenseCategory>> getAllCategories(
            @RequestParam(required = false) Boolean isActive
    ) {

        return ResponseEntity.ok(
                service.getAllCategories(isActive)
        );
    }


    // ==================================================
    // POST /api/cash_expense_categories
    // ==================================================
    @PostMapping
    public ResponseEntity<CashExpenseCategory> createCategory(
            @Valid @RequestBody CashExpenseCategory category
    ) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                service.createCategory(category)
        );
    }


    // ==================================================
    // PUT /api/cash_expense_categories/{categoryId}
    // ==================================================
    @PutMapping("/{categoryId}")
    public ResponseEntity<CashExpenseCategory> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody CashExpenseCategory category
    ) {

        return ResponseEntity.ok(
                service.updateCategory(
                        categoryId,
                        category
                )
        );
    }
}
