package com.rr.erp.service;


import com.rr.erp.dto.AssetCodeRequestDTO;
import com.rr.erp.entity.AssetCode;
import com.rr.erp.entity.ItemCategory;
import com.rr.erp.entity.ItemCode;
import com.rr.erp.entity.ItemSubCategory;
import com.rr.erp.repository.AssetCodeRepository;
import com.rr.erp.repository.ItemCategoryRepository;
import com.rr.erp.repository.ItemCodeRepository;
import com.rr.erp.repository.ItemSubCategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AssetCodeService {

    private final AssetCodeRepository assetCodeRepository;
    private final ItemCategoryRepository categoryRepository;
    private final ItemSubCategoryRepository subCategoryRepository;
    private final ItemCodeRepository itemCodeRepository;

    public AssetCodeService(
            AssetCodeRepository assetCodeRepository,
            ItemCategoryRepository categoryRepository,
            ItemSubCategoryRepository subCategoryRepository,
            ItemCodeRepository itemCoderRepository
    ) {
        this.assetCodeRepository = assetCodeRepository;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.itemCodeRepository = itemCoderRepository;
    }

    @Transactional
    public AssetCode createAssetCode(AssetCodeRequestDTO request) {

        if (!categoryRepository.existsById(request.getItemCategoryId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Item category not found with id: " + request.getItemCategoryId()
            );
        }

        if (!subCategoryRepository.existsById(request.getItemSubCategoryId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Item sub category not found with id: " + request.getItemSubCategoryId()
            );
        }

        String assetCodeValue = request.getAssetCodeCode().trim();

        if (assetCodeRepository.existsByAssetCodeCode(assetCodeValue)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Asset code already exists: " + assetCodeValue
            );
        }

//        request.setAssetCodeCode(assetCodeValue);
        ItemCategory category = categoryRepository.getItemCategoryById(request.getItemCategoryId());
        ItemSubCategory subCategory = subCategoryRepository.getItemSubCategoryById(request.getItemSubCategoryId());
        ItemCode code = new ItemCode ();
        code.setItemCodeCode(category.getItemCategoryCode() + "-" + subCategory.getItemSubCategoryCode());
        code.setItemCodeName(category.getItemCategoryName() + "," + subCategory.getItemSubCategoryName());
        code.setItemCategoryId(request.getItemCategoryId());
        code.setItemSubCategoryId(request.getItemSubCategoryId());
        if(!itemCodeRepository.existsByCode(code.getItemCodeCode())){
            request.setItemCodeId(itemCodeRepository.addItemCode(code));
        }
        else{
            request.setItemCodeId(itemCodeRepository.getIdByCode(code.getItemCodeCode()));
        }

        try {

            return assetCodeRepository.save(request);

        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid item category ID, item sub category ID, or duplicate asset code"
            );
        }
    }

    public List<AssetCode> getAllAssetCodes() {
        return assetCodeRepository.getAllAssetCodes();
    }

    public AssetCode getAssetCode(String assetCodeCode) {

        return assetCodeRepository
                .findByAssetCodeCode(assetCodeCode.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Asset code not found: " + assetCodeCode
                ));
    }

    public void deleteAssetCode(String assetCodeCode) {

        boolean deleted = assetCodeRepository.deleteByAssetCodeCode(
                assetCodeCode.trim()
        );

        if (!deleted) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Asset code not found: " + assetCodeCode
            );
        }
    }
}
