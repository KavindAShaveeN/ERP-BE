package com.rr.erp.service;

import com.rr.erp.entity.ItemSubSubCategory;
import com.rr.erp.repository.ItemSubCategoryRepository;
import com.rr.erp.repository.ItemSubSubCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemSubSubCategoryService {

    private final ItemSubSubCategoryRepository subSubCategoryRepository;
    private final ItemSubCategoryRepository subCategoryRepository;

    public ItemSubSubCategoryService(
            ItemSubSubCategoryRepository subSubCategoryRepository,
            ItemSubCategoryRepository subCategoryRepository
    ) {
        this.subSubCategoryRepository = subSubCategoryRepository;
        this.subCategoryRepository = subCategoryRepository;
    }

    private void validateParent(ItemSubSubCategory subSubCategory) {
        if (!subCategoryRepository.existsById(subSubCategory.getItemSubCategoryId())) {
            throw new IllegalArgumentException("Sub category not found with id: " + subSubCategory.getItemSubCategoryId());
        }
    }

    public ItemSubSubCategory addSubSubCategory(ItemSubSubCategory subSubCategory) {

        validateParent(subSubCategory);

        if (subSubCategoryRepository.existsByParentIdAndCode(subSubCategory.getItemSubCategoryId(), subSubCategory.getItemSubSubCategoryCode())) {
            throw new IllegalArgumentException(
                    "Sub sub category code already exists under this sub category: " + subSubCategory.getItemSubSubCategoryCode()
            );
        }

        Long id = subSubCategoryRepository.addSubSubCategory(subSubCategory);
        subSubCategory.setItemSubSubCategoryId(id);

        return subSubCategory;
    }

    public List<ItemSubSubCategory> getAllSubSubCategories() {
        return subSubCategoryRepository.getAllSubSubCategories();
    }

    public ItemSubSubCategory updateSubSubCategory(Long id, ItemSubSubCategory subSubCategory) {

        validateParent(subSubCategory);

        if (subSubCategoryRepository.existsByParentIdAndCodeExcludingId(subSubCategory.getItemSubCategoryId(), subSubCategory.getItemSubSubCategoryCode(), id)) {
            throw new IllegalArgumentException(
                    "Sub sub category code already exists under this sub category: " + subSubCategory.getItemSubSubCategoryCode()
            );
        }

        int affectedRows = subSubCategoryRepository.updateSubSubCategory(id, subSubCategory);

        if (affectedRows == 0) {
            throw new IllegalArgumentException("Sub sub category not found with id: " + id);
        }

        subSubCategory.setItemSubSubCategoryId(id);

        return subSubCategory;
    }
}
