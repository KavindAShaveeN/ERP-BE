package com.rr.erp.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Vehicle-class-specific asset fields. Field-for-field mirror of the Vehicle
 * template tabs in the frontend (src/config/assetTemplates/vehicle.ts). */
@Getter
@Setter
public class VehicleAssetDetail {

    private String assetCode;

    // ---- Basic Asset Details ----
    @NotBlank(message = "Vehicle class/type is required")
    private String vehicleClass;

    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    @NotNull(message = "Year of manufacture is required")
    private Integer yearOfManufacture;

    private String colour;
    private BigDecimal weightKg;

    // ---- Manufacturer Details ----
    @NotBlank(message = "Make/Brand is required")
    private String make;

    @NotBlank(message = "Model is required")
    private String model;

    private String countryOfManufacture;
    private String manufacturerSerialNumber;

    @NotBlank(message = "Chassis number/VIN is required")
    private String chassisNumber;

    private String bodyType;
    private String seatingLoadingCapacity;

    // ---- Engine Details ----
    @NotBlank(message = "Engine number/serial number is required")
    private String engineNumber;

    private String engineMake;
    private String engineModel;
    private Integer engineCapacityCc;

    @NotBlank(message = "Fuel type is required")
    private String fuelType;

    private Integer numberOfCylinders;
    private BigDecimal fuelTankCapacityL;
    private BigDecimal averageFuelConsumption;
    private String fuelConsumptionUnit;

    // ---- Power Details ----
    private BigDecimal horsepower;
    private BigDecimal powerKw;
    private BigDecimal voltage;
    private BigDecimal currentAmps;
    private Integer rpm;

    // ---- Tyre Details ----
    private Integer numberOfTyres;
    private String frontTyreSize;
    private String rearTyreSize;
    private String spareTyreSize;
    private String tyreMakeBrand;

    // ---- Battery Details ----
    private String batteryMakeBrand;
    private String batteryModel;
    private String batterySerialNumber;
    private BigDecimal batteryVoltage;
    private BigDecimal batteryCapacityAh;
    private Integer numberOfBatteries;

    // ---- Purchase and Ownership Details ----
    private String supplier;
    private String purchaseOrderNumber;
    private String invoiceReceiptNumber;
    private LocalDate purchaseDate;
    private BigDecimal purchaseValue;
    private String currency;
    private LocalDate receivedDate;
    private LocalDate warrantyExpiryDate;

    // ---- Registration and Insurance Details ----
    private LocalDate registrationDate;
    private LocalDate registrationExpiryDate;
    private String registrationDocument;
    private String insuranceCompany;
    private String insurancePolicyNumber;
    private LocalDate insuranceStartDate;
    private LocalDate insuranceExpiryDate;
    private String revenueLicenceNumber;
    private LocalDate revenueLicenceStartDate;
    private LocalDate revenueLicenceExpiryDate;
    private LocalDate emissionTestStartDate;
    private LocalDate emissionTestExpiryDate;
    private String insuranceLicenceDocuments;

    // ---- Assignment and Location ----
    private LocalDate assignedDate;
    private String owningDepartment;
    private String costCentreAccountCode;
    private String accountDescription;

    // ---- Documents and Images ----
    private String vehiclePhotograph;
    private String registrationCertificate;
    private String insuranceDocument;
    private String revenueLicenceDocument;
    private String purchaseInvoiceDocument;
    private String warrantyDocument;
    private String otherSupportingDocuments;
}
