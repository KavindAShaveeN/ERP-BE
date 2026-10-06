package com.rr.erp.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Power Tool-class-specific asset fields. Field-for-field mirror of
 * src/config/assetTemplates/powerTool.ts. Note the "current" -> currentAmps
 * rename, same reason as VehicleAssetDetail (avoids the SQL keyword collision). */
@Getter
@Setter
public class PowerToolAssetDetail {

    private String assetCode;

    // ---- Tool Details ----
    @NotBlank(message = "Tool type is required")
    private String toolType;

    private String make;
    private String model;
    private String manufacturerSerialNumber;
    private String countryOfManufacture;
    private Integer yearOfManufacture;

    @NotBlank(message = "Power source is required")
    private String powerSource;

    private String intendedUse;

    // ---- Technical Specifications ----
    private BigDecimal ratedPower;
    private BigDecimal voltage;
    private BigDecimal currentAmps;
    private BigDecimal frequency;
    private BigDecimal speedRpm;
    private String capacityToolSize;
    private String capacityUnit;
    private String chuckDiscBladeSize;
    private BigDecimal weightKg;

    // ---- Battery Details ----
    private String batteryType;
    private BigDecimal batteryVoltage;
    private BigDecimal batteryCapacityAh;
    private Integer numberOfBatteries;
    private String chargerModelSerialNumber;

    // ---- Fuel Details ----
    private String fuelType;
    private Integer engineCapacityCc;
    private BigDecimal fuelTankCapacityL;
    private String engineNumber;

    // ---- Safety and Accessories ----
    private String safetyClassProtectionRating;
    private String includedAccessories;
    private String carryingCaseNumber;
}
