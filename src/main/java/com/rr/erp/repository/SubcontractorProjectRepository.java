package com.rr.erp.repository;

import com.rr.erp.entity.SubcontractorProjectAssignment;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SubcontractorProjectRepository {

    private final JdbcTemplate jdbcTemplate;

    public SubcontractorProjectRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // GET by project code — subcontractors registered against this project, with their contract link.
    public List<SubcontractorProjectAssignment> getAssignmentsByProjectCode(String projectCode) {

        String sql = """
                SELECT
                    sp.subcontractor_id AS "subcontractorId",
                    sp.project_code AS "projectCode",
                    sp.contract_link AS "contractLink",
                    s.subcontractor_name AS "subcontractorName",
                    s.phone_number AS "phoneNumber",
                    s.email AS "email",
                    s.is_active AS "isActive"
                FROM subcontractor_project sp
                INNER JOIN subcontractor s
                    ON s.subcontractor_id = sp.subcontractor_id
                WHERE sp.project_code = ?
                ORDER BY s.subcontractor_name ASC
                """;

        return jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(SubcontractorProjectAssignment.class),
                projectCode
        );
    }

    // Register a subcontractor to a project, or update its contract link if already registered.
    public int assign(Integer subcontractorId, String projectCode, String contractLink) {

        String sql = """
                INSERT INTO subcontractor_project (subcontractor_id, project_code, contract_link)
                VALUES (?, ?, ?)
                ON CONFLICT (subcontractor_id, project_code)
                DO UPDATE SET contract_link = EXCLUDED.contract_link
                """;

        return jdbcTemplate.update(sql, subcontractorId, projectCode, contractLink);
    }

    public boolean existsAssignment(Integer subcontractorId, String projectCode) {

        String sql = """
                SELECT COUNT(*)
                FROM subcontractor_project
                WHERE subcontractor_id = ? AND project_code = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, subcontractorId, projectCode);
        return count != null && count > 0;
    }

    // Unregister a single subcontractor from a single project (unlike
    // SubcontractorRepository.deleteProjectAssignments, which clears ALL of a
    // subcontractor's project assignments).
    public int unassign(Integer subcontractorId, String projectCode) {

        String sql = """
                DELETE FROM subcontractor_project
                WHERE subcontractor_id = ? AND project_code = ?
                """;

        return jdbcTemplate.update(sql, subcontractorId, projectCode);
    }
}
