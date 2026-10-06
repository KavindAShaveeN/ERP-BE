package com.rr.erp.repository;

import com.rr.erp.entity.ProjectType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProjectTypeRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<ProjectType> findAll() {

        String sql = """
                SELECT
                    project_type_id,
                    project_type_name
                FROM project_type
                ORDER BY project_type_name
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ProjectType projectType = new ProjectType();

            projectType.setProjectTypeId(
                    rs.getInt("project_type_id")
            );

            projectType.setProjectTypeName(
                    rs.getString("project_type_name")
            );

            return projectType;
        });
    }
}