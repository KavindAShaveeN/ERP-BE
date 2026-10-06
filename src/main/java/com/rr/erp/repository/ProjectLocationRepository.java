package com.rr.erp.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ProjectLocationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ProjectLocationRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void deleteLocationsByProjectId(Integer projectId) {
        jdbcTemplate.update(
                "DELETE FROM project_location WHERE project_id = :projectId",
                new MapSqlParameterSource("projectId", projectId)
        );
    }

    public void insertLocations(Integer projectId, List<String> locations) {
        if (locations == null || locations.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO project_location (project_id, location) VALUES (:projectId, :location)";

        SqlParameterSource[] batchParams = locations.stream()
                .filter(location -> location != null && !location.isBlank())
                .map(location -> (SqlParameterSource) new MapSqlParameterSource()
                        .addValue("projectId", projectId)
                        .addValue("location", location.trim()))
                .toArray(SqlParameterSource[]::new);

        if (batchParams.length > 0) {
            jdbcTemplate.batchUpdate(sql, batchParams);
        }
    }

    public List<String> getLocationsByProjectId(Integer projectId) {
        String sql = "SELECT location FROM project_location WHERE project_id = :projectId ORDER BY project_location_id";
        return jdbcTemplate.queryForList(sql, new MapSqlParameterSource("projectId", projectId), String.class);
    }

    /** All project locations, grouped by project_id, in one round trip — avoids N+1
     * queries when populating the project list endpoint. */
    public Map<Integer, List<String>> getLocationsGroupedByProjectId() {
        String sql = "SELECT project_id AS \"projectId\", location AS \"location\" FROM project_location ORDER BY project_location_id";

        Map<Integer, List<String>> grouped = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            Integer projectId = rs.getInt("projectId");
            String location = rs.getString("location");
            grouped.computeIfAbsent(projectId, key -> new ArrayList<>()).add(location);
        });
        return grouped;
    }
}
