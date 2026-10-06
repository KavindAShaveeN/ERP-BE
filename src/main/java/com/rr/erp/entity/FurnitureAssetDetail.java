package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Furniture-class-specific asset fields. Field-for-field mirror of
 * src/config/assetTemplates/furniture.ts. */
@Getter
@Setter
public class FurnitureAssetDetail {

    private String assetCode;

    private String furnitureType;
    private String make;
    private String model;
    private String material;
    private String colour;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private String supplier;
    private String invoiceNumber;
    private LocalDate purchaseDate;
    private String warrantyPeriod;
}
