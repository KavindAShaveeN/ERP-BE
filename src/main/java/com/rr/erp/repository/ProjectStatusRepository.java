package com.rr.erp.repository;

import com.rr.erp.entity.ProjectStatus;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProjectStatusRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProjectStatusRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ProjectStatus> getAllProjectStatuses() {

        String sql = """
                SELECT
                    projectStatusId AS "projectStatusId",
                    projectStatusName AS "projectStatusName"
                FROM projectStatus
                ORDER BY projectStatusName
                """;

        return jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(ProjectStatus.class)
        );
    }
}
