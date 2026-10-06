package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class QuotationRequestItem {

    private UUID quotationRequestItemId;
    private UUID quotationRequestId;
    private UUID mrItemId;
    private String itemCode;
    private String description;
    private Integer uomId;
    private Integer quantity;
}
