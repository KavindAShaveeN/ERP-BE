package com.rr.erp.repository;

import com.rr.erp.dto.AssignedProject;
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


    // Get projects assigned to employee
    public List<AssignedProject> getAssignedProjects(String employeeCode) {

        String sql = """
            SELECT
                p.project_code AS "projectCode",
                p.projectName AS "projectName",
                p.project_type_id AS "projectTypeId",
                pt.project_type_name AS "projectTypeName"
            FROM assigned_projects ap
            LEFT JOIN project p
                ON p.projectId = ap.project_id
            LEFT JOIN project_type pt
                ON pt.project_type_id = p.project_type_id
            WHERE ap.employee_code = ?
            ORDER BY p.projectId
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