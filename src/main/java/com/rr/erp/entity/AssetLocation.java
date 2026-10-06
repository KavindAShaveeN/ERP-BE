package com.rr.erp.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class AssetLocation {

    private UUID assetLocationId;
    private String assetCode;
    private String newLocation;
    private String changedBy;
    private LocalDate changedDate;
    private String assignedEmployee;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;

    // Why/what caused this change — set by the GIN/GRN/issue flows, null for manual entries
    // made from AssetPage (see asset_movement_tracking.sql).
    private String fromLocation;
    private String movementType;
    private String sourceDocType;
    private UUID sourceDocId;

    // The transport trip that caused this entry (vehicle stop entries, HUB_HOLD entries); tripCode is
    // read-only, joined from transport_trip. Null for everything else (see transport_plan_schema.sql).
    private UUID tripId;
    private String tripCode;
}