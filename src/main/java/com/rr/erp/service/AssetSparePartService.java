package com.rr.erp.service;

import com.rr.erp.entity.AssetSparePart;
import com.rr.erp.repository.AssetSparePartRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AssetSparePartService {

    private final AssetSparePartRepository repository;

    public AssetSparePartService(AssetSparePartRepository repository) {
        this.repository = repository;
    }

    public List<AssetSparePart> getByAsset(String assetCode) {
        return repository.findByAssetCode(assetCode);
    }

    public List<AssetSparePart> getByItemCode(Long itemCodeId) {
        return repository.findByItemCodeId(itemCodeId);
    }

    @Transactional
    public List<AssetSparePart> add(String assetCode, AssetSparePart part) {
        requireAsset(assetCode);
        part.setAssetCode(assetCode);
        validate(part);
        try {
            repository.insert(part);
        } catch (DuplicateKeyException e) {
            throw duplicate();
        }
        return repository.findByAssetCode(assetCode);
    }

    @Transactional
    public List<AssetSparePart> update(String assetCode, Long id, AssetSparePart part) {
        requireAsset(assetCode);
        validate(part);
        try {
            if (repository.update(id, assetCode, part) == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Spare part not found");
            }
        } catch (DuplicateKeyException e) {
            throw duplicate();
        }
        return repository.findByAssetCode(assetCode);
    }

    @Transactional
    public List<AssetSparePart> delete(String assetCode, Long id) {
        if (repository.delete(id, assetCode) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Spare part not found");
        }
        return repository.findByAssetCode(assetCode);
    }

    // Replaces the whole list — used right after registering an asset.
    @Transactional
    public List<AssetSparePart> replaceAll(String assetCode, List<AssetSparePart> parts) {
        requireAsset(assetCode);
        repository.deleteAllForAsset(assetCode);
        for (AssetSparePart part : parts) {
            part.setAssetCode(assetCode);
            validate(part);
            try {
                repository.insert(part);
            } catch (DuplicateKeyException e) {
                throw duplicate();
            }
        }
        return repository.findByAssetCode(assetCode);
    }

    private void requireAsset(String assetCode) {
        if (!repository.assetExists(assetCode)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset " + assetCode + " not found");
        }
    }

    private void validate(AssetSparePart part) {
        if (part.getItemCodeId() == null || !repository.itemCodeExists(part.getItemCodeId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown item code");
        }
        if (part.getPartRole() == null || part.getPartRole().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description is required");
        }
        part.setPartRole(part.getPartRole().trim());
        part.setPartNumber(blankToNull(part.getPartNumber()));
        part.setSerialNumber(blankToNull(part.getSerialNumber()));
        if (part.getQuantityPerUnit() == null) {
            part.setQuantityPerUnit(BigDecimal.ONE);
        }
        if (part.getQuantityPerUnit().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ResponseStatusException duplicate() {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "This item is already listed with the same description and serial number on this asset");
    }
}
