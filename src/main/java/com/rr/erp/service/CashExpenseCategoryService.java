package com.rr.erp.service;

import com.rr.erp.entity.CashExpenseCategory;
import com.rr.erp.repository.CashExpenseCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CashExpenseCategoryService {

    private final CashExpenseCategoryRepository repository;


    // =========================================================
    // GET ALL
    // =========================================================

    public List<CashExpenseCategory> getAllCategories(Boolean isActive) {

        return repository.getAllCategories(isActive);
    }


    // =========================================================
    // CREATE
    // =========================================================

    @Transactional
    public CashExpenseCategory createCategory(CashExpenseCategory category) {

        validateAndNormalize(category, null);

        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }

        UUID id = repository.createCategory(category);

        return getCategoryById(id);
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Transactional
    public CashExpenseCategory updateCategory(
            UUID categoryId,
            CashExpenseCategory category) {

        CashExpenseCategory existing = getCategoryById(categoryId);

        validateAndNormalize(category, categoryId);

        if (category.getIsActive() == null) {
            category.setIsActive(existing.getIsActive());
        }

        repository.updateCategory(categoryId, category);

        return getCategoryById(categoryId);
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private CashExpenseCategory getCategoryById(UUID categoryId) {

        return repository.getCategoryById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cash expense category not found with id: " + categoryId
                ));
    }

    private void validateAndNormalize(
            CashExpenseCategory category,
            UUID excludeId) {

        String code = category.getCategoryCode().trim().toUpperCase();

        if (repository.categoryCodeExists(code, excludeId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category code already exists: " + code
            );
        }

        category.setCategoryCode(code);
        category.setCategoryName(category.getCategoryName().trim());
    }
}
