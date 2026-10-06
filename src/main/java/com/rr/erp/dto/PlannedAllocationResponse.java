package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/** One MR line's currently reserved quantity — used to pre-fill a GIN from a confirmed plan. */
@Getter
@Setter
public class PlannedAllocationResponse {

    private UUID mrItemId;
    private String itemCode;
    private String description;
    private String size;
    private Integer uomId;
    private String uomName;
    private BigDecimal allocatedQty;
}
