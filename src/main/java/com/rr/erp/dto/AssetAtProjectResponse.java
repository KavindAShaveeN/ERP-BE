package com.rr.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * A registered asset whose latest location entry is a given project — i.e. an asset that can be
 * picked on a GIN issued from that project. itemCode/itemName are the item-master code the GIN
 * line must carry for it (asset -> asset_code -> item_code).
 */
@Getter
@Setter
@AllArgsConstructor
public class AssetAtProjectResponse {

    private String assetCode;
    private String description;
    private String serialNumber;
    private String itemCode;
    private String itemName;
    private String assignedEmployee;
}
