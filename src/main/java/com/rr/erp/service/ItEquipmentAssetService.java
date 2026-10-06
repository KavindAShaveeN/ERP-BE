package com.rr.erp.service;

import com.rr.erp.entity.Asset;
import com.rr.erp.entity.ItEquipmentAssetDetail;
import com.rr.erp.repository.AssetCodeRepository;
import com.rr.erp.repository.AssetRepository;
import com.rr.erp.repository.ItEquipmentAssetDetailRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ItEquipmentAssetService {

    private static final String ASSET_CLASS = "ITEquipment";

    private final AssetRepository assetRepository;
    private final ItEquipmentAssetDetailRepository itEquipmentAssetDetailRepository;
    private final AssetCodeRepository assetCodeRepository;
    private final AssetCodeGenerator assetCodeGenerator;

    public ItEquipmentAssetService(
            AssetRepository assetRepository,
            ItEquipmentAssetDetailRepository itEquipmentAssetDetailRepository,
            AssetCodeRepository assetCodeRepository,
            AssetCodeGenerator assetCodeGenerator
    ) {
        this.assetRepository = assetRepository;
        this.itEquipmentAssetDetailRepository = itEquipmentAssetDetailRepository;
        this.assetCodeRepository = assetCodeRepository;
        this.assetCodeGenerator = assetCodeGenerator;
    }

    @Transactional
    public Asset createItEquipmentAsset(Asset asset) {

        ItEquipmentAssetDetail detail = asset.getItEquipmentDetail();
        if (detail == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "IT equipment details are required to register an IT equipment asset"
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
        itEquipmentAssetDetailRepository.insert(detail);

        return asset;
    }

    public Asset getItEquipmentAsset(String assetCode) {

        Asset asset = assetRepository.findByAssetCode(assetCode)
                .filter(a -> ASSET_CLASS.equals(a.getAssetClass()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "IT equipment asset not found: " + assetCode
                ));

        asset.setItEquipmentDetail(
                itEquipmentAssetDetailRepository.findByAssetCode(assetCode).orElse(null)
        );

        return asset;
    }

    public List<Asset> getAllItEquipmentAssets() {

        List<Asset> assets = assetRepository.findByAssetClass(ASSET_CLASS);

        java.util.Map<String, ItEquipmentAssetDetail> detailsByCode = new java.util.HashMap<>();
        for (ItEquipmentAssetDetail detail : itEquipmentAssetDetailRepository.findAll()) {
            detailsByCode.put(detail.getAssetCode(), detail);
        }
        for (Asset asset : assets) {
            asset.setItEquipmentDetail(detailsByCode.get(asset.getAssetCode()));
        }

        return assets;
    }

    @Transactional
    public Asset updateItEquipmentAsset(String assetCode, Asset asset) {

        Asset existing = assetRepository.findByAssetCode(assetCode)
                .filter(a -> ASSET_CLASS.equals(a.getAssetClass()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "IT equipment asset not found: " + assetCode
                ));

        ItEquipmentAssetDetail detail = asset.getItEquipmentDetail();
        if (detail == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "IT equipment details are required to update an IT equipment asset"
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
        itEquipmentAssetDetailRepository.update(detail);

        return asset;
    }
}
