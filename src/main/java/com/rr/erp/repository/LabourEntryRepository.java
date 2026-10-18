package com.rr.erp.repository;

import com.rr.erp.entity.LabourEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class LabourEntryRepository {

    private final JdbcTemplate jdbcTemplate;

    public LabourEntryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<LabourEntry> ROW_MAPPER = (rs, rowNum) -> {
        LabourEntry row = new LabourEntry();
        row.setLabourEntryId(rs.getObject("labourEntryId", UUID.class));
        row.setEmployeeCode(rs.getString("employeeCode"));
        row.setEmployeeName(rs.getString("employeeName"));
        row.setProjectCode(rs.getString("projectCode"));
        row.setProjectName(rs.getString("projectName"));
        row.setWorkDate(rs.getObject("workDate", LocalDate.class));
        row.setStartTime(rs.getObject("startTime", LocalTime.class));
        row.setEndTime(rs.getObject("endTime", LocalTime.class));
        row.setHours(rs.getBigDecimal("hours"));
        row.setWorkDescription(rs.getString("workDescription"));
        row.setRecordedBy(rs.getString("recordedBy"));
        row.setIsActive(rs.getObject("isActive", Boolean.class));
        row.setCreatedAt(rs.getObject("createdAt", LocalDateTime.class));
        row.setUpdatedAt(rs.getObject("updatedAt", LocalDateTime.class));
        return row;
    };

    private static final String SELECT_COLUMNS = """
            SELECT
                l.labour_entry_id AS "labourEntryId",
                l.employee_code AS "employeeCode",
                e.full_name AS "employeeName",
                l.project_code AS "projectCode",
                p.projectname AS "projectName",
                l.work_date AS "workDate",
                l.start_time AS "startTime",
                l.end_time AS "endTime",
                l.hours AS "hours",
                l.work_description AS "workDescription",
                l.recorded_by AS "recordedBy",
                l.is_active AS "isActive",
                l.created_at AS "createdAt",
                l.updated_at AS "updatedAt"
            FROM labour_entry l
            JOIN employee e ON e.employee_code = l.employee_code
            JOIN project p ON p.project_code = l.project_code
            """;

    // POST
    public void create(LabourEntry entry) {

        String sql = """
                INSERT INTO labour_entry (
                    labour_entry_id, employee_code, project_code, work_date, start_time, end_time,
                    hours, work_description, recorded_by, created_at, updated_at, is_active
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE)
                """;

        entry.setLabourEntryId(UUID.randomUUID());

        jdbcTemplate.update(
                sql,
                entry.getLabourEntryId(),
                entry.getEmployeeCode(),
                entry.getProjectCode(),
                entry.getWorkDate(),
                entry.getStartTime(),
                entry.getEndTime(),
                entry.getHours(),
                entry.getWorkDescription(),
                entry.getRecordedBy()
        );
    }

    // GET — every filter is optional; from/to are inclusive.
    public List<LabourEntry> search(LocalDate from, LocalDate to, String projectCode, String employeeCode) {

        StringBuilder sql = new StringBuilder(SELECT_COLUMNS).append(" WHERE l.is_active = TRUE");
        List<Object> args = new ArrayList<>();

        if (from != null) {
            sql.append(" AND l.work_date >= ?");
            args.add(from);
        }
        if (to != null) {
            sql.append(" AND l.work_date <= ?");
            args.add(to);
        }
        if (projectCode != null && !projectCode.isBlank()) {
            sql.append(" AND l.project_code = ?");
            args.add(projectCode);
        }
        if (employeeCode != null && !employeeCode.isBlank()) {
            sql.append(" AND l.employee_code = ?");
            args.add(employeeCode);
        }
        sql.append(" ORDER BY l.work_date DESC, e.full_name, l.start_time NULLS LAST, l.created_at");

        return jdbcTemplate.query(sql.toString(), ROW_MAPPER, args.toArray());
    }

    // Hours already booked for this employee on a day, optionally ignoring one entry (when editing).
    public BigDecimal sumHoursForEmployeeOnDate(String employeeCode, LocalDate workDate, UUID excludeId) {

        String sql = """
                SELECT COALESCE(SUM(hours), 0)
                FROM labour_entry
                WHERE employee_code = ?
                  AND work_date = ?
                  AND is_active = TRUE
                  AND (?::uuid IS NULL OR labour_entry_id <> ?::uuid)
                """;

        BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class, employeeCode, workDate, excludeId, excludeId);
        return total == null ? BigDecimal.ZERO : total;
    }

    // PUT — the employee and date stay fixed; the project, times, hours and description can change.
    public int update(UUID labourEntryId, LabourEntry entry) {

        String sql = """
                UPDATE labour_entry
                SET
                    project_code = ?,
                    start_time = ?,
                    end_time = ?,
                    hours = ?,
                    work_description = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE labour_entry_id = ?
                  AND is_active = TRUE
                """;

        return jdbcTemplate.update(
                sql,
                entry.getProjectCode(),
                entry.getStartTime(),
                entry.getEndTime(),
                entry.getHours(),
                entry.getWorkDescription(),
                labourEntryId
        );
    }

    public LabourEntry findById(UUID labourEntryId) {

        List<LabourEntry> results = jdbcTemplate.query(
                SELECT_COLUMNS + " WHERE l.labour_entry_id = ? AND l.is_active = TRUE", ROW_MAPPER, labourEntryId);

        return results.isEmpty() ? null : results.get(0);
    }

    // SOFT DELETE
    public int delete(UUID labourEntryId) {

        String sql = """
                UPDATE labour_entry
                SET
                    is_active = FALSE,
                    updated_at = CURRENT_TIMESTAMP
                WHERE labour_entry_id = ?
                """;

        return jdbcTemplate.update(sql, labourEntryId);
    }
}
