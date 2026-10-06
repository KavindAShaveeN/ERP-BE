package com.rr.erp.service;

import com.rr.erp.dto.ItemCodeDetailResponse;
import com.rr.erp.entity.ItemCode;
import com.rr.erp.repository.ItemBrandRepository;
import com.rr.erp.repository.ItemCategoryRepository;
import com.rr.erp.repository.ItemCodeRepository;
import com.rr.erp.repository.ItemModelRepository;
import com.rr.erp.repository.ItemOptionalOneRepository;
import com.rr.erp.repository.ItemOptionalThreeRepository;
import com.rr.erp.repository.ItemOptionalTwoRepository;
import com.rr.erp.repository.ItemSubCategoryRepository;
import com.rr.erp.repository.ItemSubSubCategoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ItemCodeService {

    private final ItemCodeRepository itemCodeRepository;
    private final ItemCategoryRepository categoryRepository;
    private final ItemSubCategoryRepository subCategoryRepository;
    private final ItemSubSubCategoryRepository subSubCategoryRepository;
    private final ItemBrandRepository brandRepository;
    private final ItemModelRepository modelRepository;
    private final ItemOptionalOneRepository optionalOneRepository;
    private final ItemOptionalTwoRepository optionalTwoRepository;
    private final ItemOptionalThreeRepository optionalThreeRepository;

    public ItemCodeService(
            ItemCodeRepository itemCodeRepository,
            ItemCategoryRepository categoryRepository,
            ItemSubCategoryRepository subCategoryRepository,
            ItemSubSubCategoryRepository subSubCategoryRepository,
            ItemBrandRepository brandRepository,
            ItemModelRepository modelRepository,
            ItemOptionalOneRepository optionalOneRepository,
            ItemOptionalTwoRepository optionalTwoRepository,
            ItemOptionalThreeRepository optionalThreeRepository
    ) {
        this.itemCodeRepository = itemCodeRepository;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.subSubCategoryRepository = subSubCategoryRepository;
        this.brandRepository = brandRepository;
        this.modelRepository = modelRepository;
        this.optionalOneRepository = optionalOneRepository;
        this.optionalTwoRepository = optionalTwoRepository;
        this.optionalThreeRepository = optionalThreeRepository;
    }

    private void validateForeignKeys(ItemCode itemCode) {

        if (itemCode.getItemCategoryId() != null && !categoryRepository.existsById(itemCode.getItemCategoryId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Category not found with id: " + itemCode.getItemCategoryId());
        }
        if (itemCode.getItemSubCategoryId() != null && !subCategoryRepository.existsById(itemCode.getItemSubCategoryId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Sub category not found with id: " + itemCode.getItemSubCategoryId());
        }
        if (itemCode.getItemSubSubCategoryId() != null && !subSubCategoryRepository.existsById(itemCode.getItemSubSubCategoryId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Sub sub category not found with id: " + itemCode.getItemSubSubCategoryId());
        }
        if (itemCode.getItemBrandId() != null && !brandRepository.existsById(itemCode.getItemBrandId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Brand not found with id: " + itemCode.getItemBrandId());
        }
        if (itemCode.getItemModelId() != null && !modelRepository.existsById(itemCode.getItemModelId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Model not found with id: " + itemCode.getItemModelId());
        }
        if (itemCode.getItemOptionalOneId() != null && !optionalOneRepository.existsById(itemCode.getItemOptionalOneId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Optional 1 not found with id: " + itemCode.getItemOptionalOneId());
        }
        if (itemCode.getItemOptionalTwoId() != null && !optionalTwoRepository.existsById(itemCode.getItemOptionalTwoId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Optional 2 not found with id: " + itemCode.getItemOptionalTwoId());
        }
        if (itemCode.getItemOptionalThreeId() != null && !optionalThreeRepository.existsById(itemCode.getItemOptionalThreeId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Optional 3 not found with id: " + itemCode.getItemOptionalThreeId());
        }
    }

    public ItemCode addItemCode(ItemCode itemCode) {

        validateForeignKeys(itemCode);

        if (itemCodeRepository.existsByCode(itemCode.getItemCodeCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Item code already exists: " + itemCode.getItemCodeCode());
        }

        Long id = itemCodeRepository.addItemCode(itemCode);
        itemCode.setItemCodeId(id);

        return itemCode;
    }

    public List<ItemCodeDetailResponse> getAllItemCodes() {
        return itemCodeRepository.getAllItemCodes();
    }

    public ItemCode getItemCodeById(Long id) {
        return itemCodeRepository.getItemCodeById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Item code not found with id: " + id));
    }

    public ItemCode updateItemCodeName(Long id, String name) {

        int affectedRows = itemCodeRepository.updateName(id, name.trim());

        if (affectedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Item code not found with id: " + id);
        }

        return getItemCodeById(id);
    }

    public void deleteItemCode(Long id) {

        try {
            int affectedRows = itemCodeRepository.deleteById(id);

            if (affectedRows == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Item code not found with id: " + id);
            }
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Item code cannot be deleted because it is referenced by another record", e);
        }
    }
}
