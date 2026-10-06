package com.rr.erp.service;

import com.rr.erp.entity.AssetPackItem;
import com.rr.erp.entity.PackCarrier;
import com.rr.erp.entity.PackSelection;
import com.rr.erp.repository.AssetPackRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class AssetPackService {

    public static final String DOC_GIN = "GIN";
    public static final String DOC_GRN = "GRN";
    public static final String DOC_STOCK_RETURN = "STOCK_RETURN";

    private final AssetPackRepository repository;

    public AssetPackService(AssetPackRepository repository) {
        this.repository = repository;
    }

    public List<AssetPackItem> getByAsset(String assetCode) {
        return repository.findByAssetCode(assetCode);
    }

    @Transactional
    public List<AssetPackItem> add(String assetCode, AssetPackItem item) {
        requireAsset(assetCode);
        validate(item);
        item.setAssetCode(assetCode);
        repository.insert(item);
        return repository.findByAssetCode(assetCode);
    }

    @Transactional
    public List<AssetPackItem> update(String assetCode, Long id, AssetPackItem item) {
        requireAsset(assetCode);
        validate(item);
        if (repository.update(id, assetCode, item) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pack item not found: " + id);
        }
        return repository.findByAssetCode(assetCode);
    }

    @Transactional
    public List<AssetPackItem> delete(String assetCode, Long id) {
        if (repository.delete(id, assetCode) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pack item not found: " + id);
        }
        return repository.findByAssetCode(assetCode);
    }

    /** Replaces the ticked/unticked pack selections recorded for every asset line of a document. */
    @Transactional
    public void saveSelections(String docType, UUID docId, List<? extends PackCarrier> items) {

        repository.deleteSelections(docType, docId);

        if (items == null) {
            return;
        }
        for (PackCarrier item : items) {
            String assetCode = item.getAssetCode();
            if (assetCode == null || assetCode.isBlank() || item.getPackItems() == null) {
                continue;
            }
            for (PackSelection selection : item.getPackItems()) {
                if (selection.getPackItemId() == null) {
                    continue;
                }
                repository.insertSelection(docType, docId, assetCode.trim(),
                        selection.getPackItemId(), !Boolean.FALSE.equals(selection.getIncluded()));
            }
        }
    }

    /** Called when the asset actually moves: ticked packs go with it, unticked ones stay behind. */
    @Transactional
    public void applySelections(String docType, UUID docId, String assetCode) {
        for (PackSelection selection : repository.findSelections(docType, docId, assetCode)) {
            repository.setWithAsset(selection.getPackItemId(), Boolean.TRUE.equals(selection.getIncluded()));
        }
    }

    private void requireAsset(String assetCode) {
        if (!repository.assetExists(assetCode)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found: " + assetCode);
        }
    }

    private void validate(AssetPackItem item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pack item name is required.");
        }
        item.setName(item.getName().trim());
        if (item.getQuantity() != null && item.getQuantity() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pack item quantity must be at least 1.");
        }
    }
}
