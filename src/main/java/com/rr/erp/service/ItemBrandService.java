package com.rr.erp.service;

import com.rr.erp.entity.ItemBrand;
import com.rr.erp.repository.ItemBrandRepository;
import com.rr.erp.repository.ItemSubSubCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemBrandService {

    private final ItemBrandRepository brandRepository;
    private final ItemSubSubCategoryRepository subSubCategoryRepository;

    public ItemBrandService(
            ItemBrandRepository brandRepository,
            ItemSubSubCategoryRepository subSubCategoryRepository
    ) {
        this.brandRepository = brandRepository;
        this.subSubCategoryRepository = subSubCategoryRepository;
    }

    private void validateParent(ItemBrand brand) {
        if (!subSubCategoryRepository.existsById(brand.getItemSubSubCategoryId())) {
            throw new IllegalArgumentException("Sub sub category not found with id: " + brand.getItemSubSubCategoryId());
        }
    }

    public ItemBrand addBrand(ItemBrand brand) {

        validateParent(brand);

        if (brandRepository.existsByParentIdAndCode(brand.getItemSubSubCategoryId(), brand.getItemBrandCode())) {
            throw new IllegalArgumentException(
                    "Brand code already exists under this sub sub category: " + brand.getItemBrandCode()
            );
        }

        Long id = brandRepository.addBrand(brand);
        brand.setItemBrandId(id);

        return brand;
    }

    public List<ItemBrand> getAllBrands() {
        return brandRepository.getAllBrands();
    }

    public ItemBrand updateBrand(Long id, ItemBrand brand) {

        validateParent(brand);

        if (brandRepository.existsByParentIdAndCodeExcludingId(brand.getItemSubSubCategoryId(), brand.getItemBrandCode(), id)) {
            throw new IllegalArgumentException(
                    "Brand code already exists under this sub sub category: " + brand.getItemBrandCode()
            );
        }

        int affectedRows = brandRepository.updateBrand(id, brand);

        if (affectedRows == 0) {
            throw new IllegalArgumentException("Brand not found with id: " + id);
        }

        brand.setItemBrandId(id);

        return brand;
    }
}
