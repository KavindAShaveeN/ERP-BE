package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Which columns to export (in this order) and which assets: an explicit list of asset
 * codes (the user's selection), or — when assetCodes is empty — every asset matching the
 * search/class/status filters (i.e. "export everything matching the current view"). */
@Getter
@Setter
public class AssetExportRequest {

    private List<String> columns;

    private List<String> assetCodes;

    private String search;

    private String assetClass;

    private String status;
}
