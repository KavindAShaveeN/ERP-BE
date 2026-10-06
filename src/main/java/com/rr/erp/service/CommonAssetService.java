package com.rr.erp.service;

import com.rr.erp.entity.Asset;
import com.rr.erp.repository.AssetCodeRepository;
import com.rr.erp.repository.AssetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/** Register / read / update assets of a class that has no class-specific detail table — only the
 * common fields on the shared asset table. Same rules as the class-specific services (e.g.
 * MachineryAssetService), minus the nested detail object. */
@Service
public class CommonAssetService {

    private final AssetRepository assetRepository;
    private final AssetCodeRepository assetCodeRepository;
    private final AssetCodeGenerator assetCodeGenerator;

    public CommonAssetService(
            AssetRepository assetRepository,
            AssetCodeRepository assetCodeRepository,
            AssetCodeGenerator assetCodeGenerator
    ) {
        this.assetRepository = assetRepository;
        this.assetCodeRepository = assetCodeRepository;
        this.assetCodeGenerator = assetCodeGenerator;
    }

    @Transactional
    public Asset create(String assetClass, Asset asset) {

        if (asset.getAssetCodeId() == null || !assetCodeRepository.existsById(asset.getAssetCodeId())) {
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
        asset.setAssetClass(assetClass);
        asset.setCreatedAt(now);
        asset.setUpdatedAt(now);

        assetRepository.insert(asset);

        return asset;
    }

    public Asset get(String assetClass, String assetCode) {

        return assetRepository.findByAssetCode(assetCode)
                .filter(asset -> assetClass.equals(asset.getAssetClass()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        assetClass + " asset not found: " + assetCode
                ));
    }

    public List<Asset> getAll(String assetClass) {
        return assetRepository.findByAssetClass(assetClass);
    }

    @Transactional
    public Asset update(String assetClass, String assetCode, Asset asset) {

        Asset existing = get(assetClass, assetCode);

        if (asset.getAssetCodeId() == null || !assetCodeRepository.existsById(asset.getAssetCodeId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Asset code not found with id: " + asset.getAssetCodeId()
            );
        }

        asset.setAssetCode(assetCode);
        asset.setAssetClass(assetClass);
        asset.setCreatedAt(existing.getCreatedAt());
        asset.setUpdatedAt(LocalDateTime.now());

        assetRepository.update(asset);

        return asset;
    }
}
