package com.rr.erp.repository;

import com.rr.erp.entity.ItemType;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class ItemTypeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ItemTypeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ItemType> getAllItemTypes() {

        String sql = """
                SELECT
                    item_type_id AS "itemTypeId",
                    item_type_name AS "itemTypeName"
                FROM item_type
                ORDER BY item_type_id
                """;

        return jdbcTemplate.query(
                sql,
                Collections.emptyMap(),
                new BeanPropertyRowMapper<>(ItemType.class)
        );
    }

    public boolean existsById(Integer id) {

        String sql = """
                SELECT COUNT(*)
                FROM item_type
                WHERE item_type_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }
}
