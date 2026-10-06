package com.rr.erp.repository;



import com.rr.erp.dto.FuelAssetSummary;
import com.rr.erp.dto.FuelConsumptionReport;
import com.rr.erp.entity.FuelIssue;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FuelIssueRepository {

    private final JdbcTemplate jdbcTemplate;


    public int createFuelIssue(FuelIssue fuelIssue) {

        String sql = """
                INSERT INTO fuel_issue (
                    fuel_issue_id,
                    fuel_issue_code,
                    issued_date,
                    fuel_issue_date,
                    project_code,
                    issued_by,
                    received_by,
                    remarks,
                    asset_code,
                    fuel_type,
                    quantity,
                    meter_reading,
                    is_issued,
                    is_received,
                    is_active,
                    is_filled
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                fuelIssue.getFuelIssueId(),
                fuelIssue.getFuelIssueCode(),
                fuelIssue.getIssuedDate(),
                fuelIssue.getFuelIssueDate(),
                fuelIssue.getProjectCode(),
                fuelIssue.getIssuedBy(),
                fuelIssue.getReceivedBy(),
                fuelIssue.getRemarks(),
                fuelIssue.getAssetCode(),
                fuelIssue.getFuelType(),
                fuelIssue.getQuantity(),
                fuelIssue.getMeterReading(),
                fuelIssue.getIsIssued(),
                fuelIssue.getIsReceived(),
                fuelIssue.getIsActive(),
                fuelIssue.getIsFilled()
        );
    }


    // Soft-deleted rows (is_active = FALSE) keep their fuel_issue_code under the unique
    // constraint, so the next code must be derived from every row ever created for this
    // project — not just the active ones the UI lists — or a re-issue after a delete
    // collides with the deleted row's code.
    public List<String> getFuelIssueCodesForProject(String projectCode) {

        String sql = """
                SELECT fuel_issue_code
                FROM fuel_issue
                WHERE project_code = ?
                """;

        return jdbcTemplate.queryForList(sql, String.class, projectCode);
    }

    public List<FuelIssue> getIssuedFuel(
            String projectCode
    ) {

        String sql = """
                SELECT *
                FROM fuel_issue
                WHERE project_code = ?
                  AND is_issued = TRUE
                  AND is_active = TRUE
                ORDER BY fuel_issue_date DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapFuelIssueSnakeCase(rs),
                projectCode
        );
    }


    public Optional<FuelIssue> findById(UUID fuelIssueId) {

        String sql = """
                SELECT *
                FROM fuel_issue
                WHERE fuel_issue_id = ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapFuelIssueSnakeCase(rs),
                fuelIssueId
        ).stream().findFirst();
    }

    public int receiveFuel(
            UUID fuelIssueId
    ) {

        String sql = """
                UPDATE fuel_issue
                SET
                    is_received = TRUE
                WHERE fuel_issue_id = ?
                  AND is_active = TRUE
                  AND is_received = FALSE
                """;

        return jdbcTemplate.update(
                sql,
                fuelIssueId
        );
    }


    public int deleteFuelIssue(UUID fuelIssueId) {

        String sql = """
                UPDATE fuel_issue
                SET is_active = FALSE
                WHERE fuel_issue_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                fuelIssueId
        );
    }

    public List<FuelIssue> getReceivedFuelIssues(String receivedBy) {

        String sql = """
            SELECT
                fuel_issue_id AS "fuelIssueId",
                fuel_issue_code AS "fuelIssueCode",
                project_code AS "projectCode",
                asset_code AS "assetCode",
                fuel_type AS "fuelType",
                quantity AS "quantity",
                meter_reading AS "meterReading",
                issued_by AS "issuedBy",
                issued_date AS "issuedDate",
                fuel_issue_date AS "fuelIssueDate",
                is_received AS "isReceived",
                received_by AS "receivedBy",
                is_active AS "isActive",
                is_filled AS "isFilled",
                remarks AS "remarks"
            FROM fuel_issue
            WHERE received_by = ?
              AND is_active = TRUE
            ORDER BY fuel_issue_date DESC
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapFuelIssue(rs),
                receivedBy
        );
    }

    private FuelIssue mapFuelIssue(ResultSet rs) throws SQLException {

        FuelIssue fuelIssue = new FuelIssue();

        fuelIssue.setFuelIssueId(
                rs.getObject("fuelIssueId", UUID.class)
        );

        fuelIssue.setFuelIssueCode(
                rs.getString("fuelIssueCode")
        );

        fuelIssue.setProjectCode(
                rs.getString("projectCode")
        );

        fuelIssue.setAssetCode(
                rs.getString("assetCode")
        );

        fuelIssue.setFuelType(
                rs.getString("fuelType")
        );

        fuelIssue.setQuantity(
                rs.getBigDecimal("quantity")
        );

        fuelIssue.setMeterReading(
                rs.getObject("meterReading", Integer.class)
        );

        fuelIssue.setIssuedBy(
                rs.getString("issuedBy")
        );

        if (rs.getTimestamp("issuedDate") != null) {
            fuelIssue.setIssuedDate(
                    rs.getTimestamp("issuedDate").toLocalDateTime()
            );
        }

        if (rs.getTimestamp("fuelIssueDate") != null) {
            fuelIssue.setFuelIssueDate(
                    rs.getTimestamp("fuelIssueDate").toLocalDateTime()
            );
        }

        fuelIssue.setIsReceived(
                rs.getBoolean("isReceived")
        );

        fuelIssue.setReceivedBy(
                rs.getString("receivedBy")
        );

        fuelIssue.setIsActive(
                rs.getBoolean("isActive")
        );

        fuelIssue.setRemarks(
                rs.getString("remarks")
        );

        fuelIssue.setIsFilled(
                rs.getBoolean("isFilled")
        );

        return fuelIssue;
    }

    private FuelIssue mapFuelIssueSnakeCase(ResultSet rs) throws SQLException {

        FuelIssue fuelIssue = new FuelIssue();

        fuelIssue.setFuelIssueId(
                rs.getObject("fuel_issue_id", UUID.class)
        );

        fuelIssue.setFuelIssueCode(
                rs.getString("fuel_issue_code")
        );

        fuelIssue.setIssuedDate(
                rs.getObject("issued_date", java.time.LocalDateTime.class)
        );

        fuelIssue.setFuelIssueDate(
                rs.getObject("fuel_issue_date", java.time.LocalDateTime.class)
        );

        fuelIssue.setProjectCode(
                rs.getString("project_code")
        );

        fuelIssue.setIssuedBy(
                rs.getString("issued_by")
        );

        fuelIssue.setReceivedBy(
                rs.getString("received_by")
        );

        fuelIssue.setRemarks(
                rs.getString("remarks")
        );

        fuelIssue.setAssetCode(
                rs.getString("asset_code")
        );

        fuelIssue.setFuelType(
                rs.getString("fuel_type")
        );

        fuelIssue.setQuantity(
                rs.getBigDecimal("quantity")
        );

        fuelIssue.setMeterReading(
                rs.getObject("meter_reading", Integer.class)
        );

        fuelIssue.setIsIssued(
                rs.getBoolean("is_issued")
        );

        fuelIssue.setIsReceived(
                rs.getBoolean("is_received")
        );

        fuelIssue.setIsActive(
                rs.getBoolean("is_active")
        );

        fuelIssue.setIsFilled(
                rs.getBoolean("is_filled")
        );

        return fuelIssue;
    }

    public List<FuelAssetSummary> getFuelAssetSummary() {

        String sql = """
            SELECT
                fi.asset_code,
                ig.item_category_name AS asset_class,

                COUNT(fi.fuel_issue_id) AS fuel_gins,

                COALESCE(SUM(fi.quantity), 0) AS total_consumption,

                (
                    SELECT fi2.meter_reading
                    FROM fuel_issue fi2
                    WHERE fi2.asset_code = fi.asset_code
                      AND fi2.is_active = TRUE
                    ORDER BY fi2.fuel_issue_date DESC
                    LIMIT 1
                ) AS latest_meter_reading,

                MAX(fi.fuel_issue_date) AS last_issued

            FROM fuel_issue fi

            LEFT JOIN asset_code a
                ON a.asset_code_code = fi.asset_code

            LEFT JOIN item_code ic
                ON ic.item_code_id = a.item_code_id

            LEFT JOIN item_category ig
                ON ig.item_category_id = ic.item_category_id

            WHERE fi.is_active = TRUE
              AND fi.asset_code IS NOT NULL

            GROUP BY
                fi.asset_code,
                ig.item_category_name

            ORDER BY fi.asset_code
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    FuelAssetSummary summary = new FuelAssetSummary();

                    summary.setAssetCode(
                            rs.getString("asset_code")
                    );

                    summary.setAssetClass(
                            rs.getString("asset_class")
                    );

                    summary.setFuelGins(
                            rs.getLong("fuel_gins")
                    );

                    summary.setTotalConsumption(
                            rs.getBigDecimal("total_consumption")
                    );

                    summary.setLatestMeterReading(
                            rs.getObject("latest_meter_reading", Integer.class)
                    );

                    summary.setLastIssued(
                            rs.getTimestamp("last_issued")
                    );

                    return summary;
                }
        );
    }
    public List<FuelIssue> getFuelIssuesByAssetCode(
            String assetCode
    ) {

        String sql = """
                SELECT *
                FROM fuel_issue
                WHERE asset_code = ?
                  AND is_issued = TRUE
                  AND is_active = TRUE
                ORDER BY fuel_issue_date DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapFuelIssueSnakeCase(rs),
                assetCode
        );
    }

    /**
     * Per project/fuel-item consumption totals — the "total fuel consumption in the project"
     * and cross-project "distribution" report. projectCode is optional: null returns every
     * project (the distribution view), a specific code scopes to that project.
     */
    public List<FuelConsumptionReport> getFuelConsumptionReport(String projectCode) {

        String sql = """
                SELECT
                    fi.project_code,
                    p.projectName,
                    fi.fuel_type AS fuel_item_code,
                    COALESCE(ic.item_code_name, fi.fuel_type) AS fuel_item_name,
                    SUM(fi.quantity) AS total_issued,
                    COUNT(fi.fuel_issue_id) AS issue_count
                FROM fuel_issue fi
                LEFT JOIN project p
                    ON p.project_code = fi.project_code
                LEFT JOIN item_code ic
                    ON ic.item_code_code = fi.fuel_type
                WHERE fi.is_active = TRUE
                  AND (CAST(? AS VARCHAR) IS NULL OR fi.project_code = CAST(? AS VARCHAR))
                GROUP BY fi.project_code, p.projectName, fi.fuel_type, ic.item_code_name
                ORDER BY fi.project_code, fuel_item_code
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    FuelConsumptionReport report = new FuelConsumptionReport();

                    report.setProjectCode(rs.getString("project_code"));
                    report.setProjectName(rs.getString("projectName"));
                    report.setFuelItemCode(rs.getString("fuel_item_code"));
                    report.setFuelItemName(rs.getString("fuel_item_name"));
                    report.setTotalIssued(rs.getBigDecimal("total_issued"));
                    report.setIssueCount(rs.getLong("issue_count"));

                    return report;
                },
                projectCode,
                projectCode
        );
    }
}
