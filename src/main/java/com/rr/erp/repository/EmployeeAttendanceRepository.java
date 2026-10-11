package com.rr.erp.repository;

import com.rr.erp.entity.EmployeeAttendance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class EmployeeAttendanceRepository {

    private final JdbcTemplate jdbcTemplate;

    public EmployeeAttendanceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<EmployeeAttendance> ROW_MAPPER = (rs, rowNum) -> {
        EmployeeAttendance row = new EmployeeAttendance();
        row.setAttendanceId(rs.getObject("attendanceId", UUID.class));
        row.setEmployeeCode(rs.getString("employeeCode"));
        row.setEmployeeName(rs.getString("employeeName"));
        row.setDepartment(rs.getString("department"));
        row.setAttendanceDate(rs.getObject("attendanceDate", LocalDate.class));
        row.setStatus(rs.getString("status"));
        row.setCheckInTime(rs.getObject("checkInTime", LocalTime.class));
        row.setCheckOutTime(rs.getObject("checkOutTime", LocalTime.class));
        row.setRemarks(rs.getString("remarks"));
        row.setRecordedBy(rs.getString("recordedBy"));
        row.setIsActive(rs.getObject("isActive", Boolean.class));
        row.setCreatedAt(rs.getObject("createdAt", LocalDateTime.class));
        row.setUpdatedAt(rs.getObject("updatedAt", LocalDateTime.class));
        return row;
    };

    private static final String SELECT_COLUMNS = """
            SELECT
                a.attendance_id AS "attendanceId",
                a.employee_code AS "employeeCode",
                e.full_name AS "employeeName",
                e.department AS "department",
                a.attendance_date AS "attendanceDate",
                a.status AS "status",
                a.check_in_time AS "checkInTime",
                a.check_out_time AS "checkOutTime",
                a.remarks AS "remarks",
                a.recorded_by AS "recordedBy",
                a.is_active AS "isActive",
                a.created_at AS "createdAt",
                a.updated_at AS "updatedAt"
            FROM employee_attendance a
            JOIN employee e ON e.employee_code = a.employee_code
            """;

    // POST
    public void create(EmployeeAttendance record) {

        String sql = """
                INSERT INTO employee_attendance (
                    attendance_id, employee_code, attendance_date, status, check_in_time, check_out_time,
                    remarks, recorded_by, created_at, updated_at, is_active
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE)
                """;

        record.setAttendanceId(UUID.randomUUID());

        jdbcTemplate.update(
                sql,
                record.getAttendanceId(),
                record.getEmployeeCode(),
                record.getAttendanceDate(),
                record.getStatus(),
                record.getCheckInTime(),
                record.getCheckOutTime(),
                record.getRemarks(),
                record.getRecordedBy()
        );
    }

    // GET — every filter is optional; from/to are inclusive.
    public List<EmployeeAttendance> search(LocalDate from, LocalDate to, String employeeCode, String status) {

        StringBuilder sql = new StringBuilder(SELECT_COLUMNS).append(" WHERE a.is_active = TRUE");
        List<Object> args = new ArrayList<>();

        if (from != null) {
            sql.append(" AND a.attendance_date >= ?");
            args.add(from);
        }
        if (to != null) {
            sql.append(" AND a.attendance_date <= ?");
            args.add(to);
        }
        if (employeeCode != null && !employeeCode.isBlank()) {
            sql.append(" AND a.employee_code = ?");
            args.add(employeeCode);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND a.status = ?");
            args.add(status);
        }
        sql.append(" ORDER BY a.attendance_date DESC, e.full_name");

        return jdbcTemplate.query(sql.toString(), ROW_MAPPER, args.toArray());
    }

    public EmployeeAttendance findById(UUID attendanceId) {

        List<EmployeeAttendance> results = jdbcTemplate.query(
                SELECT_COLUMNS + " WHERE a.attendance_id = ? AND a.is_active = TRUE", ROW_MAPPER, attendanceId);

        return results.isEmpty() ? null : results.get(0);
    }

    public boolean existsForEmployeeOnDate(String employeeCode, LocalDate attendanceDate) {

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM employee_attendance WHERE employee_code = ? AND attendance_date = ? AND is_active = TRUE",
                Integer.class, employeeCode, attendanceDate);
        return count != null && count > 0;
    }

    // PUT — the employee and date stay fixed; status, times and remarks can change.
    public int update(UUID attendanceId, EmployeeAttendance record) {

        String sql = """
                UPDATE employee_attendance
                SET
                    status = ?,
                    check_in_time = ?,
                    check_out_time = ?,
                    remarks = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE attendance_id = ?
                  AND is_active = TRUE
                """;

        return jdbcTemplate.update(
                sql,
                record.getStatus(),
                record.getCheckInTime(),
                record.getCheckOutTime(),
                record.getRemarks(),
                attendanceId
        );
    }

    // SOFT DELETE
    public int delete(UUID attendanceId) {

        String sql = """
                UPDATE employee_attendance
                SET
                    is_active = FALSE,
                    updated_at = CURRENT_TIMESTAMP
                WHERE attendance_id = ?
                """;

        return jdbcTemplate.update(sql, attendanceId);
    }
}
