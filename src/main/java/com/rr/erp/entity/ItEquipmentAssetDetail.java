package com.rr.erp.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** IT Equipment-class-specific asset fields. Field-for-field mirror of
 * src/config/assetTemplates/itEquipment.ts. */
@Getter
@Setter
public class ItEquipmentAssetDetail {

    private String assetCode;

    @NotBlank(message = "Equipment type is required")
    private String equipmentType;

    private String make;
    private String model;
    private String manufacturerSerialNumber;
    private String assetTag;
    private String deviceNameHostname;
    private Integer yearOfManufacture;
    private String colour;
    private String processorMainSpecification;
    private String ramCapacity;
    private String storageCapacity;
    private String operatingSystem;
    private String screenSize;
    private String networkMacAddress;
    private String ipAddress;
    private String powerRatingCapacity;
    private BigDecimal voltage;
    private String includedAccessories;
    private String assignedDepartment;
    private String assignedProjectLocation;
    private LocalDate purchaseDate;
    private LocalDate warrantyExpiryDate;
    private String supplier;
    private BigDecimal purchaseValue;
}
