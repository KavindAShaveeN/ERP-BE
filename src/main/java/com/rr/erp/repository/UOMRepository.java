package com.rr.erp.repository;

import com.rr.erp.entity.UOM;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UOMRepository {

    private final JdbcTemplate jdbcTemplate;

    public UOMRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<UOM> getAllUom() {

        String sql = """
                SELECT
                    uom_id AS "uomId",
                    uom_name AS "uomName"
                FROM uom
                ORDER BY uom_name
                """;

        return jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(UOM.class)
        );
    }

    public boolean existsById(Integer uomId) {

        String sql = """
                SELECT COUNT(*)
                FROM uom
                WHERE uom_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, uomId);

        return count != null && count > 0;
    }
}