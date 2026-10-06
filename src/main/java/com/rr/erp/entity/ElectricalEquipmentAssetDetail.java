package com.rr.erp.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Electrical Equipment-class-specific asset fields. Field-for-field mirror of
 * src/config/assetTemplates/electricalEquipment.ts. */
@Getter
@Setter
public class ElectricalEquipmentAssetDetail {

    private String assetCode;

    // ---- Identification Details ----
    @NotBlank(message = "Equipment type is required")
    private String equipmentType;

    private String make;
    private String model;
    private String manufacturerSerialNumber;
    private String countryOfManufacture;
    private Integer yearOfManufacture;
    private String installationType;

    // ---- Electrical Specifications ----
    private BigDecimal ratedPower;
    private BigDecimal horsepower;
    private BigDecimal ratedVoltage;
    private BigDecimal ratedCurrent;
    private BigDecimal frequency;
    private String numberOfPhases;
    private BigDecimal speedRpm;
    private BigDecimal powerFactor;
    private String efficiencyClass;
    private String insulationClass;
    private String protectionRating;
    private String connectionType;
    private String dutyType;

    // ---- Equipment-Specific Specifications ----
    private String equipmentCapacity;
    private String capacityUnit;
    private String inputRating;
    private String outputRating;
    private String pressureRating;
    private String flowRate;
    private String coolingMethod;
    private BigDecimal transformerRatingKva;
    private BigDecimal generatorRatingKva;
    private BigDecimal pumpHeadM;

    // ---- Installation Details ----
    private LocalDate installationDate;
    private String installationLocation;
    private String panelCircuitReference;
    private String connectedLoad;
    private String responsibleDepartment;
}
