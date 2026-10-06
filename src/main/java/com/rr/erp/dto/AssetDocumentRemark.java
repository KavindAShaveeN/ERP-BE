package com.rr.erp.dto;

import java.time.LocalDateTime;

/** The free-text remark shown in an asset's Documents section. */
public record AssetDocumentRemark(String assetCode, String remark, LocalDateTime updatedAt) {
}
