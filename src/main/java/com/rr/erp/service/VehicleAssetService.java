package com.rr.erp.service;

import com.rr.erp.entity.Asset;
import com.rr.erp.entity.VehicleAssetDetail;
import com.rr.erp.repository.AssetCodeRepository;
import com.rr.erp.repository.AssetRepository;
import com.rr.erp.repository.VehicleAssetDetailRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VehicleAssetService {

    private static final String ASSET_CLASS = "Vehicle";

    private final AssetRepository assetRepository;
    private final VehicleAssetDetailRepository vehicleAssetDetailRepository;

    public List<com.rr.erp.dto.AssetExpiryEvent> getExpiryEvents() {
        return vehicleAssetDetailRepository.findExpiryEvents();
    }
    private final AssetCodeRepository assetCodeRepository;
    private final AssetCodeGenerator assetCodeGenerator;

    public VehicleAssetService(
            AssetRepository assetRepository,
            VehicleAssetDetailRepository vehicleAssetDetailRepository,
            AssetCodeRepository assetCodeRepository,
            AssetCodeGenerator assetCodeGenerator
    ) {
        this.assetRepository = assetRepository;
        this.vehicleAssetDetailRepository = vehicleAssetDetailRepository;
        this.assetCodeRepository = assetCodeRepository;
        this.assetCodeGenerator = assetCodeGenerator;
    }

    @Transactional
    public Asset createVehicleAsset(Asset asset) {

        VehicleAssetDetail detail = asset.getVehicleDetail();
        if (detail == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vehicle details are required to register a vehicle asset"
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
        vehicleAssetDetailRepository.insert(detail);

        return asset;
    }

    public Asset getVehicleAsset(String assetCode) {

        Asset asset = assetRepository.findByAssetCode(assetCode)
                .filter(a -> ASSET_CLASS.equals(a.getAssetClass()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vehicle asset not found: " + assetCode
                ));

        asset.setVehicleDetail(
                vehicleAssetDetailRepository.findByAssetCode(assetCode).orElse(null)
        );

        return asset;
    }

    public List<Asset> getAllVehicleAssets() {

        List<Asset> assets = assetRepository.findByAssetClass(ASSET_CLASS);

        java.util.Map<String, VehicleAssetDetail> detailsByCode = new java.util.HashMap<>();
        for (VehicleAssetDetail detail : vehicleAssetDetailRepository.findAll()) {
            detailsByCode.put(detail.getAssetCode(), detail);
        }
        for (Asset asset : assets) {
            asset.setVehicleDetail(detailsByCode.get(asset.getAssetCode()));
        }

        return assets;
    }

    @Transactional
    public Asset updateVehicleAsset(String assetCode, Asset asset) {

        Asset existing = assetRepository.findByAssetCode(assetCode)
                .filter(a -> ASSET_CLASS.equals(a.getAssetClass()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vehicle asset not found: " + assetCode
                ));

        VehicleAssetDetail detail = asset.getVehicleDetail();
        if (detail == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vehicle details are required to update a vehicle asset"
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
        vehicleAssetDetailRepository.update(detail);

        return asset;
    }
}
