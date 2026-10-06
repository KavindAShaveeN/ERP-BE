package com.rr.erp.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Plant Equipment-class-specific asset fields. Field-for-field mirror of
 * src/config/assetTemplates/plantEquipment.ts. */
@Getter
@Setter
public class PlantEquipmentAssetDetail {

    private String assetCode;

    @NotBlank(message = "Plant type is required")
    private String plantType;

    @NotBlank(message = "Plant name is required")
    private String plantName;

    private String plantCodeAssetNumber;
    private String make;
    private String model;
    private String manufacturer;
    private String manufacturerSerialNumber;
    private String countryOfManufacture;
    private Integer yearOfManufacture;
    private String plantConfiguration;
    private BigDecimal productionCapacity;
    private String capacityUnit;
    private String mainPowerSource;
    private String fuelType;
    private String assignedProject;
    private LocalDate installationDate;
    private LocalDate commissioningDate;
    private LocalDate purchaseDate;
    private String supplier;
    private BigDecimal purchaseValue;
    private LocalDate warrantyExpiryDate;
    private String responsibleEmployeeDepartment;
    private BigDecimal gpsLatitude;
    private BigDecimal gpsLongitude;
    private String plantImages;
    private String supportingDocuments;
}
