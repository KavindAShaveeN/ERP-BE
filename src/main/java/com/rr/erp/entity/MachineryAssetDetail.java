package com.rr.erp.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Machinery-class-specific asset fields. Field-for-field mirror of
 * src/config/assetTemplates/machinery.ts. */
@Getter
@Setter
public class MachineryAssetDetail {

    private String assetCode;

    // ---- Identification and Manufacturer Details ----
    @NotBlank(message = "Machinery type is required")
    private String machineryType;

    private String make;
    private String model;
    private String countryOfManufacture;
    private Integer yearOfManufacture;
    private String manufacturerSerialNumber;
    private String chassisFrameNumber;
    private String registrationNumber;
    private String colour;

    // ---- Engine Details ----
    private String engineNumber;
    private String engineMake;
    private String engineModel;
    private String fuelType;
    private Integer engineCapacityCc;
    private Integer numberOfCylinders;
    private BigDecimal enginePowerHp;
    private BigDecimal enginePowerKw;
    private BigDecimal ratedRpm;
    private BigDecimal fuelTankCapacityL;
    private BigDecimal averageFuelConsumption;
    private String fuelConsumptionUnit;

    // ---- Operational Details ----
    private String meterType;
    private String initialMeterReading;
    private String operatingCapacity;
    private String operatingCapacityUnit;
    private BigDecimal machineWeightKg;
    private String loadLiftCapacity;
    private String bucketBladeCapacity;

    // ---- Tyre or Track Details ----
    private String runningSystem;
    private Integer numberOfTyres;
    private String frontTyreSize;
    private String rearTyreSize;
    private String trackSizeWidth;
    private String tyreTrackBrand;

    // ---- Battery Details ----
    private String batteryBrand;
    private String batteryModel;
    private BigDecimal batteryVoltage;
    private BigDecimal batteryCapacityAh;
    private Integer numberOfBatteries;
}
