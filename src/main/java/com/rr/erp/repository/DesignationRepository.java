package com.rr.erp.repository;

import com.rr.erp.entity.Designation;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DesignationRepository {

    private final JdbcTemplate jdbcTemplate;

    public DesignationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Designation> getAllDesignations() {

        String sql = """
                SELECT
                    designationId AS "designationId",
                    designationName AS "designationName"
                FROM designation
                ORDER BY designationName
                """;

        return jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(Designation.class)
        );
    }
}