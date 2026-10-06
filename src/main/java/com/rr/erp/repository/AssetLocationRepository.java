package com.rr.erp.repository;

import com.rr.erp.dto.AssetAtProjectResponse;
import com.rr.erp.entity.AssetLocation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AssetLocationRepository {

    private final JdbcTemplate jdbcTemplate;

    public AssetLocationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final org.springframework.jdbc.core.RowMapper<AssetLocation> ASSET_LOCATION_ROW_MAPPER =
            (rs, rowNum) -> {

                AssetLocation assetLocation = new AssetLocation();

                assetLocation.setAssetLocationId(
                        rs.getObject("assetLocationId", UUID.class)
                );

                assetLocation.setAssetCode(
                        rs.getString("assetCode")
                );

                assetLocation.setNewLocation(
                        rs.getString("newLocation")
                );

                assetLocation.setChangedBy(
                        rs.getString("changedBy")
                );

                assetLocation.setChangedDate(
                        rs.getObject("changedDate", java.time.LocalDate.class)
                );

                assetLocation.setReason(
                        rs.getString("reason")
                );
                assetLocation.setAssignedEmployee(
                        rs.getString("assignedEmployee")
                );

                assetLocation.setCreatedAt(
                        rs.getObject("createdAt", java.time.LocalDateTime.class)
                );

                assetLocation.setUpdatedAt(
                        rs.getObject("updatedAt", java.time.LocalDateTime.class)
                );

                assetLocation.setIsActive(
                        rs.getObject("isActive", Boolean.class)
                );

                assetLocation.setFromLocation(rs.getString("fromLocation"));
                assetLocation.setMovementType(rs.getString("movementType"));
                assetLocation.setSourceDocType(rs.getString("sourceDocType"));
                assetLocation.setSourceDocId(rs.getObject("sourceDocId", UUID.class));
                assetLocation.setTripId(rs.getObject("tripId", UUID.class));
                assetLocation.setTripCode(rs.getString("tripCode"));

                return assetLocation;
            };

    // POST
    public int createAssetLocation(AssetLocation assetLocation) {

        String sql = """
                INSERT INTO asset_location (
                    asset_location_id,
                    asset_code,
                    new_location,
                    changed_by,
                    changed_date,
                    reason,
                    assigned_employee,
                    created_at,
                    updated_at,
                    is_active,
                    from_location,
                    movement_type,
                    source_doc_type,
                    source_doc_id,
                    trip_id
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE, ?, ?, ?, ?, ?)
                """;

        UUID assetLocationId = UUID.randomUUID();
        assetLocation.setAssetLocationId(assetLocationId);

        return jdbcTemplate.update(
                sql,
                assetLocation.getAssetLocationId(),
                assetLocation.getAssetCode(),
                assetLocation.getNewLocation(),
                assetLocation.getChangedBy(),
                assetLocation.getChangedDate(),
                assetLocation.getReason(),
                assetLocation.getAssignedEmployee(),
                assetLocation.getFromLocation(),
                assetLocation.getMovementType(),
                assetLocation.getSourceDocType(),
                assetLocation.getSourceDocId(),
                assetLocation.getTripId()
        );
    }

    // Row-locks the asset so two documents moving the same asset at once are serialized —
    // the second one then sees the location the first one wrote. False if no such asset.
    public boolean lockAsset(String assetCode) {

        List<String> found = jdbcTemplate.queryForList(
                "SELECT asset_code FROM asset WHERE asset_code = ? FOR UPDATE",
                String.class,
                assetCode
        );

        return !found.isEmpty();
    }

    // GET BY ASSET CODE
    public List<AssetLocation> getAssetLocationsByAssetCode(String assetCode) {

        String sql = """
                SELECT
                    asset_location_id AS "assetLocationId",
                    asset_code AS "assetCode",
                    new_location AS "newLocation",
                    changed_by AS "changedBy",
                    changed_date AS "changedDate",
                    reason AS "reason",
                    assigned_employee AS "assignedEmployee",
                    created_at AS "createdAt",
                    updated_at AS "updatedAt",
                    is_active AS "isActive",
                    from_location AS "fromLocation",
                    movement_type AS "movementType",
                    source_doc_type AS "sourceDocType",
                    source_doc_id AS "sourceDocId",
                    trip_id AS "tripId",
                    (SELECT t.trip_code FROM transport_trip t WHERE t.trip_id = asset_location.trip_id) AS "tripCode"
                FROM asset_location
                WHERE asset_code = ?
                  AND is_active = TRUE
                ORDER BY changed_date DESC, created_at DESC
                """;

        return jdbcTemplate.query(
                sql,
                ASSET_LOCATION_ROW_MAPPER,
                assetCode
        );
    }

    // The most recently recorded active location for an asset.
    public Optional<AssetLocation> findLatestActiveLocation(String assetCode) {

        String sql = """
                SELECT
                    asset_location_id AS "assetLocationId",
                    asset_code AS "assetCode",
                    new_location AS "newLocation",
                    changed_by AS "changedBy",
                    changed_date AS "changedDate",
                    reason AS "reason",
                    assigned_employee AS "assignedEmployee",
                    created_at AS "createdAt",
                    updated_at AS "updatedAt",
                    is_active AS "isActive",
                    from_location AS "fromLocation",
                    movement_type AS "movementType",
                    source_doc_type AS "sourceDocType",
                    source_doc_id AS "sourceDocId",
                    trip_id AS "tripId",
                    (SELECT t.trip_code FROM transport_trip t WHERE t.trip_id = asset_location.trip_id) AS "tripCode"
                FROM asset_location
                WHERE asset_code = ?
                  AND is_active = TRUE
                ORDER BY changed_date DESC, created_at DESC
                LIMIT 1
                """;

        List<AssetLocation> results = jdbcTemplate.query(
                sql,
                ASSET_LOCATION_ROW_MAPPER,
                assetCode
        );

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    // SOFT DELETE
    public int deleteAssetLocation(UUID assetLocationId) {

        String sql = """
                UPDATE asset_location
                SET
                    is_active = FALSE,
                    updated_at = CURRENT_TIMESTAMP
                WHERE asset_location_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                assetLocationId
        );
    }

    // Every asset whose most recent active location entry is this project (so assets in transit -
    // latest entry is a DISPATCH or HUB_HOLD - or at another project, are excluded), with the item-master code a GIN line
    // needs for it. Disposed assets are left out.
    public List<AssetAtProjectResponse> findAssetsAtProject(String projectCode) {
        return findAssetsAtProject(projectCode, false);
    }

    // Stock views include disposed assets; issue forms retain the default exclusion.
    public List<AssetAtProjectResponse> findAssetsAtProject(String projectCode, boolean includeDisposed) {

        String sql = """
                SELECT
                    a.asset_code AS "assetCode",
                    a.description AS "description",
                    a.serial_number AS "serialNumber",
                    ic.item_code_code AS "itemCode",
                    ic.item_code_name AS "itemName",
                    loc.assigned_employee AS "assignedEmployee"
                FROM asset a
                JOIN LATERAL (
                    SELECT l.new_location, l.assigned_employee, l.movement_type
                    FROM asset_location l
                    WHERE l.asset_code = a.asset_code
                      AND l.is_active = TRUE
                    ORDER BY l.changed_date DESC, l.created_at DESC
                    LIMIT 1
                ) loc ON TRUE
                LEFT JOIN asset_code ac ON ac.asset_code_id = a.asset_code_id
                LEFT JOIN item_code ic ON ic.item_code_id = ac.item_code_id
                WHERE UPPER(loc.new_location) = UPPER(?)
                  AND COALESCE(loc.movement_type, '') NOT IN ('DISPATCH', 'HUB_HOLD')
                  AND (? OR COALESCE(UPPER(a.status), '') <> 'DISPOSED')
                ORDER BY a.asset_code
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new AssetAtProjectResponse(
                        rs.getString("assetCode"),
                        rs.getString("description"),
                        rs.getString("serialNumber"),
                        rs.getString("itemCode"),
                        rs.getString("itemName"),
                        rs.getString("assignedEmployee")
                ),
                projectCode,
                includeDisposed
        );
    }
}
