package com.rr.erp.repository;

import com.rr.erp.entity.ProjectPhase;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProjectPhaseRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProjectPhaseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // POST
    public Integer createProjectPhase(ProjectPhase projectPhase) {

        String sql = """
                INSERT INTO project_phase (
                    project_id,
                    parent_project_phase_id,
                    project_phase_code,
                    project_phase_name,
                    description,
                    start_date,
                    end_date,
                    status_id
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING project_phase_id
                """;

        return jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                projectPhase.getProjectId(),
                projectPhase.getParentProjectPhaseId(),
                projectPhase.getProjectPhaseCode(),
                projectPhase.getProjectPhaseName(),
                projectPhase.getDescription(),
                projectPhase.getStartDate(),
                projectPhase.getEndDate(),
                projectPhase.getStatusId()
        );
    }


    // GET by Project ID
    public List<ProjectPhase> getProjectPhasesByProjectId(Integer projectId) {

        String sql = """
                SELECT
                    project_phase_id AS "projectPhaseId",
                    project_id AS "projectId",
                    parent_project_phase_id AS "parentProjectPhaseId",
                    project_phase_code AS "projectPhaseCode",
                    project_phase_name AS "projectPhaseName",
                    description AS "description",
                    start_date AS "startDate",
                    end_date AS "endDate",
                    status_id AS "statusId"
                FROM project_phase
                WHERE project_id = ?
                ORDER BY start_date ASC, project_phase_id ASC
                """;

        return jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(ProjectPhase.class),
                projectId
        );
    }


    // GET ALL
    public List<ProjectPhase> getAllProjectPhases() {

        String sql = """
                SELECT
                    project_phase_id AS "projectPhaseId",
                    project_id AS "projectId",
                    parent_project_phase_id AS "parentProjectPhaseId",
                    project_phase_code AS "projectPhaseCode",
                    project_phase_name AS "projectPhaseName",
                    description AS "description",
                    start_date AS "startDate",
                    end_date AS "endDate",
                    status_id AS "statusId"
                FROM project_phase
                ORDER BY project_id ASC, start_date ASC, project_phase_id ASC
                """;

        return jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(ProjectPhase.class)
        );
    }


    // PUT
    public int updateProjectPhase(
            Integer projectPhaseId,
            ProjectPhase projectPhase
    ) {

        String sql = """
                UPDATE project_phase
                SET
                    project_id = ?,
                    parent_project_phase_id = ?,
                    project_phase_code = ?,
                    project_phase_name = ?,
                    description = ?,
                    start_date = ?,
                    end_date = ?,
                    status_id = ?
                WHERE project_phase_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                projectPhase.getProjectId(),
                projectPhase.getParentProjectPhaseId(),
                projectPhase.getProjectPhaseCode(),
                projectPhase.getProjectPhaseName(),
                projectPhase.getDescription(),
                projectPhase.getStartDate(),
                projectPhase.getEndDate(),
                projectPhase.getStatusId(),
                projectPhaseId
        );
    }


    // Used to check whether ProjectPhase exists
    public boolean existsById(Integer projectPhaseId) {

        String sql = """
                SELECT COUNT(*)
                FROM project_phase
                WHERE project_phase_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                projectPhaseId
        );

        return count != null && count > 0;
    }


    // DELETE
    public int deleteProjectPhase(Integer projectPhaseId) {

        String sql = """
                DELETE FROM project_phase
                WHERE project_phase_id = ?
                """;

        return jdbcTemplate.update(sql, projectPhaseId);
    }
}