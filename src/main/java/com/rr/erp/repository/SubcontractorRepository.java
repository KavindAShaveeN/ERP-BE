package com.rr.erp.repository;

import com.rr.erp.entity.Subcontractor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SubcontractorRepository {

    private final JdbcTemplate jdbcTemplate;

    public SubcontractorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SELECT_COLUMNS = """
            SELECT
                subcontractor_id AS "subcontractorId",
                subcontractor_name AS "subcontractorName",
                phone_number AS "phoneNumber",
                email AS "email",
                line1 AS "line1",
                line2 AS "line2",
                city AS "city",
                district AS "district",
                is_active AS "isActive"
            FROM subcontractor
            """;

    // POST
    public Integer createSubcontractor(Subcontractor subcontractor) {

        String sql = """
                INSERT INTO subcontractor (
                    subcontractor_name,
                    phone_number,
                    email,
                    line1,
                    line2,
                    city,
                    district,
                    is_active
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING subcontractor_id
                """;

        return jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                subcontractor.getSubcontractorName(),
                subcontractor.getPhoneNumber(),
                subcontractor.getEmail(),
                subcontractor.getLine1(),
                subcontractor.getLine2(),
                subcontractor.getCity(),
                subcontractor.getDistrict(),
                subcontractor.getIsActive()
        );
    }


    // GET ALL
    public List<Subcontractor> getAllSubcontractors() {

        String sql = SELECT_COLUMNS + " ORDER BY subcontractor_id ASC";

        List<Subcontractor> subcontractors = jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(Subcontractor.class)
        );

        for (Subcontractor subcontractor : subcontractors) {
            subcontractor.setProjectCodes(
                    getProjectCodesForSubcontractor(subcontractor.getSubcontractorId())
            );
        }

        return subcontractors;
    }


    // GET by Project Code
    public List<Subcontractor> getSubcontractorsByProjectCode(String projectCode) {

        String sql = """
                SELECT
                    s.subcontractor_id AS "subcontractorId",
                    s.subcontractor_name AS "subcontractorName",
                    s.phone_number AS "phoneNumber",
                    s.email AS "email",
                    s.line1 AS "line1",
                    s.line2 AS "line2",
                    s.city AS "city",
                    s.district AS "district",
                    s.is_active AS "isActive"
                FROM subcontractor s
                INNER JOIN subcontractor_project sp
                    ON sp.subcontractor_id = s.subcontractor_id
                WHERE sp.project_code = ?
                ORDER BY s.subcontractor_id ASC
                """;

        List<Subcontractor> subcontractors = jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(Subcontractor.class),
                projectCode
        );

        for (Subcontractor subcontractor : subcontractors) {
            subcontractor.setProjectCodes(
                    getProjectCodesForSubcontractor(subcontractor.getSubcontractorId())
            );
        }

        return subcontractors;
    }


    // GET by ID
    public Optional<Subcontractor> getSubcontractorById(Integer subcontractorId) {

        String sql = SELECT_COLUMNS + " WHERE subcontractor_id = ?";

        List<Subcontractor> results = jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(Subcontractor.class),
                subcontractorId
        );

        Optional<Subcontractor> subcontractor = results.stream().findFirst();

        subcontractor.ifPresent(value ->
                value.setProjectCodes(getProjectCodesForSubcontractor(subcontractorId))
        );

        return subcontractor;
    }


    // PUT
    public int updateSubcontractor(Integer subcontractorId, Subcontractor subcontractor) {

        String sql = """
                UPDATE subcontractor
                SET
                    subcontractor_name = ?,
                    phone_number = ?,
                    email = ?,
                    line1 = ?,
                    line2 = ?,
                    city = ?,
                    district = ?,
                    is_active = ?
                WHERE subcontractor_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                subcontractor.getSubcontractorName(),
                subcontractor.getPhoneNumber(),
                subcontractor.getEmail(),
                subcontractor.getLine1(),
                subcontractor.getLine2(),
                subcontractor.getCity(),
                subcontractor.getDistrict(),
                subcontractor.getIsActive(),
                subcontractorId
        );
    }


    // Used to check whether Subcontractor exists
    public boolean existsById(Integer subcontractorId) {

        String sql = """
                SELECT COUNT(*)
                FROM subcontractor
                WHERE subcontractor_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                subcontractorId
        );

        return count != null && count > 0;
    }


    // DELETE
    public int deleteSubcontractor(Integer subcontractorId) {

        String sql = """
                DELETE FROM subcontractor
                WHERE subcontractor_id = ?
                """;

        return jdbcTemplate.update(sql, subcontractorId);
    }


    // =========================================================
    // PROJECT ASSIGNMENTS (subcontractor <-> project, many-to-many)
    // =========================================================

    public List<String> getProjectCodesForSubcontractor(Integer subcontractorId) {

        String sql = """
                SELECT project_code
                FROM subcontractor_project
                WHERE subcontractor_id = ?
                ORDER BY project_code
                """;

        return jdbcTemplate.queryForList(sql, String.class, subcontractorId);
    }

    public int addProjectAssignment(Integer subcontractorId, String projectCode) {

        String sql = """
                INSERT INTO subcontractor_project (
                    subcontractor_id,
                    project_code
                )
                VALUES (?, ?)
                ON CONFLICT DO NOTHING
                """;

        return jdbcTemplate.update(sql, subcontractorId, projectCode);
    }

    public int deleteProjectAssignments(Integer subcontractorId) {

        String sql = """
                DELETE FROM subcontractor_project
                WHERE subcontractor_id = ?
                """;

        return jdbcTemplate.update(sql, subcontractorId);
    }
}
