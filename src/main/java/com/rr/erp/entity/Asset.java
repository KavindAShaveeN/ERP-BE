package com.rr.erp.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** A single physical asset. Common fields live here; class-specific fields (e.g.
 * {@link VehicleAssetDetail}) are nested, one at most populated per asset class. */
@Getter
@Setter
public class Asset {

    private String assetCode;

    @NotNull(message = "Asset code ID is required")
    private Long assetCodeId;

    private String assetClass;

    private String description;

    private String serialNumber;

    private String projectOrDepartment;

    private String ownershipType;

    @NotBlank(message = "Asset status is required")
    private String status;

    private String condition;

    private String remarks;

    private String documentName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Valid
    private VehicleAssetDetail vehicleDetail;

    @Valid
    private PlantEquipmentAssetDetail plantEquipmentDetail;

    @Valid
    private MachineryAssetDetail machineryDetail;

    @Valid
    private PowerToolAssetDetail powerToolDetail;

    @Valid
    private ElectricalEquipmentAssetDetail electricalEquipmentDetail;

    @Valid
    private ItEquipmentAssetDetail itEquipmentDetail;

    @Valid
    private FurnitureAssetDetail furnitureDetail;
}
