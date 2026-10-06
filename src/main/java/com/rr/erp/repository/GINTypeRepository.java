package com.rr.erp.repository;

import com.rr.erp.entity.GINType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GINTypeRepository {

    private final JdbcTemplate jdbcTemplate;

    public GINTypeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<GINType> getAllGinTypes() {

        String sql = """
                SELECT gin_type_id,
                       gin_type_name
                FROM gin_type
                ORDER BY gin_type_id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            GINType ginType = new GINType();

            ginType.setGinTypeId(rs.getInt("gin_type_id"));
            ginType.setGinTypeName(rs.getString("gin_type_name"));

            return ginType;
        });
    }
}