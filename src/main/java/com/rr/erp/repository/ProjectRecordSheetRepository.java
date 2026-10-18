package com.rr.erp.repository;

import com.rr.erp.dto.ProjectRecordSheetResponse.LabourRow;
import com.rr.erp.dto.ProjectRecordSheetResponse.MaterialRow;
import com.rr.erp.dto.ProjectRecordSheetResponse.PlantRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public class ProjectRecordSheetRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProjectRecordSheetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Labour: the daily labour log (actual stints worked on the project).
    public List<LabourRow> findLabour(String projectCode, LocalDate from, LocalDate to) {

        // Logged stints, plus planned daily allocations for employees with no logged stint on that
        // project that day (so allocated staff show even before their hours are logged).
        String sql = """
                SELECT * FROM (
                    SELECT
                        l.work_date AS "workDate",
                        l.employee_code AS "employeeCode",
                        e.full_name AS "employeeName",
                        COALESCE(d.designationname, 'Unspecified') AS "trade",
                        l.start_time AS "startTime",
                        l.end_time AS "endTime",
                        l.hours AS "hours",
                        l.work_description AS "workDescription"
                    FROM labour_entry l
                    JOIN employee e ON e.employee_code = l.employee_code
                    LEFT JOIN designation d ON d.designationid = e.designationid
                    WHERE l.is_active = TRUE
                      AND l.project_code = ?
                      AND l.work_date BETWEEN ? AND ?
                    UNION ALL
                    SELECT
                        a.work_date,
                        a.employee_code,
                        e.full_name,
                        COALESCE(d.designationname, 'Unspecified'),
                        CAST(NULL AS time),
                        CAST(NULL AS time),
                        COALESCE(a.hours, 0),
                        COALESCE(a.remarks, 'Allocated')
                    FROM employee_project_allocation a
                    JOIN project p ON p.projectid = a.project_id AND p.project_code = ?
                    JOIN employee e ON e.employee_code = a.employee_code
                    LEFT JOIN designation d ON d.designationid = e.designationid
                    WHERE a.work_date BETWEEN ? AND ?
                      AND NOT EXISTS (
                          SELECT 1 FROM labour_entry l2
                          WHERE l2.is_active = TRUE AND l2.project_code = p.project_code
                            AND l2.employee_code = a.employee_code AND l2.work_date = a.work_date)
                ) t
                ORDER BY "workDate", "trade", "employeeName"
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            LabourRow row = new LabourRow();
            row.setWorkDate(rs.getObject("workDate", LocalDate.class));
            row.setEmployeeCode(rs.getString("employeeCode"));
            row.setEmployeeName(rs.getString("employeeName"));
            row.setTrade(rs.getString("trade"));
            row.setStartTime(rs.getObject("startTime", LocalTime.class));
            row.setEndTime(rs.getObject("endTime", LocalTime.class));
            row.setHours(rs.getBigDecimal("hours"));
            row.setWorkDescription(rs.getString("workDescription"));
            return row;
        }, projectCode, from, to, projectCode, from, to);
    }

    // Plant: for every day in the range, the assets whose latest location entry on or before that day
    // is this project (i.e. on site that day), joined to that day's meter readings if any.
    public List<PlantRow> findPlant(String projectCode, LocalDate from, LocalDate to) {

        String sql = """
                WITH days AS (
                    SELECT CAST(g AS date) AS d
                    FROM generate_series(CAST(? AS date), CAST(? AS date), INTERVAL '1 day') g
                ),
                placed AS (
                    SELECT DISTINCT ON (days.d, al.asset_code)
                        days.d AS d, al.asset_code AS asset_code, al.new_location AS location
                    FROM days
                    JOIN asset_location al ON al.is_active = TRUE AND al.changed_date <= days.d
                    ORDER BY days.d, al.asset_code, al.changed_date DESC, al.created_at DESC
                ),
                readings AS (
                    SELECT asset_code, reading_date, MAX(meter_type) AS meter_type,
                           MIN(previous_reading) AS meter_in, MAX(reading_value) AS meter_out,
                           SUM(usage_value) AS usage_value
                    FROM meter_reading
                    WHERE reading_date BETWEEN ? AND ?
                    GROUP BY asset_code, reading_date
                )
                SELECT
                    placed.d AS "workDate",
                    placed.asset_code AS "assetCode",
                    a.description AS "description",
                    a.asset_class AS "assetClass",
                    r.meter_type AS "meterType",
                    r.meter_in AS "meterIn",
                    r.meter_out AS "meterOut",
                    r.usage_value AS "usage"
                FROM placed
                LEFT JOIN asset a ON a.asset_code = placed.asset_code
                LEFT JOIN readings r ON r.asset_code = placed.asset_code AND r.reading_date = placed.d
                WHERE placed.location = ?
                ORDER BY placed.d, placed.asset_code
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            PlantRow row = new PlantRow();
            row.setWorkDate(rs.getObject("workDate", LocalDate.class));
            row.setAssetCode(rs.getString("assetCode"));
            row.setDescription(rs.getString("description"));
            row.setAssetClass(rs.getString("assetClass"));
            row.setMeterType(rs.getString("meterType"));
            row.setMeterIn(rs.getBigDecimal("meterIn"));
            row.setMeterOut(rs.getBigDecimal("meterOut"));
            row.setUsage(rs.getBigDecimal("usage"));
            return row;
        }, from, to, from, to, projectCode);
    }

    // Materials: stock items issued from the project's store (asset lines are plant, so excluded),
    // plus approved returns back to it.
    public List<MaterialRow> findMaterials(String projectCode, LocalDate from, LocalDate to) {

        String sql = """
                SELECT
                    CAST(i.issued_date AS date) AS "issueDate",
                    'ISSUE' AS "entryType",
                    i.intra_project_issue_code AS "documentCode",
                    i.issue_type AS "issueType",
                    it.item_code AS "itemCode",
                    COALESCE(it.description, ic.item_code_name) AS "description",
                    it."size" AS "size",
                    u.uom_name AS "unit",
                    it.quantity AS "quantity",
                    COALESCE(it.amount, it.quantity * it.unit_price, 0) AS "amount",
                    it.remarks AS "remarks"
                FROM intra_project_issue i
                JOIN intra_project_issue_item it ON it.intra_project_issue_id = i.intra_project_issue_id
                LEFT JOIN item_code ic ON ic.item_code_code = it.item_code
                LEFT JOIN uom u ON u.uom_id = it.uom_id
                WHERE i.issued_project_code = ?
                  AND i.is_issued = TRUE
                  AND it.asset_code IS NULL
                  AND CAST(i.issued_date AS date) BETWEEN ? AND ?
                UNION ALL
                SELECT
                    r.return_date AS "issueDate",
                    'RETURN' AS "entryType",
                    r.intra_project_issue_return_code AS "documentCode",
                    r.return_type AS "issueType",
                    ri.item_code AS "itemCode",
                    COALESCE(ri.description, ic.item_code_name) AS "description",
                    ri."size" AS "size",
                    u.uom_name AS "unit",
                    ri.quantity AS "quantity",
                    COALESCE(ri.amount, ri.quantity * ri.unit_price, 0) AS "amount",
                    ri.remarks AS "remarks"
                FROM intra_project_issue_return r
                JOIN intra_project_issue_return_item ri ON ri.intra_project_issue_return_id = r.intra_project_issue_return_id
                LEFT JOIN item_code ic ON ic.item_code_code = ri.item_code
                LEFT JOIN uom u ON u.uom_id = ri.uom_id
                WHERE r.issued_project_code = ?
                  AND r.is_approved = TRUE
                  AND ri.asset_code IS NULL
                  AND r.return_date BETWEEN ? AND ?
                ORDER BY "issueDate", "entryType", "description"
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            MaterialRow row = new MaterialRow();
            row.setIssueDate(rs.getObject("issueDate", LocalDate.class));
            row.setEntryType(rs.getString("entryType"));
            row.setDocumentCode(rs.getString("documentCode"));
            row.setIssueType(rs.getString("issueType"));
            row.setItemCode(rs.getString("itemCode"));
            row.setDescription(rs.getString("description"));
            row.setSize(rs.getString("size"));
            row.setUnit(rs.getString("unit"));
            row.setQuantity(rs.getBigDecimal("quantity"));
            row.setAmount(rs.getBigDecimal("amount"));
            row.setRemarks(rs.getString("remarks"));
            return row;
        }, projectCode, from, to, projectCode, from, to);
    }
}
