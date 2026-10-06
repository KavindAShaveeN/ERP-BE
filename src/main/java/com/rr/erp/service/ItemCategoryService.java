package com.rr.erp.service;

import com.rr.erp.entity.ItemCategory;
import com.rr.erp.repository.ItemCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ItemCategoryService {

    private final ItemCategoryRepository categoryRepository;

    public ItemCategoryService(ItemCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }


    public ItemCategory addCategory(ItemCategory category) {

        if (categoryRepository.existsByTypeAndCode(category.getItemTypeId(), category.getItemCategoryCode())) {
            throw new IllegalArgumentException(
                    "Category code already exists for type " + category.getItemTypeId() + ": " + category.getItemCategoryCode()
            );
        }

        Long id = categoryRepository.addCategory(category);
        category.setItemCategoryId(id);

        return category;
    }

    public List<ItemCategory> getAllCategories() {
        return categoryRepository.getAllCategories();
    }

    public ItemCategory updateCategory(Long id, ItemCategory category) {

        if (categoryRepository.existsByTypeAndCodeExcludingId(category.getItemTypeId(), category.getItemCategoryCode(), id)) {
            throw new IllegalArgumentException(
                    "Category code already exists for type " + category.getItemTypeId() + ": " + category.getItemCategoryCode()
            );
        }

        int affectedRows = categoryRepository.updateCategory(id, category);

        if (affectedRows == 0) {
            throw new IllegalArgumentException("Category not found with id: " + id);
        }

        category.setItemCategoryId(id);

        return category;
    }
}
