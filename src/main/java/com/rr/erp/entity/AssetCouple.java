package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

/** One coupling, seen from a given asset: the fields describe the OTHER asset in the pair. */
@Getter
@Setter
public class AssetCouple {

    private Long coupleId;
    private String coupledAssetCode;
    private String assetClass;
    private String description;
    private String status;
}
