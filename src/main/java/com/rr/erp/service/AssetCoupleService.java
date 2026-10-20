package com.rr.erp.service;

import com.rr.erp.entity.AssetCouple;
import com.rr.erp.repository.AssetCoupleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AssetCoupleService {

    private final AssetCoupleRepository repository;

    public AssetCoupleService(AssetCoupleRepository repository) {
        this.repository = repository;
    }

    public List<AssetCouple> getByAsset(String assetCode) {
        return repository.findByAssetCode(assetCode);
    }

    @Transactional
    public List<AssetCouple> add(String assetCode, String coupledAssetCode) {
        if (coupledAssetCode == null || coupledAssetCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coupled asset code is required.");
        }
        String other = coupledAssetCode.trim();
        if (other.equals(assetCode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An asset cannot be coupled with itself.");
        }
        requireAsset(assetCode);
        requireAsset(other);

        String a = assetCode.compareTo(other) < 0 ? assetCode : other;
        String b = a.equals(assetCode) ? other : assetCode;
        if (!repository.insert(a, b)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Assets " + assetCode + " and " + other + " are already coupled.");
        }
        return repository.findByAssetCode(assetCode);
    }

    @Transactional
    public List<AssetCouple> remove(String assetCode, Long coupleId) {
        if (repository.delete(coupleId, assetCode) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Couple not found: " + coupleId);
        }
        return repository.findByAssetCode(assetCode);
    }

    private void requireAsset(String assetCode) {
        if (!repository.assetExists(assetCode)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found: " + assetCode);
        }
    }
}
