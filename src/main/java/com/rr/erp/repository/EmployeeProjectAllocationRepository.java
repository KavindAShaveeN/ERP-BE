package com.rr.erp.repository;

import com.rr.erp.entity.EmployeeProjectAllocation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class EmployeeProjectAllocationRepository {

    private final JdbcTemplate jdbcTemplate;

    public EmployeeProjectAllocationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<EmployeeProjectAllocation> ROW_MAPPER = (rs, rowNum) -> {
        EmployeeProjectAllocation row = new EmployeeProjectAllocation();
        row.setAllocationId(rs.getObject("allocationId", UUID.class));
        row.setEmployeeCode(rs.getString("employeeCode"));
        row.setEmployeeName(rs.getString("employeeName"));
        row.setDepartment(rs.getString("department"));
        row.setProjectId(rs.getObject("projectId", Integer.class));
        row.setProjectCode(rs.getString("projectCode"));
        row.setProjectName(rs.getString("projectName"));
        row.setWorkDate(rs.getObject("workDate", LocalDate.class));
        row.setHours(rs.getBigDecimal("hours"));
        row.setRemarks(rs.getString("remarks"));
        row.setAssignedBy(rs.getString("assignedBy"));
        row.setCreatedAt(rs.getObject("createdAt", LocalDateTime.class));
        row.setUpdatedAt(rs.getObject("updatedAt", LocalDateTime.class));
        return row;
    };

    private static final String SELECT_COLUMNS = """
            SELECT
                a.allocation_id AS "allocationId",
                a.employee_code AS "employeeCode",
                e.full_name AS "employeeName",
                e.department AS "department",
                a.project_id AS "projectId",
                p.project_code AS "projectCode",
                p.projectname AS "projectName",
                a.work_date AS "workDate",
                a.hours AS "hours",
                a.remarks AS "remarks",
                a.assigned_by AS "assignedBy",
                a.created_at AS "createdAt",
                a.updated_at AS "updatedAt"
            FROM employee_project_allocation a
            JOIN employee e ON e.employee_code = a.employee_code
            JOIN project p ON p.projectid = a.project_id
            """;

    // GET — every filter is optional; from/to are inclusive.
    public List<EmployeeProjectAllocation> search(LocalDate from, LocalDate to, String employeeCode, Integer projectId) {

        StringBuilder sql = new StringBuilder(SELECT_COLUMNS).append(" WHERE 1 = 1");
        List<Object> args = new ArrayList<>();

        if (from != null) {
            sql.append(" AND a.work_date >= ?");
            args.add(from);
        }
        if (to != null) {
            sql.append(" AND a.work_date <= ?");
            args.add(to);
        }
        if (employeeCode != null && !employeeCode.isBlank()) {
            sql.append(" AND a.employee_code = ?");
            args.add(employeeCode);
        }
        if (projectId != null) {
            sql.append(" AND a.project_id = ?");
            args.add(projectId);
        }
        sql.append(" ORDER BY a.work_date DESC, e.full_name, p.project_code");

        return jdbcTemplate.query(sql.toString(), ROW_MAPPER, args.toArray());
    }

    public int deleteForEmployeeOnDate(String employeeCode, LocalDate workDate) {
        return jdbcTemplate.update(
                "DELETE FROM employee_project_allocation WHERE employee_code = ? AND work_date = ?",
                employeeCode, workDate);
    }

    public void insert(String employeeCode, Integer projectId, LocalDate workDate, BigDecimal hours,
                       String remarks, String assignedBy) {

        jdbcTemplate.update("""
                INSERT INTO employee_project_allocation (
                    allocation_id, employee_code, project_id, work_date, hours, remarks, assigned_by,
                    created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                UUID.randomUUID(), employeeCode, projectId, workDate, hours, remarks, assignedBy);
    }

    public int delete(UUID allocationId) {
        return jdbcTemplate.update("DELETE FROM employee_project_allocation WHERE allocation_id = ?", allocationId);
    }
}
