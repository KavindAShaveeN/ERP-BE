package com.rr.erp.repository;

import com.rr.erp.dto.AssignedProject;
import com.rr.erp.dto.UserLoginDto;
import com.rr.erp.entity.Credential;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class LoginRepository {

    private final JdbcTemplate jdbcTemplate;

    public LoginRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    // Look up a credential by username only; the caller verifies the password hash.
    public Optional<Credential> findByUserName(
            String userName
    ) {

        String sql = """
                SELECT
                    credential_id AS "credentialId",
                    employee_code AS "employeeCode",
                    userName AS "userName",
                    password AS "password"
                FROM credentials
                WHERE userName = ?
                """;

        List<Credential> credentials = jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(Credential.class),
                userName
        );

        if (credentials.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(credentials.get(0));
    }


    public boolean employeeExists(String employeeCode) {

        String sql = "SELECT COUNT(*) FROM employee WHERE employee_code = ?";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, employeeCode);

        return count != null && count > 0;
    }


    public boolean employeeHasCredential(String employeeCode) {

        String sql = "SELECT COUNT(*) FROM credentials WHERE employee_code = ?";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, employeeCode);

        return count != null && count > 0;
    }


    /** Every employee with the username of their login (null when they have none), logins last. */
    public List<UserLoginDto> findEmployeeLogins() {
        return jdbcTemplate.query("""
                SELECT e.employee_code, e.full_name, c.userName AS user_name
                FROM employee e
                LEFT JOIN credentials c ON c.employee_code = e.employee_code
                ORDER BY (c.credential_id IS NULL) DESC, e.employee_code
                """, (rs, rowNum) -> new UserLoginDto(
                rs.getString("employee_code"), rs.getString("full_name"), rs.getString("user_name")));
    }


    public boolean userNameTaken(String userName) {

        String sql = "SELECT COUNT(*) FROM credentials WHERE userName = ?";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userName);

        return count != null && count > 0;
    }


    public int insertCredential(String employeeCode, String userName, String hashedPassword) {

        String sql = """
                INSERT INTO credentials (employee_code, userName, password)
                VALUES (?, ?, ?)
                """;

        return jdbcTemplate.update(sql, employeeCode, userName, hashedPassword);
    }


    // Get projects the employee can work in: their assigned projects, or — when any assigned
    // project is a Head Quarters project — every project, so head office can switch into a
    // site and act there. Assigned projects are listed first (the first one becomes the
    // active project at login).
    public List<AssignedProject> getAssignedProjects(String employeeCode) {

        String sql = """
            SELECT
                p.project_code AS "projectCode",
                p.projectName AS "projectName",
                p.project_type_id AS "projectTypeId",
                pt.project_type_name AS "projectTypeName"
            FROM project p
            LEFT JOIN project_type pt
                ON pt.project_type_id = p.project_type_id
            WHERE p.projectId IN (
                    SELECT project_id FROM assigned_projects WHERE employee_code = ?
                )
               OR EXISTS (
                    SELECT 1
                    FROM assigned_projects ap2
                    JOIN project p2 ON p2.projectId = ap2.project_id
                    JOIN project_type pt2 ON pt2.project_type_id = p2.project_type_id
                    WHERE ap2.employee_code = ?
                      AND LOWER(TRIM(pt2.project_type_name)) = 'head quarters'
                )
            ORDER BY
                CASE WHEN p.projectId IN (
                    SELECT project_id FROM assigned_projects WHERE employee_code = ?
                ) THEN 0 ELSE 1 END,
                p.projectId
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    AssignedProject project = new AssignedProject();

                    project.setProjectCode(
                            rs.getString("projectCode")
                    );

                    project.setProjectName(
                            rs.getString("projectName")
                    );

                    int projectTypeId = rs.getInt("projectTypeId");
                    project.setProjectTypeId(
                            rs.wasNull() ? null : projectTypeId
                    );

                    project.setProjectTypeName(
                            rs.getString("projectTypeName")
                    );

                    return project;
                },
                employeeCode,
                employeeCode,
                employeeCode
        );
    }
    // Update password for a user
    public int updatePassword(String userName, String newPassword) {

        String sql = """
                UPDATE credentials
                SET password = ?
                WHERE userName = ?
                """;

        return jdbcTemplate.update(sql, newPassword, userName);
    }


    public boolean isCashManager(String employeeCode) {

        try {
            Boolean manager = jdbcTemplate.queryForObject(
                    "SELECT is_cash_manager FROM employee WHERE employee_code = ?",
                    Boolean.class,
                    employeeCode
            );
            return Boolean.TRUE.equals(manager);
        } catch (org.springframework.dao.DataAccessException e) {
            // employee_cash_manager_v1.sql has not been run yet: sign in as an ordinary employee
            return false;
        }
    }


    public String getEmployeeName(String employeeCode) {
        String sql = """
            SELECT full_name
            FROM employee
            WHERE employee_code = ?
            """;

        return jdbcTemplate.queryForObject(
                sql,
                String.class,
                employeeCode
        );
    }
}