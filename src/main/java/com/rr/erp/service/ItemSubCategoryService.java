package com.rr.erp.service;

import com.rr.erp.entity.ItemSubCategory;
import com.rr.erp.repository.ItemCategoryRepository;
import com.rr.erp.repository.ItemSubCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemSubCategoryService {

    private final ItemSubCategoryRepository subCategoryRepository;
    private final ItemCategoryRepository categoryRepository;

    public ItemSubCategoryService(
            ItemSubCategoryRepository subCategoryRepository,
            ItemCategoryRepository categoryRepository
    ) {
        this.subCategoryRepository = subCategoryRepository;
        this.categoryRepository = categoryRepository;
    }

    private void validateParent(ItemSubCategory subCategory) {
        if (!categoryRepository.existsById(subCategory.getItemCategoryId())) {
            throw new IllegalArgumentException("Category not found with id: " + subCategory.getItemCategoryId());
        }
    }

    public ItemSubCategory addSubCategory(ItemSubCategory subCategory) {

        validateParent(subCategory);

        if (subCategoryRepository.existsByParentIdAndCode(subCategory.getItemCategoryId(), subCategory.getItemSubCategoryCode())) {
            throw new IllegalArgumentException(
                    "Sub category code already exists under this category: " + subCategory.getItemSubCategoryCode()
            );
        }

        Long id = subCategoryRepository.addSubCategory(subCategory);
        subCategory.setItemSubCategoryId(id);

        return subCategory;
    }

    public List<ItemSubCategory> getAllSubCategories() {
        return subCategoryRepository.getAllSubCategories();
    }

    public ItemSubCategory updateSubCategory(Long id, ItemSubCategory subCategory) {

        validateParent(subCategory);

        if (subCategoryRepository.existsByParentIdAndCodeExcludingId(subCategory.getItemCategoryId(), subCategory.getItemSubCategoryCode(), id)) {
            throw new IllegalArgumentException(
                    "Sub category code already exists under this category: " + subCategory.getItemSubCategoryCode()
            );
        }

        int affectedRows = subCategoryRepository.updateSubCategory(id, subCategory);

        if (affectedRows == 0) {
            throw new IllegalArgumentException("Sub category not found with id: " + id);
        }

        subCategory.setItemSubCategoryId(id);

        return subCategory;
    }
}
