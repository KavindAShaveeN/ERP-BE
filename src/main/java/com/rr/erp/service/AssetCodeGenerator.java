package com.rr.erp.service;

import com.rr.erp.entity.AssetCode;
import com.rr.erp.repository.AssetCodeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Resolves the asset code string a newly registered asset should be stored under. The asset
 * code (e.g. "V-C-002") is already unique per physical asset — enforced when it's created via
 * AssetCodeService — so a registered asset's identifier is simply that code, unchanged. Shared
 * by every *AssetService (VehicleAssetService, MachineryAssetService, ...) so this lookup only
 * lives in one place. */
@Service
public class AssetCodeGenerator {

    private final AssetCodeRepository assetCodeRepository;

    public AssetCodeGenerator(AssetCodeRepository assetCodeRepository) {
        this.assetCodeRepository = assetCodeRepository;
    }

    public String generate(Long assetCodeId) {

        AssetCode assetCode = assetCodeRepository.findById(assetCodeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Asset code not found with id: " + assetCodeId
                ));

        return assetCode.getAssetCodeCode();
    }
}
