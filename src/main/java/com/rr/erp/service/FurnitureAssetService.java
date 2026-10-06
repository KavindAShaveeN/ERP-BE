package com.rr.erp.service;

import com.rr.erp.entity.Asset;
import com.rr.erp.entity.FurnitureAssetDetail;
import com.rr.erp.repository.AssetCodeRepository;
import com.rr.erp.repository.AssetRepository;
import com.rr.erp.repository.FurnitureAssetDetailRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FurnitureAssetService {

    private static final String ASSET_CLASS = "Furniture";

    private final AssetRepository assetRepository;
    private final FurnitureAssetDetailRepository furnitureAssetDetailRepository;
    private final AssetCodeRepository assetCodeRepository;
    private final AssetCodeGenerator assetCodeGenerator;

    public FurnitureAssetService(
            AssetRepository assetRepository,
            FurnitureAssetDetailRepository furnitureAssetDetailRepository,
            AssetCodeRepository assetCodeRepository,
            AssetCodeGenerator assetCodeGenerator
    ) {
        this.assetRepository = assetRepository;
        this.furnitureAssetDetailRepository = furnitureAssetDetailRepository;
        this.assetCodeRepository = assetCodeRepository;
        this.assetCodeGenerator = assetCodeGenerator;
    }

    @Transactional
    public Asset createFurnitureAsset(Asset asset) {

        FurnitureAssetDetail detail = asset.getFurnitureDetail();
        if (detail == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Furniture details are required to register a furniture asset"
            );
        }

        if (!assetCodeRepository.existsById(asset.getAssetCodeId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Asset code not found with id: " + asset.getAssetCodeId()
            );
        }

        if (asset.getStatus() == null || asset.getStatus().isBlank()) {
            asset.setStatus("Active");
        }

        LocalDateTime now = LocalDateTime.now();
        asset.setAssetCode(assetCodeGenerator.generate(asset.getAssetCodeId()));
        asset.setAssetClass(ASSET_CLASS);
        asset.setCreatedAt(now);
        asset.setUpdatedAt(now);

        assetRepository.insert(asset);

        detail.setAssetCode(asset.getAssetCode());
        furnitureAssetDetailRepository.insert(detail);

        return asset;
    }

    public Asset getFurnitureAsset(String assetCode) {

        Asset asset = assetRepository.findByAssetCode(assetCode)
                .filter(a -> ASSET_CLASS.equals(a.getAssetClass()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Furniture asset not found: " + assetCode
                ));

        asset.setFurnitureDetail(
                furnitureAssetDetailRepository.findByAssetCode(assetCode).orElse(null)
        );

        return asset;
    }

    public List<Asset> getAllFurnitureAssets() {

        List<Asset> assets = assetRepository.findByAssetClass(ASSET_CLASS);

        java.util.Map<String, FurnitureAssetDetail> detailsByCode = new java.util.HashMap<>();
        for (FurnitureAssetDetail detail : furnitureAssetDetailRepository.findAll()) {
            detailsByCode.put(detail.getAssetCode(), detail);
        }
        for (Asset asset : assets) {
            asset.setFurnitureDetail(detailsByCode.get(asset.getAssetCode()));
        }

        return assets;
    }

    @Transactional
    public Asset updateFurnitureAsset(String assetCode, Asset asset) {

        Asset existing = assetRepository.findByAssetCode(assetCode)
                .filter(a -> ASSET_CLASS.equals(a.getAssetClass()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Furniture asset not found: " + assetCode
                ));

        FurnitureAssetDetail detail = asset.getFurnitureDetail();
        if (detail == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Furniture details are required to update a furniture asset"
            );
        }

        if (!assetCodeRepository.existsById(asset.getAssetCodeId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Asset code not found with id: " + asset.getAssetCodeId()
            );
        }

        asset.setAssetCode(assetCode);
        asset.setAssetClass(ASSET_CLASS);
        asset.setCreatedAt(existing.getCreatedAt());
        asset.setUpdatedAt(LocalDateTime.now());

        assetRepository.update(asset);

        detail.setAssetCode(assetCode);
        furnitureAssetDetailRepository.update(detail);

        return asset;
    }
}
