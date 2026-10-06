package com.rr.erp.service;

import com.rr.erp.entity.Asset;
import com.rr.erp.entity.ElectricalEquipmentAssetDetail;
import com.rr.erp.repository.AssetCodeRepository;
import com.rr.erp.repository.AssetRepository;
import com.rr.erp.repository.ElectricalEquipmentAssetDetailRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ElectricalEquipmentAssetService {

    private static final String ASSET_CLASS = "ElectricalEquipment";

    private final AssetRepository assetRepository;
    private final ElectricalEquipmentAssetDetailRepository electricalEquipmentAssetDetailRepository;
    private final AssetCodeRepository assetCodeRepository;
    private final AssetCodeGenerator assetCodeGenerator;

    public ElectricalEquipmentAssetService(
            AssetRepository assetRepository,
            ElectricalEquipmentAssetDetailRepository electricalEquipmentAssetDetailRepository,
            AssetCodeRepository assetCodeRepository,
            AssetCodeGenerator assetCodeGenerator
    ) {
        this.assetRepository = assetRepository;
        this.electricalEquipmentAssetDetailRepository = electricalEquipmentAssetDetailRepository;
        this.assetCodeRepository = assetCodeRepository;
        this.assetCodeGenerator = assetCodeGenerator;
    }

    @Transactional
    public Asset createElectricalEquipmentAsset(Asset asset) {

        ElectricalEquipmentAssetDetail detail = asset.getElectricalEquipmentDetail();
        if (detail == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Electrical equipment details are required to register an electrical equipment asset"
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
        electricalEquipmentAssetDetailRepository.insert(detail);

        return asset;
    }

    public Asset getElectricalEquipmentAsset(String assetCode) {

        Asset asset = assetRepository.findByAssetCode(assetCode)
                .filter(a -> ASSET_CLASS.equals(a.getAssetClass()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Electrical equipment asset not found: " + assetCode
                ));

        asset.setElectricalEquipmentDetail(
                electricalEquipmentAssetDetailRepository.findByAssetCode(assetCode).orElse(null)
        );

        return asset;
    }

    public List<Asset> getAllElectricalEquipmentAssets() {

        List<Asset> assets = assetRepository.findByAssetClass(ASSET_CLASS);

        java.util.Map<String, ElectricalEquipmentAssetDetail> detailsByCode = new java.util.HashMap<>();
        for (ElectricalEquipmentAssetDetail detail : electricalEquipmentAssetDetailRepository.findAll()) {
            detailsByCode.put(detail.getAssetCode(), detail);
        }
        for (Asset asset : assets) {
            asset.setElectricalEquipmentDetail(detailsByCode.get(asset.getAssetCode()));
        }

        return assets;
    }

    @Transactional
    public Asset updateElectricalEquipmentAsset(String assetCode, Asset asset) {

        Asset existing = assetRepository.findByAssetCode(assetCode)
                .filter(a -> ASSET_CLASS.equals(a.getAssetClass()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Electrical equipment asset not found: " + assetCode
                ));

        ElectricalEquipmentAssetDetail detail = asset.getElectricalEquipmentDetail();
        if (detail == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Electrical equipment details are required to update an electrical equipment asset"
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
        electricalEquipmentAssetDetailRepository.update(detail);

        return asset;
    }
}
