package com.rr.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** An asset currently operated by a given employee — just enough to identify it. */
@Getter
@Setter
@AllArgsConstructor
public class OperatorAssetResponse {

    private String assetCode;
    private Long assetCodeId;
    private String assetClass;
}
