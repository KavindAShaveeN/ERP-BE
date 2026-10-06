package com.rr.erp.repository;

import com.rr.erp.entity.NonStockItem;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class NonStockItemRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public NonStockItemRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long addNonStockItem(NonStockItem item) {

        String sql = """
                INSERT INTO non_stock_item (
                    item_code_id,
                    uom_id,
                    is_active,
                    remarks
                )
                VALUES (
                    :itemCodeId,
                    :uomId,
                    :isActive,
                    :remarks
                )
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("itemCodeId", item.getItemCodeId())
                .addValue("uomId", item.getUomId())
                .addValue("isActive", item.getIsActive())
                .addValue("remarks", item.getRemarks());
        jdbcTemplate.update(sql, parameters, keyHolder, new String[] { "non_stock_item_id" });

        return keyHolder.getKey().longValue();
    }

    public boolean existsById(Long id) {

        String sql = """
                SELECT COUNT(*)
                FROM non_stock_item
                WHERE non_stock_item_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByItemCodeId(Long itemCodeId) {

        String sql = """
                SELECT COUNT(*)
                FROM non_stock_item
                WHERE item_code_id = :itemCodeId
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("itemCodeId", itemCodeId);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByItemCodeIdExcludingId(Long itemCodeId, Long excludeId) {

        String sql = """
                SELECT COUNT(*)
                FROM non_stock_item
                WHERE item_code_id = :itemCodeId AND non_stock_item_id <> :excludeId
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("itemCodeId", itemCodeId)
                .addValue("excludeId", excludeId);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public List<NonStockItem> getAllNonStockItems() {

        String sql = """
            SELECT
                non_stock_item_id AS "nonStockItemId",
                item_code_id AS "itemCodeId",
                uom_id AS "uomId",
                is_active AS "isActive",
                remarks AS "remarks"
            FROM non_stock_item
            ORDER BY non_stock_item_id
            """ ;

        return jdbcTemplate.query(
                sql,
                Collections.emptyMap(),
                new BeanPropertyRowMapper<>(NonStockItem.class)
        );
    }

    public Optional<NonStockItem> getNonStockItemById(Long id) {

        String sql = """
            SELECT
                non_stock_item_id AS "nonStockItemId",
                item_code_id AS "itemCodeId",
                uom_id AS "uomId",
                is_active AS "isActive",
                remarks AS "remarks"
            FROM non_stock_item
            WHERE non_stock_item_id = :id
            """ ;

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);

        List<NonStockItem> results = jdbcTemplate.query(
                sql,
                parameters,
                new BeanPropertyRowMapper<>(NonStockItem.class)
        );

        return results.stream().findFirst();
    }

    public int updateNonStockItem(Long id, NonStockItem item) {

        String sql = """
                UPDATE non_stock_item
                SET
                    item_code_id = :itemCodeId,
                    uom_id = :uomId,
                    is_active = :isActive,
                    remarks = :remarks
                WHERE non_stock_item_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("itemCodeId", item.getItemCodeId())
                .addValue("uomId", item.getUomId())
                .addValue("isActive", item.getIsActive())
                .addValue("remarks", item.getRemarks())
                .addValue("id", id);

        return jdbcTemplate.update(sql, parameters);
    }

    public int deleteById(Long id) {

        String sql = """
                DELETE FROM non_stock_item
                WHERE non_stock_item_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);

        return jdbcTemplate.update(sql, parameters);
    }
}
