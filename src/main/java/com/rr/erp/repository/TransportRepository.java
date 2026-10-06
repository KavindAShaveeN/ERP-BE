package com.rr.erp.repository;

import com.rr.erp.dto.TransportDtos.AvailableGin;
import com.rr.erp.dto.TransportDtos.CustodyEvent;
import com.rr.erp.dto.TransportDtos.GinTripInfo;
import com.rr.erp.dto.TransportDtos.HeldGin;
import com.rr.erp.dto.TransportDtos.TripSummary;
import com.rr.erp.dto.TransportDtos.VehicleCandidate;
import com.rr.erp.dto.TransportDtos.VehicleView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** JDBC access for the transport plan (see db/transport_plan_schema.sql). */
@Repository
public class TransportRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ---------------------------------------------------------------- rows used by the service

    public record TripRow(UUID tripId, String tripCode, String vehicleAssetCode, String driverName,
                          String originProjectCode, String tripType, String status,
                          LocalDateTime plannedDeparture, LocalDateTime actualDeparture, String remarks,
                          String createdBy, String closedBy, LocalDateTime closedAt) {}

    public record StopRow(UUID stopId, UUID tripId, int seq, String projectCode, String stopType, String status,
                          LocalDateTime eta, LocalDateTime arrivedAt, LocalDateTime departedAt) {}

    public record AllocationRow(UUID allocationId, UUID tripId, UUID ginId, String ginCode,
                                String issuedProjectCode, String destinationProjectCode,
                                String deliveryMode, UUID dropStopId, String dropProjectCode,
                                String custodyStatus, boolean active, UUID previousAllocationId,
                                boolean authorized, boolean gateVerified, int itemCount,
                                String grnCode, boolean grnApproved, String nextTripCode) {}

    /** A stop of a running trip at some project, with the trip fields the project needs to see. */
    public record StopTaskRow(StopRow stop, String tripCode, String vehicleNo, String driverName,
                              String originProjectCode) {}

    /** A GIN on a vehicle that is heading to a hub project, for that hub's gate. */
    public record HubArrivalRow(UUID ginId, String ginCode, LocalDateTime issuedDate, String issuedProjectCode,
                                String destinationProjectCode, int itemCount, String tripCode, String vehicleNo) {}

    /** A stop that a vehicle has reached at some project, with the trip fields shown in a visit history. */
    public record VisitRow(UUID stopId, UUID tripId, String tripCode, String vehicleNo, String driverName,
                           String originProjectCode, String stopType, String stopStatus,
                           LocalDateTime arrivedAt, LocalDateTime departedAt) {}

    /** The event that took a GIN out of a hub. */
    public record ReleaseEventRow(String eventType, String remarks, LocalDateTime eventTime) {}

    public record IncomingRow(UUID ginId, String ginCode, String issuedProjectCode, int itemCount,
                              String deliveryMode, String custodyStatus, UUID tripId, String tripCode,
                              String tripStatus, String vehicleAssetCode, String vehicleNo, String driverName,
                              String dropProjectCode, LocalDateTime dropEta, Integer dropSeq,
                              String legNote, LocalDateTime legTime) {}

    // ---------------------------------------------------------------- access / lookups

    /** The project type name of a project (e.g. "Head Quarters"), empty if the project has none. */
    public Optional<String> findProjectTypeName(String projectCode) {
        List<String> found = jdbcTemplate.queryForList("""
                SELECT pt.project_type_name
                FROM project p
                JOIN project_type pt ON pt.project_type_id = p.project_type_id
                WHERE p.project_code = ?
                """, String.class, projectCode);
        return found.isEmpty() ? Optional.empty() : Optional.ofNullable(found.get(0));
    }

    public boolean projectExists(String projectCode) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM project WHERE project_code = ?", Integer.class, projectCode);
        return count != null && count > 0;
    }

    public boolean employeeExists(String employeeCode) {
        if (employeeCode == null || employeeCode.isBlank()) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM employee WHERE employee_code = ?", Integer.class, employeeCode);
        return count != null && count > 0;
    }

    /** project_code -> project name, for sentences shown to receivers. */
    public Map<String, String> findProjectNames() {
        Map<String, String> names = new HashMap<>();
        jdbcTemplate.query("SELECT project_code, projectname FROM project",
                rs -> {
                    names.put(rs.getString("project_code"), rs.getString("projectname"));
                });
        return names;
    }

    // ---------------------------------------------------------------- vehicle pool

    public List<VehicleCandidate> findVehicleCandidates() {
        return jdbcTemplate.query("""
                SELECT a.asset_code, a.description, v.registration_number, v.make, v.model
                FROM asset a
                LEFT JOIN vehicle_asset_detail v ON v.asset_code = a.asset_code
                WHERE a.asset_class = 'Vehicle'
                  AND COALESCE(UPPER(a.status), '') <> 'DISPOSED'
                  AND NOT EXISTS (
                      SELECT 1 FROM transport_vehicle tv
                      WHERE tv.asset_code = a.asset_code AND tv.is_active = TRUE)
                ORDER BY a.asset_code
                """,
                (rs, n) -> new VehicleCandidate(
                        rs.getString("asset_code"), rs.getString("description"),
                        rs.getString("registration_number"), rs.getString("make"), rs.getString("model")));
    }

    public boolean isVehicleAsset(String assetCode) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM asset WHERE asset_code = ? AND asset_class = 'Vehicle'",
                Integer.class, assetCode);
        return count != null && count > 0;
    }

    /** Pool vehicles with a derived status; pass an asset code for just that vehicle, null for all. */
    public List<VehicleView> findVehicles(String assetCode) {

        String sql = """
                SELECT tv.asset_code, a.description, v.registration_number, v.make, v.model, tv.default_driver,
                       a.status AS asset_status,
                       (SELECT COUNT(*) FROM job_card j
                         WHERE j.asset_code = tv.asset_code
                           AND COALESCE(j.is_finished, FALSE) = FALSE
                           AND COALESCE(j.job_status_type_id, 0) NOT IN (6, 7)) AS open_jobs,
                       loc.new_location, loc.movement_type,
                       t.trip_id, t.trip_code, t.status AS trip_status, t.driver_name,
                       stp.project_code AS stop_project, stp.status AS stop_status
                FROM transport_vehicle tv
                JOIN asset a ON a.asset_code = tv.asset_code
                LEFT JOIN vehicle_asset_detail v ON v.asset_code = tv.asset_code
                LEFT JOIN LATERAL (
                    SELECT l.new_location, l.movement_type
                    FROM asset_location l
                    WHERE l.asset_code = tv.asset_code AND l.is_active = TRUE
                    ORDER BY l.changed_date DESC, l.created_at DESC
                    LIMIT 1
                ) loc ON TRUE
                LEFT JOIN transport_trip t
                       ON t.vehicle_asset_code = tv.asset_code
                      AND t.status IN ('PLANNED', 'IN_TRANSIT', 'RETURNING')
                LEFT JOIN LATERAL (
                    SELECT s.project_code, s.status
                    FROM transport_trip_stop s
                    WHERE s.trip_id = t.trip_id AND s.status IN ('PENDING', 'ARRIVED')
                    ORDER BY s.seq
                    LIMIT 1
                ) stp ON TRUE
                WHERE tv.is_active = TRUE
                  AND (CAST(? AS VARCHAR) IS NULL OR tv.asset_code = ?)
                ORDER BY tv.asset_code
                """;

        return jdbcTemplate.query(sql, (rs, n) -> {

            String movementType = rs.getString("movement_type");
            boolean onTheRoad = "DISPATCH".equals(movementType) || "HUB_HOLD".equals(movementType);
            String location = rs.getString("new_location");
            String tripStatus = rs.getString("trip_status");
            String assetStatus = rs.getString("asset_status");
            int openJobs = rs.getInt("open_jobs");

            String status;
            String detail = null;
            if (tripStatus != null) {
                status = switch (tripStatus) {
                    case "PLANNED" -> "ALLOCATED";
                    case "RETURNING" -> "RETURNING";
                    default -> "IN_TRANSIT";
                };
            } else if (assetStatus != null && !"ACTIVE".equalsIgnoreCase(assetStatus.trim())) {
                status = "MAINTENANCE";
                detail = "Asset status: " + assetStatus;
            } else if (openJobs > 0) {
                status = "MAINTENANCE";
                detail = openJobs + " open job card" + (openJobs > 1 ? "s" : "") + " in the workshop";
            } else {
                status = "AVAILABLE";
            }

            return new VehicleView(
                    rs.getString("asset_code"), rs.getString("description"),
                    rs.getString("registration_number"), rs.getString("make"), rs.getString("model"),
                    rs.getString("default_driver"), status, detail,
                    onTheRoad ? null : location, onTheRoad ? location : null,
                    rs.getObject("trip_id", UUID.class), rs.getString("trip_code"), rs.getString("driver_name"),
                    rs.getString("stop_project"), rs.getString("stop_status"));
        }, assetCode, assetCode);
    }

    public void upsertVehicle(String assetCode, String defaultDriver, String addedBy) {
        jdbcTemplate.update("""
                INSERT INTO transport_vehicle (asset_code, default_driver, is_active, added_by)
                VALUES (?, ?, TRUE, ?)
                ON CONFLICT (asset_code) DO UPDATE
                   SET is_active = TRUE,
                       default_driver = EXCLUDED.default_driver,
                       updated_at = CURRENT_TIMESTAMP
                """, assetCode, defaultDriver, addedBy);
    }

    public int updateVehicleDriver(String assetCode, String defaultDriver) {
        return jdbcTemplate.update("""
                UPDATE transport_vehicle
                SET default_driver = ?, updated_at = CURRENT_TIMESTAMP
                WHERE asset_code = ? AND is_active = TRUE
                """, defaultDriver, assetCode);
    }

    public int deactivateVehicle(String assetCode) {
        return jdbcTemplate.update("""
                UPDATE transport_vehicle
                SET is_active = FALSE, updated_at = CURRENT_TIMESTAMP
                WHERE asset_code = ?
                """, assetCode);
    }

    public boolean hasOpenTrip(String assetCode) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM transport_trip
                WHERE vehicle_asset_code = ? AND status IN ('PLANNED', 'IN_TRANSIT', 'RETURNING')
                """, Integer.class, assetCode);
        return count != null && count > 0;
    }

    // ---------------------------------------------------------------- GINs available for a trip

    private static final String AVAILABLE_GIN_SQL = """
            SELECT x.* FROM (
                SELECT g.gin_id, g.gin_code, g.issued_date, g.issued_project_code, g.received_project_code,
                       (SELECT COUNT(*) FROM gin_item i WHERE i.gin_id = g.gin_id) AS item_count,
                       (SELECT COUNT(*) FROM gin_item i
                         WHERE i.gin_id = g.gin_id AND COALESCE(i.asset_code, '') <> '') AS asset_count,
                       COALESCE(g.is_authorized, FALSE) AS is_authorized,
                       COALESCE(g.is_gate_verified, FALSE) AS is_gate_verified,
                       'NEW' AS kind,
                       CAST(NULL AS VARCHAR) AS held_from_trip_code
                FROM gin g
                WHERE g.issued_project_code = ?
                  AND COALESCE(g.received_project_code, '') <> ''
                  AND g.received_project_code <> g.issued_project_code
                  AND COALESCE(g.is_gate_verified, FALSE) = FALSE
                  AND NOT EXISTS (SELECT 1 FROM transport_trip_gin a WHERE a.gin_id = g.gin_id AND a.is_active)
                  AND NOT EXISTS (SELECT 1 FROM grn WHERE grn.gin_id = g.gin_id)

                UNION ALL

                SELECT g.gin_id, g.gin_code, g.issued_date, g.issued_project_code, g.received_project_code,
                       (SELECT COUNT(*) FROM gin_item i WHERE i.gin_id = g.gin_id),
                       (SELECT COUNT(*) FROM gin_item i
                         WHERE i.gin_id = g.gin_id AND COALESCE(i.asset_code, '') <> ''),
                       COALESCE(g.is_authorized, FALSE), COALESCE(g.is_gate_verified, FALSE),
                       'AT_HUB', t.trip_code
                FROM transport_trip_gin a
                JOIN gin g ON g.gin_id = a.gin_id
                JOIN transport_trip t ON t.trip_id = a.trip_id
                JOIN transport_trip_stop s ON s.stop_id = a.drop_stop_id
                WHERE a.is_active = TRUE
                  AND a.custody_status = 'AT_HUB'
                  AND s.project_code = ?
                  AND g.received_project_code <> ?

                UNION ALL

                SELECT g.gin_id, g.gin_code, g.issued_date, g.issued_project_code, g.received_project_code,
                       (SELECT COUNT(*) FROM gin_item i WHERE i.gin_id = g.gin_id),
                       (SELECT COUNT(*) FROM gin_item i
                         WHERE i.gin_id = g.gin_id AND COALESCE(i.asset_code, '') <> ''),
                       COALESCE(g.is_authorized, FALSE), COALESCE(g.is_gate_verified, FALSE),
                       'RETURNED', r.trip_code
                FROM gin g
                JOIN LATERAL (
                    SELECT a.returned_at_project, t.trip_code
                    FROM transport_trip_gin a
                    JOIN transport_trip t ON t.trip_id = a.trip_id
                    WHERE a.gin_id = g.gin_id AND a.custody_status = 'RETURNED_TO_STORE'
                    ORDER BY a.updated_at DESC
                    LIMIT 1
                ) r ON TRUE
                WHERE r.returned_at_project = ?
                  AND COALESCE(g.received_project_code, '') <> ''
                  AND g.received_project_code <> ?
                  AND COALESCE(g.is_arrival_gate_verified, FALSE) = FALSE
                  AND NOT EXISTS (SELECT 1 FROM transport_trip_gin a WHERE a.gin_id = g.gin_id AND a.is_active)
                  AND NOT EXISTS (SELECT 1 FROM grn WHERE grn.gin_id = g.gin_id)
            ) x
            WHERE (CAST(? AS UUID) IS NULL OR x.gin_id = ?)
            ORDER BY x.issued_date
            """;

    private static final RowMapper<AvailableGin> AVAILABLE_GIN_MAPPER = (rs, n) -> new AvailableGin(
            rs.getObject("gin_id", UUID.class), rs.getString("gin_code"),
            rs.getObject("issued_date", LocalDateTime.class), rs.getString("issued_project_code"),
            rs.getString("received_project_code"), rs.getInt("item_count"), rs.getInt("asset_count"),
            rs.getBoolean("is_authorized"), rs.getBoolean("is_gate_verified"),
            rs.getString("kind"), rs.getString("held_from_trip_code"));

    /** GINs that can go on a trip leaving {@code origin}; pass a ginId to check just that one. */
    public List<AvailableGin> findAvailableGins(String origin, UUID ginId) {
        return jdbcTemplate.query(AVAILABLE_GIN_SQL, AVAILABLE_GIN_MAPPER,
                origin, origin, origin, origin, origin, ginId, ginId);
    }

    // ---------------------------------------------------------------- trips

    private static final RowMapper<TripRow> TRIP_MAPPER = (rs, n) -> new TripRow(
            rs.getObject("trip_id", UUID.class), rs.getString("trip_code"),
            rs.getString("vehicle_asset_code"), rs.getString("driver_name"),
            rs.getString("origin_project_code"), rs.getString("trip_type"), rs.getString("status"),
            rs.getObject("planned_departure", LocalDateTime.class),
            rs.getObject("actual_departure", LocalDateTime.class), rs.getString("remarks"),
            rs.getString("created_by"), rs.getString("closed_by"),
            rs.getObject("closed_at", LocalDateTime.class));

    private static final String TRIP_COLUMNS = """
            trip_id, trip_code, vehicle_asset_code, driver_name, origin_project_code, trip_type, status,
            planned_departure, actual_departure, remarks, created_by, closed_by, closed_at
            """;

    public long nextTripNumber() {
        Long next = jdbcTemplate.queryForObject("SELECT nextval('transport_trip_code_seq')", Long.class);
        return next == null ? 1 : next;
    }

    public void insertTrip(TripRow trip) {
        jdbcTemplate.update("""
                INSERT INTO transport_trip (trip_id, trip_code, vehicle_asset_code, driver_name,
                    origin_project_code, trip_type, status, planned_departure, remarks, created_by)
                VALUES (?, ?, ?, ?, ?, ?, 'PLANNED', ?, ?, ?)
                """,
                trip.tripId(), trip.tripCode(), trip.vehicleAssetCode(), trip.driverName(),
                trip.originProjectCode(), trip.tripType(), trip.plannedDeparture(), trip.remarks(),
                trip.createdBy());
    }

    public void updatePlannedTrip(TripRow trip) {
        jdbcTemplate.update("""
                UPDATE transport_trip
                SET vehicle_asset_code = ?, driver_name = ?, origin_project_code = ?, trip_type = ?,
                    planned_departure = ?, remarks = ?, updated_at = CURRENT_TIMESTAMP
                WHERE trip_id = ?
                """,
                trip.vehicleAssetCode(), trip.driverName(), trip.originProjectCode(), trip.tripType(),
                trip.plannedDeparture(), trip.remarks(), trip.tripId());
    }

    /** Locks the trip row so two planners acting on the same trip are serialized. */
    public Optional<TripRow> findTripForUpdate(UUID tripId) {
        List<TripRow> found = jdbcTemplate.query(
                "SELECT " + TRIP_COLUMNS + " FROM transport_trip WHERE trip_id = ? FOR UPDATE",
                TRIP_MAPPER, tripId);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    public Optional<TripRow> findTrip(UUID tripId) {
        List<TripRow> found = jdbcTemplate.query(
                "SELECT " + TRIP_COLUMNS + " FROM transport_trip WHERE trip_id = ?", TRIP_MAPPER, tripId);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    public void setTripStatus(UUID tripId, String status) {
        jdbcTemplate.update(
                "UPDATE transport_trip SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE trip_id = ?",
                status, tripId);
    }

    public void markTripDeparted(UUID tripId) {
        jdbcTemplate.update("""
                UPDATE transport_trip
                SET status = 'IN_TRANSIT', actual_departure = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                WHERE trip_id = ?
                """, tripId);
    }

    public void markTripClosed(UUID tripId, String closedBy) {
        jdbcTemplate.update("""
                UPDATE transport_trip
                SET status = 'COMPLETED', closed_by = ?, closed_at = CURRENT_TIMESTAMP,
                    updated_at = CURRENT_TIMESTAMP
                WHERE trip_id = ?
                """, closedBy, tripId);
    }

    public List<TripSummary> findTrips(String status) {
        return jdbcTemplate.query("""
                SELECT t.trip_id, t.trip_code, t.vehicle_asset_code,
                       COALESCE(v.registration_number, t.vehicle_asset_code) AS vehicle_no,
                       t.driver_name, t.origin_project_code, t.trip_type, t.status,
                       t.planned_departure, t.actual_departure,
                       (SELECT COUNT(*) FROM transport_trip_gin a
                         WHERE a.trip_id = t.trip_id AND a.custody_status <> 'REMOVED') AS gin_count,
                       (SELECT COUNT(*) FROM transport_trip_gin a
                         WHERE a.trip_id = t.trip_id AND a.custody_status = 'DELIVERED') AS delivered_count,
                       (SELECT COUNT(*) FROM transport_trip_stop s WHERE s.trip_id = t.trip_id) AS stop_count,
                       nxt.project_code AS next_stop_project, nxt.status AS next_stop_status
                FROM transport_trip t
                LEFT JOIN vehicle_asset_detail v ON v.asset_code = t.vehicle_asset_code
                LEFT JOIN LATERAL (
                    SELECT s.project_code, s.status
                    FROM transport_trip_stop s
                    WHERE s.trip_id = t.trip_id AND s.status IN ('PENDING', 'ARRIVED')
                    ORDER BY s.seq
                    LIMIT 1
                ) nxt ON TRUE
                WHERE (CAST(? AS VARCHAR) IS NULL OR t.status = ?)
                ORDER BY t.created_at DESC
                """,
                (rs, n) -> new TripSummary(
                        rs.getObject("trip_id", UUID.class), rs.getString("trip_code"),
                        rs.getString("vehicle_asset_code"), rs.getString("vehicle_no"),
                        rs.getString("driver_name"), rs.getString("origin_project_code"),
                        rs.getString("trip_type"), rs.getString("status"),
                        rs.getObject("planned_departure", LocalDateTime.class),
                        rs.getObject("actual_departure", LocalDateTime.class),
                        rs.getInt("gin_count"), rs.getInt("delivered_count"), rs.getInt("stop_count"),
                        rs.getString("next_stop_project"), rs.getString("next_stop_status")),
                status, status);
    }

    public String findVehicleNo(String assetCode) {
        List<String> found = jdbcTemplate.queryForList("""
                SELECT COALESCE(v.registration_number, a.asset_code)
                FROM asset a LEFT JOIN vehicle_asset_detail v ON v.asset_code = a.asset_code
                WHERE a.asset_code = ?
                """, String.class, assetCode);
        return found.isEmpty() ? assetCode : found.get(0);
    }

    // ---------------------------------------------------------------- stops

    private static final RowMapper<StopRow> STOP_MAPPER = (rs, n) -> new StopRow(
            rs.getObject("stop_id", UUID.class), rs.getObject("trip_id", UUID.class), rs.getInt("seq"),
            rs.getString("project_code"), rs.getString("stop_type"), rs.getString("status"),
            rs.getObject("eta", LocalDateTime.class), rs.getObject("arrived_at", LocalDateTime.class),
            rs.getObject("departed_at", LocalDateTime.class));

    public void insertStop(StopRow stop) {
        jdbcTemplate.update("""
                INSERT INTO transport_trip_stop (stop_id, trip_id, seq, project_code, stop_type, status, eta)
                VALUES (?, ?, ?, ?, ?, 'PENDING', ?)
                """, stop.stopId(), stop.tripId(), stop.seq(), stop.projectCode(), stop.stopType(), stop.eta());
    }

    public List<StopRow> findStops(UUID tripId) {
        return jdbcTemplate.query("""
                SELECT stop_id, trip_id, seq, project_code, stop_type, status, eta, arrived_at, departed_at
                FROM transport_trip_stop WHERE trip_id = ? ORDER BY seq
                """, STOP_MAPPER, tripId);
    }

    public void deleteStops(UUID tripId) {
        jdbcTemplate.update("DELETE FROM transport_trip_stop WHERE trip_id = ?", tripId);
    }

    /** at is when the vehicle really arrived (entered by the project it reached); null means now. */
    public void markStopArrived(UUID stopId, LocalDateTime at) {
        jdbcTemplate.update(
                "UPDATE transport_trip_stop SET status = 'ARRIVED', arrived_at = COALESCE(?, CURRENT_TIMESTAMP) WHERE stop_id = ?",
                at, stopId);
    }

    public void markStopDone(UUID stopId, LocalDateTime at) {
        jdbcTemplate.update(
                "UPDATE transport_trip_stop SET status = 'DONE', departed_at = COALESCE(?, CURRENT_TIMESTAMP) WHERE stop_id = ?",
                at, stopId);
    }

    /** Pending/arrived stops at {@code projectCode} on trips that are on the road. */
    public List<StopTaskRow> findStopTasks(String projectCode) {
        return jdbcTemplate.query("""
                SELECT s.stop_id, s.trip_id, s.seq, s.project_code, s.stop_type, s.status, s.eta, s.arrived_at,
                       s.departed_at, t.trip_code, COALESCE(v.registration_number, t.vehicle_asset_code) AS vehicle_no,
                       t.driver_name, t.origin_project_code
                FROM transport_trip_stop s
                JOIN transport_trip t ON t.trip_id = s.trip_id
                LEFT JOIN vehicle_asset_detail v ON v.asset_code = t.vehicle_asset_code
                WHERE s.project_code = ? AND s.status IN ('PENDING', 'ARRIVED')
                  AND t.status IN ('IN_TRANSIT', 'RETURNING')
                ORDER BY t.actual_departure, s.seq
                """,
                (rs, n) -> new StopTaskRow(
                        new StopRow(rs.getObject("stop_id", UUID.class), rs.getObject("trip_id", UUID.class),
                                rs.getInt("seq"), rs.getString("project_code"), rs.getString("stop_type"),
                                rs.getString("status"), rs.getObject("eta", LocalDateTime.class),
                                rs.getObject("arrived_at", LocalDateTime.class),
                                rs.getObject("departed_at", LocalDateTime.class)),
                        rs.getString("trip_code"), rs.getString("vehicle_no"), rs.getString("driver_name"),
                        rs.getString("origin_project_code")),
                projectCode);
    }

    public void markStopSkipped(UUID stopId) {
        jdbcTemplate.update("UPDATE transport_trip_stop SET status = 'SKIPPED' WHERE stop_id = ?", stopId);
    }

    // ---------------------------------------------------------------- allocations

    private static final String ALLOCATION_SELECT = """
            SELECT a.allocation_id, a.trip_id, a.gin_id, g.gin_code, g.issued_project_code,
                   g.received_project_code, a.delivery_mode, a.drop_stop_id, ds.project_code AS drop_project,
                   a.custody_status, a.is_active, a.previous_allocation_id,
                   COALESCE(g.is_authorized, FALSE) AS is_authorized,
                   COALESCE(g.is_gate_verified, FALSE) AS is_gate_verified,
                   (SELECT COUNT(*) FROM gin_item i WHERE i.gin_id = g.gin_id) AS item_count,
                   (SELECT r.grn_code FROM grn r WHERE r.gin_id = g.gin_id
                     ORDER BY r.grn_date DESC NULLS LAST LIMIT 1) AS grn_code,
                   COALESCE((SELECT BOOL_OR(COALESCE(r.is_approved, FALSE)) FROM grn r WHERE r.gin_id = g.gin_id),
                            FALSE) AS grn_approved,
                   (SELECT t2.trip_code FROM transport_trip_gin a2
                      JOIN transport_trip t2 ON t2.trip_id = a2.trip_id
                     WHERE a2.previous_allocation_id = a.allocation_id
                     ORDER BY a2.created_at DESC LIMIT 1) AS next_trip_code
            FROM transport_trip_gin a
            JOIN gin g ON g.gin_id = a.gin_id
            LEFT JOIN transport_trip_stop ds ON ds.stop_id = a.drop_stop_id
            """;

    private static final RowMapper<AllocationRow> ALLOCATION_MAPPER = (rs, n) -> new AllocationRow(
            rs.getObject("allocation_id", UUID.class), rs.getObject("trip_id", UUID.class),
            rs.getObject("gin_id", UUID.class), rs.getString("gin_code"),
            rs.getString("issued_project_code"), rs.getString("received_project_code"),
            rs.getString("delivery_mode"), rs.getObject("drop_stop_id", UUID.class),
            rs.getString("drop_project"), rs.getString("custody_status"), rs.getBoolean("is_active"),
            rs.getObject("previous_allocation_id", UUID.class), rs.getBoolean("is_authorized"),
            rs.getBoolean("is_gate_verified"), rs.getInt("item_count"),
            rs.getString("grn_code"), rs.getBoolean("grn_approved"), rs.getString("next_trip_code"));

    public void insertAllocation(UUID allocationId, UUID tripId, UUID ginId, String deliveryMode,
                                 UUID dropStopId, UUID previousAllocationId) {
        jdbcTemplate.update("""
                INSERT INTO transport_trip_gin (allocation_id, trip_id, gin_id, delivery_mode, drop_stop_id,
                    custody_status, is_active, previous_allocation_id)
                VALUES (?, ?, ?, ?, ?, 'ALLOCATED', TRUE, ?)
                """, allocationId, tripId, ginId, deliveryMode, dropStopId, previousAllocationId);
    }

    /** Every allocation on a trip except ones taken off it, oldest first. */
    public List<AllocationRow> findAllocations(UUID tripId) {
        return jdbcTemplate.query(
                ALLOCATION_SELECT + " WHERE a.trip_id = ? AND a.custody_status <> 'REMOVED' ORDER BY a.created_at, g.gin_code",
                ALLOCATION_MAPPER, tripId);
    }

    public Optional<AllocationRow> findAllocation(UUID allocationId) {
        List<AllocationRow> found = jdbcTemplate.query(
                ALLOCATION_SELECT + " WHERE a.allocation_id = ?", ALLOCATION_MAPPER, allocationId);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    public Optional<AllocationRow> findActiveAllocationByGin(UUID ginId) {
        List<AllocationRow> found = jdbcTemplate.query(
                ALLOCATION_SELECT + " WHERE a.gin_id = ? AND a.is_active = TRUE", ALLOCATION_MAPPER, ginId);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    public void updateAllocation(UUID allocationId, String custodyStatus, boolean active) {
        jdbcTemplate.update("""
                UPDATE transport_trip_gin
                SET custody_status = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP
                WHERE allocation_id = ?
                """, custodyStatus, active, allocationId);
    }

    public void markAllocationReturned(UUID allocationId, String baseProjectCode) {
        jdbcTemplate.update("""
                UPDATE transport_trip_gin
                SET custody_status = 'RETURNED_TO_STORE', is_active = FALSE, returned_at_project = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE allocation_id = ?
                """, baseProjectCode, allocationId);
    }

    /** Puts the trip's vehicle on the GIN when it has none (the GIN form and the GRN then default to it). */
    public void fillGinVehicle(UUID ginId, String vehicleAssetCode, String vehicleNo) {
        jdbcTemplate.update("""
                UPDATE gin SET vehicle_asset_code = ?, vehicle_no = ?
                WHERE gin_id = ? AND COALESCE(vehicle_asset_code, '') = '' AND COALESCE(vehicle_no, '') = ''
                """, vehicleAssetCode, vehicleNo, ginId);
    }

    /** Takes that trip vehicle back off the GIN when it comes off a planned trip. */
    public void clearGinVehicle(UUID ginId, String vehicleAssetCode) {
        jdbcTemplate.update(
                "UPDATE gin SET vehicle_asset_code = NULL, vehicle_no = NULL WHERE gin_id = ? AND vehicle_asset_code = ?",
                ginId, vehicleAssetCode);
    }

    public List<String> findGinAssetCodes(UUID ginId) {
        return jdbcTemplate.queryForList("""
                SELECT asset_code FROM gin_item
                WHERE gin_id = ? AND COALESCE(asset_code, '') <> ''
                """, String.class, ginId);
    }

    // ---------------------------------------------------------------- custody events

    public void insertEvent(UUID ginId, UUID tripId, String eventType, String locationProjectCode,
                            String performedBy, String remarks) {
        insertEvent(ginId, tripId, eventType, locationProjectCode, performedBy, remarks, null);
    }

    /** eventTime is when it really happened (entered by whoever did it); null means now. */
    public void insertEvent(UUID ginId, UUID tripId, String eventType, String locationProjectCode,
                            String performedBy, String remarks, LocalDateTime eventTime) {
        jdbcTemplate.update("""
                INSERT INTO gin_custody_event (event_id, gin_id, trip_id, event_type, location_project_code,
                    performed_by, event_time, remarks)
                VALUES (?, ?, ?, ?, ?, ?, COALESCE(?, CURRENT_TIMESTAMP), ?)
                """, UUID.randomUUID(), ginId, tripId, eventType, locationProjectCode, performedBy, eventTime, remarks);
    }

    /** Everything that happened to the GINs of one trip, oldest first. */
    public List<com.rr.erp.dto.TransportDtos.TripActivity> findTripActivity(UUID tripId) {
        return jdbcTemplate.query("""
                SELECT e.event_id, e.gin_id, g.gin_code, e.event_type, e.location_project_code, e.performed_by,
                       e.event_time, e.remarks
                FROM gin_custody_event e
                JOIN gin g ON g.gin_id = e.gin_id
                WHERE e.trip_id = ?
                ORDER BY e.event_time, e.event_id
                """,
                (rs, n) -> new com.rr.erp.dto.TransportDtos.TripActivity(
                        rs.getObject("event_id", UUID.class), rs.getObject("gin_id", UUID.class),
                        rs.getString("gin_code"), rs.getString("event_type"),
                        rs.getString("location_project_code"), rs.getString("performed_by"),
                        rs.getObject("event_time", LocalDateTime.class), rs.getString("remarks")),
                tripId);
    }

    public List<CustodyEvent> findEvents(UUID ginId) {
        return jdbcTemplate.query("""
                SELECT e.event_id, e.gin_id, e.trip_id, t.trip_code, e.event_type, e.location_project_code,
                       e.performed_by, e.event_time, e.remarks
                FROM gin_custody_event e
                LEFT JOIN transport_trip t ON t.trip_id = e.trip_id
                WHERE e.gin_id = ?
                ORDER BY e.event_time, e.event_id
                """,
                (rs, n) -> new CustodyEvent(
                        rs.getObject("event_id", UUID.class), rs.getObject("gin_id", UUID.class),
                        rs.getObject("trip_id", UUID.class), rs.getString("trip_code"),
                        rs.getString("event_type"), rs.getString("location_project_code"),
                        rs.getString("performed_by"), rs.getObject("event_time", LocalDateTime.class),
                        rs.getString("remarks")),
                ginId);
    }

    // ---------------------------------------------------------------- hub + incoming

    public List<HeldGin> findHeldAtHub(String hubProjectCode) {
        return jdbcTemplate.query("""
                SELECT g.gin_id, g.gin_code, g.issued_project_code, g.received_project_code,
                       (SELECT COUNT(*) FROM gin_item i WHERE i.gin_id = g.gin_id) AS item_count,
                       t.trip_code,
                       (SELECT MAX(e.event_time) FROM gin_custody_event e
                         WHERE e.gin_id = g.gin_id AND e.event_type = 'UNLOADED_TO_HUB') AS held_since
                FROM transport_trip_gin a
                JOIN gin g ON g.gin_id = a.gin_id
                JOIN transport_trip t ON t.trip_id = a.trip_id
                JOIN transport_trip_stop s ON s.stop_id = a.drop_stop_id
                WHERE a.is_active = TRUE AND a.custody_status = 'AT_HUB' AND s.project_code = ?
                ORDER BY held_since, g.gin_code
                """,
                (rs, n) -> new HeldGin(
                        rs.getObject("gin_id", UUID.class), rs.getString("gin_code"),
                        rs.getString("issued_project_code"), rs.getString("received_project_code"),
                        rs.getInt("item_count"), rs.getString("trip_code"),
                        rs.getObject("held_since", LocalDateTime.class)),
                hubProjectCode);
    }

    /** GINs on the road to this project as a hub: left their origin, not yet unloaded here. */
    public List<HubArrivalRow> findHubArrivals(String hubProjectCode) {
        return jdbcTemplate.query("""
                SELECT g.gin_id, g.gin_code, g.issued_date, g.issued_project_code, g.received_project_code,
                       (SELECT COUNT(*) FROM gin_item i WHERE i.gin_id = g.gin_id) AS item_count,
                       t.trip_code, COALESCE(v.registration_number, t.vehicle_asset_code) AS vehicle_no
                FROM transport_trip_gin a
                JOIN gin g ON g.gin_id = a.gin_id
                JOIN transport_trip t ON t.trip_id = a.trip_id
                JOIN transport_trip_stop s ON s.stop_id = a.drop_stop_id
                LEFT JOIN vehicle_asset_detail v ON v.asset_code = t.vehicle_asset_code
                WHERE a.is_active = TRUE AND a.delivery_mode = 'VIA_HUB' AND a.custody_status = 'ON_VEHICLE'
                  AND s.project_code = ? AND t.status IN ('IN_TRANSIT', 'RETURNING')
                ORDER BY g.issued_date
                """,
                (rs, n) -> new HubArrivalRow(
                        rs.getObject("gin_id", UUID.class), rs.getString("gin_code"),
                        rs.getObject("issued_date", LocalDateTime.class), rs.getString("issued_project_code"),
                        rs.getString("received_project_code"), rs.getInt("item_count"),
                        rs.getString("trip_code"), rs.getString("vehicle_no")),
                hubProjectCode);
    }

    /** Every stop a vehicle reached at this project (most recent first). */
    public List<VisitRow> findVisits(String projectCode) {
        return jdbcTemplate.query("""
                SELECT s.stop_id, t.trip_id, t.trip_code, COALESCE(v.registration_number, t.vehicle_asset_code) AS vehicle_no,
                       t.driver_name, t.origin_project_code, s.stop_type, s.status, s.arrived_at, s.departed_at
                FROM transport_trip_stop s
                JOIN transport_trip t ON t.trip_id = s.trip_id
                LEFT JOIN vehicle_asset_detail v ON v.asset_code = t.vehicle_asset_code
                WHERE s.project_code = ? AND s.status IN ('ARRIVED', 'DONE') AND t.status <> 'CANCELLED'
                ORDER BY s.arrived_at DESC NULLS LAST
                LIMIT 200
                """,
                (rs, n) -> new VisitRow(
                        rs.getObject("stop_id", UUID.class), rs.getObject("trip_id", UUID.class),
                        rs.getString("trip_code"), rs.getString("vehicle_no"), rs.getString("driver_name"),
                        rs.getString("origin_project_code"), rs.getString("stop_type"), rs.getString("status"),
                        rs.getObject("arrived_at", LocalDateTime.class), rs.getObject("departed_at", LocalDateTime.class)),
                projectCode);
    }

    /** GINs the trip dropped at one stop, whatever has happened to them since. */
    public List<AllocationRow> findAllocationsByDropStop(UUID stopId) {
        return jdbcTemplate.query(
                ALLOCATION_SELECT + " WHERE a.drop_stop_id = ? AND a.custody_status <> 'REMOVED' ORDER BY g.gin_code",
                ALLOCATION_MAPPER, stopId);
    }

    /** How a GIN left a hub (collected by the destination, or sent on by the hub), if it has. */
    public Optional<ReleaseEventRow> findReleaseEvent(UUID ginId, UUID tripId) {
        List<ReleaseEventRow> found = jdbcTemplate.query("""
                SELECT event_type, remarks, event_time FROM gin_custody_event
                WHERE gin_id = ? AND trip_id = ? AND event_type IN ('COLLECTED_FROM_HUB', 'DISPATCHED_FROM_HUB')
                ORDER BY event_time DESC LIMIT 1
                """,
                (rs, n) -> new ReleaseEventRow(rs.getString("event_type"), rs.getString("remarks"),
                        rs.getObject("event_time", LocalDateTime.class)),
                ginId, tripId);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    /** Goods for this project that came out of a hub, newest first. */
    public List<com.rr.erp.dto.TransportDtos.HubRelease> findHubReleasesFor(String destinationProjectCode) {
        return jdbcTemplate.query("""
                SELECT g.gin_code, e.location_project_code AS hub, g.issued_project_code, e.event_type, e.remarks,
                       e.event_time,
                       (SELECT r.grn_code FROM grn r WHERE r.gin_id = g.gin_id
                         ORDER BY r.grn_date DESC NULLS LAST LIMIT 1) AS grn_code,
                       COALESCE((SELECT BOOL_OR(COALESCE(r.is_approved, FALSE)) FROM grn r WHERE r.gin_id = g.gin_id),
                                FALSE) AS grn_approved
                FROM gin_custody_event e
                JOIN gin g ON g.gin_id = e.gin_id
                WHERE e.event_type IN ('COLLECTED_FROM_HUB', 'DISPATCHED_FROM_HUB')
                  AND g.received_project_code = ?
                ORDER BY e.event_time DESC
                LIMIT 200
                """,
                (rs, n) -> new com.rr.erp.dto.TransportDtos.HubRelease(
                        rs.getString("gin_code"), rs.getString("hub"), rs.getString("issued_project_code"),
                        rs.getString("event_type"), rs.getString("remarks"),
                        rs.getObject("event_time", LocalDateTime.class), rs.getString("grn_code"),
                        rs.getBoolean("grn_approved")),
                destinationProjectCode);
    }

    public List<IncomingRow> findIncoming(String projectCode) {
        return jdbcTemplate.query("""
                SELECT g.gin_id, g.gin_code, g.issued_project_code,
                       (SELECT COUNT(*) FROM gin_item i WHERE i.gin_id = g.gin_id) AS item_count,
                       a.delivery_mode, a.custody_status,
                       t.trip_id, t.trip_code, t.status AS trip_status, t.vehicle_asset_code,
                       COALESCE(vd.registration_number, t.vehicle_asset_code) AS vehicle_no, t.driver_name,
                       ds.project_code AS drop_project, ds.eta AS drop_eta, ds.seq AS drop_seq,
                       (SELECT e.remarks FROM gin_custody_event e
                         WHERE e.gin_id = g.gin_id AND e.trip_id = t.trip_id
                           AND e.event_type IN ('COLLECTED_FROM_HUB', 'DISPATCHED_FROM_HUB')
                         ORDER BY e.event_time DESC LIMIT 1) AS leg_note,
                       (SELECT e.event_time FROM gin_custody_event e
                         WHERE e.gin_id = g.gin_id AND e.trip_id = t.trip_id
                           AND e.event_type IN ('COLLECTED_FROM_HUB', 'DISPATCHED_FROM_HUB')
                         ORDER BY e.event_time DESC LIMIT 1) AS leg_time
                FROM transport_trip_gin a
                JOIN gin g ON g.gin_id = a.gin_id
                JOIN transport_trip t ON t.trip_id = a.trip_id
                LEFT JOIN transport_trip_stop ds ON ds.stop_id = a.drop_stop_id
                LEFT JOIN vehicle_asset_detail vd ON vd.asset_code = t.vehicle_asset_code
                WHERE a.is_active = TRUE
                  AND g.received_project_code = ?
                  AND t.status <> 'CANCELLED'
                  AND a.custody_status IN ('ALLOCATED', 'ON_VEHICLE', 'AT_HUB', 'COLLECTED', 'AT_DESTINATION', 'UNDELIVERED')
                ORDER BY t.planned_departure NULLS LAST, g.gin_code
                """,
                (rs, n) -> new IncomingRow(
                        rs.getObject("gin_id", UUID.class), rs.getString("gin_code"),
                        rs.getString("issued_project_code"), rs.getInt("item_count"),
                        rs.getString("delivery_mode"), rs.getString("custody_status"),
                        rs.getObject("trip_id", UUID.class), rs.getString("trip_code"),
                        rs.getString("trip_status"), rs.getString("vehicle_asset_code"),
                        rs.getString("vehicle_no"), rs.getString("driver_name"),
                        rs.getString("drop_project"), rs.getObject("drop_eta", LocalDateTime.class),
                        rs.getObject("drop_seq", Integer.class),
                        rs.getString("leg_note"), rs.getObject("leg_time", LocalDateTime.class)),
                projectCode);
    }

    // ---------------------------------------------------------------- cross-module guards and info

    /** Trip code + vehicle for each GIN that currently has an active allocation (gate pass queue). */
    public Map<UUID, GinTripInfo> findTripInfoByGinIds(Collection<UUID> ginIds) {

        Map<UUID, GinTripInfo> result = new HashMap<>();
        if (ginIds == null || ginIds.isEmpty()) {
            return result;
        }

        String placeholders = String.join(",", ginIds.stream().map(id -> "?").toList());
        jdbcTemplate.query(
                "SELECT a.gin_id, t.trip_code, COALESCE(v.registration_number, t.vehicle_asset_code) AS vehicle_no "
                        + "FROM transport_trip_gin a "
                        + "JOIN transport_trip t ON t.trip_id = a.trip_id "
                        + "LEFT JOIN vehicle_asset_detail v ON v.asset_code = t.vehicle_asset_code "
                        + "WHERE a.is_active = TRUE AND a.gin_id IN (" + placeholders + ")",
                rs -> {
                    result.put(rs.getObject("gin_id", UUID.class),
                            new GinTripInfo(rs.getString("trip_code"), rs.getString("vehicle_no")));
                },
                ginIds.toArray());
        return result;
    }

    /**
     * True while a GIN is still on its way TO a hub (planned or on the vehicle): it is not yet held anywhere
     * the destination can receive it from, so the destination's gate check and GRN must wait. Once it is held
     * at the hub the destination may receive it directly (see TransportService#prepareReceipt).
     */
    public boolean isOnHubRoute(UUID ginId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM transport_trip_gin
                WHERE gin_id = ? AND is_active = TRUE AND delivery_mode = 'VIA_HUB'
                  AND custody_status IN ('ALLOCATED', 'ON_VEHICLE')
                """, Integer.class, ginId);
        return count != null && count > 0;
    }

    /**
     * True if a trip carries (or carried) the GIN, so the trip, not the GIN/GRN, owns the vehicle's location
     * history. A GIN the destination collected from a hub itself is not counted: its own vehicle is recorded
     * by the GRN as for any GIN.
     */
    public boolean hasTripAllocation(UUID ginId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM transport_trip_gin WHERE gin_id = ? AND custody_status NOT IN ('REMOVED', 'HANDED_OVER', 'COLLECTED')
                """, Integer.class, ginId);
        return count != null && count > 0;
    }
}
