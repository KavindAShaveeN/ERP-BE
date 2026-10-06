package com.rr.erp.service;

import com.rr.erp.entity.AssetOperatorHistory;
import com.rr.erp.repository.AssetOperatorHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AssetOperatorHistoryService {

    private final AssetOperatorHistoryRepository assetOperatorHistoryRepository;

    public AssetOperatorHistoryService(AssetOperatorHistoryRepository assetOperatorHistoryRepository) {
        this.assetOperatorHistoryRepository = assetOperatorHistoryRepository;
    }

    public int createAssetOperatorHistory(AssetOperatorHistory assetOperatorHistory) {
        return assetOperatorHistoryRepository.createAssetOperatorHistory(assetOperatorHistory);
    }

    public List<AssetOperatorHistory> getAssetOperatorHistoryByAssetCode(String assetCode) {
        return assetOperatorHistoryRepository.getAssetOperatorHistoryByAssetCode(assetCode);
    }

    public Optional<AssetOperatorHistory> findLatestActiveOperator(String assetCode) {
        return assetOperatorHistoryRepository.findLatestActiveOperator(assetCode);
    }

    public List<String> findAssetCodesForOperator(String operatorEmployeeCode) {
        return assetOperatorHistoryRepository.findAssetCodesForOperator(operatorEmployeeCode);
    }

    public int updateAssetOperatorHistoryDetails(UUID assetOperatorHistoryId, String remarks, String contactNumber) {
        return assetOperatorHistoryRepository.updateAssetOperatorHistoryDetails(assetOperatorHistoryId, remarks, contactNumber);
    }

    public int deleteAssetOperatorHistory(UUID assetOperatorHistoryId) {
        return assetOperatorHistoryRepository.deleteAssetOperatorHistory(assetOperatorHistoryId);
    }
}
