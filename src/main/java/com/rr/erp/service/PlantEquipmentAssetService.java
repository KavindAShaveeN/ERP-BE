package com.rr.erp.service;

import com.rr.erp.entity.Asset;
import com.rr.erp.entity.PlantEquipmentAssetDetail;
import com.rr.erp.repository.AssetCodeRepository;
import com.rr.erp.repository.AssetRepository;
import com.rr.erp.repository.PlantEquipmentAssetDetailRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlantEquipmentAssetService {

    private static final String ASSET_CLASS = "PlantEquipment";

    private final AssetRepository assetRepository;
    private final PlantEquipmentAssetDetailRepository plantEquipmentAssetDetailRepository;
    private final AssetCodeRepository assetCodeRepository;
    private final AssetCodeGenerator assetCodeGenerator;

    public PlantEquipmentAssetService(
            AssetRepository assetRepository,
            PlantEquipmentAssetDetailRepository plantEquipmentAssetDetailRepository,
            AssetCodeRepository assetCodeRepository,
            AssetCodeGenerator assetCodeGenerator
    ) {
        this.assetRepository = assetRepository;
        this.plantEquipmentAssetDetailRepository = plantEquipmentAssetDetailRepository;
        this.assetCodeRepository = assetCodeRepository;
        this.assetCodeGenerator = assetCodeGenerator;
    }

    @Transactional
    public Asset createPlantEquipmentAsset(Asset asset) {

        PlantEquipmentAssetDetail detail = asset.getPlantEquipmentDetail();
        if (detail == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Plant equipment details are required to register a plant equipment asset"
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
        plantEquipmentAssetDetailRepository.insert(detail);

        return asset;
    }

    public Asset getPlantEquipmentAsset(String assetCode) {

        Asset asset = assetRepository.findByAssetCode(assetCode)
                .filter(a -> ASSET_CLASS.equals(a.getAssetClass()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Plant equipment asset not found: " + assetCode
                ));

        asset.setPlantEquipmentDetail(
                plantEquipmentAssetDetailRepository.findByAssetCode(assetCode).orElse(null)
        );

        return asset;
    }

    public List<Asset> getAllPlantEquipmentAssets() {

        List<Asset> assets = assetRepository.findByAssetClass(ASSET_CLASS);

        java.util.Map<String, PlantEquipmentAssetDetail> detailsByCode = new java.util.HashMap<>();
        for (PlantEquipmentAssetDetail detail : plantEquipmentAssetDetailRepository.findAll()) {
            detailsByCode.put(detail.getAssetCode(), detail);
        }
        for (Asset asset : assets) {
            asset.setPlantEquipmentDetail(detailsByCode.get(asset.getAssetCode()));
        }

        return assets;
    }

    @Transactional
    public Asset updatePlantEquipmentAsset(String assetCode, Asset asset) {

        Asset existing = assetRepository.findByAssetCode(assetCode)
                .filter(a -> ASSET_CLASS.equals(a.getAssetClass()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Plant equipment asset not found: " + assetCode
                ));

        PlantEquipmentAssetDetail detail = asset.getPlantEquipmentDetail();
        if (detail == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Plant equipment details are required to update a plant equipment asset"
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
        plantEquipmentAssetDetailRepository.update(detail);

        return asset;
    }
}
