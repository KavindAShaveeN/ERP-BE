package com.rr.erp.service;

import com.rr.erp.dto.AssetDocumentRemark;
import com.rr.erp.dto.OperatorAssetResponse;
import com.rr.erp.dto.PagedResponse;
import com.rr.erp.entity.Asset;
import com.rr.erp.repository.AssetDocumentRemarkRepository;
import com.rr.erp.repository.AssetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AssetService {

    private static final int MAX_PAGE_SIZE = 200;
    private static final int MAX_DOCUMENT_REMARK_LENGTH = 50_000;

    private final AssetRepository assetRepository;
    private final AssetDocumentRemarkRepository documentRemarkRepository;

    public AssetService(AssetRepository assetRepository, AssetDocumentRemarkRepository documentRemarkRepository) {
        this.assetRepository = assetRepository;
        this.documentRemarkRepository = documentRemarkRepository;
    }

    /** Common-fields-only lookup, independent of asset class — lets a caller that only
     * has an asset code discover its assetClass (and assetCodeId) before it knows
     * which class-specific endpoint (/api/assets/vehicles, /api/assets/machinery, ...)
     * to call for the full class-specific detail. */
    public Asset getAsset(String assetCode) {
        return assetRepository.findByAssetCode(assetCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Asset not found: " + assetCode
                ));
    }

    /** The Documents-section remark for an asset — empty text when none has been written yet. */
    public AssetDocumentRemark getDocumentRemark(String assetCode) {

        requireAsset(assetCode);

        return documentRemarkRepository.findByAssetCode(assetCode)
                .orElseGet(() -> new AssetDocumentRemark(assetCode, "", null));
    }

    public AssetDocumentRemark saveDocumentRemark(String assetCode, String remark) {

        requireAsset(assetCode);

        String text = remark == null ? "" : remark;
        if (text.length() > MAX_DOCUMENT_REMARK_LENGTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Remark is too long (maximum " + MAX_DOCUMENT_REMARK_LENGTH + " characters)"
            );
        }

        documentRemarkRepository.save(assetCode, text);
        return documentRemarkRepository.findByAssetCode(assetCode)
                .orElseGet(() -> new AssetDocumentRemark(assetCode, text, null));
    }

    private void requireAsset(String assetCode) {
        if (!assetRepository.existsByAssetCode(assetCode)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found: " + assetCode);
        }
    }

    public List<String> getAllAssetCodes() {
        return assetRepository.findAllAssetCodes();
    }

    /** Codes of every asset matching the filters (all assets when none are given). */
    public List<String> getMatchingAssetCodes(String search, String assetClass, String status) {
        return assetRepository.findMatchingAssetCodes(search, assetClass, status);
    }

    public List<OperatorAssetResponse> getAssetsByOperator(String operatorEmployeeCode) {
        return assetRepository.findByCurrentOperator(operatorEmployeeCode);
    }

    /** One page of assets (common fields only), across every class, filtered in the database. */
    public PagedResponse<Asset> getAssetPage(String search, String assetClass, String status, int page, int size) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        List<Asset> content = assetRepository.findPage(search, assetClass, status, safeSize, safePage * safeSize);
        long total = assetRepository.countPage(search, assetClass, status);

        return new PagedResponse<>(content, safePage, safeSize, total);
    }

    /** @return true if an asset with this code was found and updated, false otherwise. */
    public boolean updateAssetStatus(String assetCode, String status) {
        return assetRepository.updateStatus(assetCode, status) > 0;
    }
}
