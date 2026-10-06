package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

// One spare part of an asset. The itemCode/itemName/uomName/currentStock/unitPrice fields are
// read-only, filled from the item master on GET; only itemCodeId, partRole, quantityPerUnit and
// remarks are taken from a request.
@Getter
@Setter
public class AssetSparePart {

    private Long assetSparePartId;
    private String assetCode;
    private Long itemCodeId;
    private String partRole;
    private String partNumber;
    private String serialNumber;
    private BigDecimal quantityPerUnit;
    private String remarks;

    private String itemCode;
    private String itemName;
    private String uomName;
    private BigDecimal currentStock;
    private BigDecimal unitPrice;
}
