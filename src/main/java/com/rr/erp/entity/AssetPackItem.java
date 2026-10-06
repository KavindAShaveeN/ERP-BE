package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetPackItem {

    private Long packItemId;
    private String assetCode;
    private String name;
    private String description;
    private String serialNumber;
    private Integer quantity;
    private Boolean isWithAsset;
    private String remarks;
}
