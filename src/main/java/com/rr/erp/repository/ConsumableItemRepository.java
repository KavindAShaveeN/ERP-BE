package com.rr.erp.repository;

import com.rr.erp.entity.ConsumableItem;
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
public class ConsumableItemRepository {


    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ConsumableItemRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long addConsumableItem(ConsumableItem item) {

        String sql = """
                INSERT INTO consumable_item (
                    item_code_id,
                    uom_id,
                    unit_price,
                    current_stock,
                    is_active,
                    remarks,
                    part_number
                )
                VALUES (
                    :itemCodeId,
                    :uomId,
                    :unitPrice,
                    :currentStock,
                    :isActive,
                    :remarks,
                    :partNumber
                )
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("itemCodeId", item.getItemCodeId())
                .addValue("uomId", item.getUomId())
                .addValue("unitPrice", item.getUnitPrice())
                .addValue("currentStock", item.getCurrentStock())
                .addValue("isActive", item.getIsActive())
                .addValue("remarks", item.getRemarks())
                .addValue("partNumber", item.getPartNumber());
        jdbcTemplate.update(sql, parameters, keyHolder, new String[] { "consumable_item_id" });

        return keyHolder.getKey().longValue();
    }

    public boolean existsById(Long id) {

        String sql = """
                SELECT COUNT(*)
                FROM consumable_item
                WHERE consumable_item_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByItemCodeId(Long itemCodeId) {

        String sql = """
                SELECT COUNT(*)
                FROM consumable_item
                WHERE item_code_id = :itemCodeId
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("itemCodeId", itemCodeId);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByItemCodeIdExcludingId(Long itemCodeId, Long excludeId) {

        String sql = """
                SELECT COUNT(*)
                FROM consumable_item
                WHERE item_code_id = :itemCodeId AND consumable_item_id <> :excludeId
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("itemCodeId", itemCodeId)
                .addValue("excludeId", excludeId);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public List<ConsumableItem> getAllConsumableItems() {

        String sql = """
            SELECT
                consumable_item_id AS "consumableItemId",
                item_code_id AS "itemCodeId",
                uom_id AS "uomId",
                unit_price AS "unitPrice",
                current_stock AS "currentStock",
                is_active AS "isActive",
                remarks AS "remarks",
                part_number AS "partNumber"
            FROM consumable_item
            ORDER BY consumable_item_id
            """ ;

        return jdbcTemplate.query(
                sql,
                Collections.emptyMap(),
                new BeanPropertyRowMapper<>(ConsumableItem.class)
        );
    }

    public Optional<ConsumableItem> getConsumableItemById(Long id) {

        String sql = """
            SELECT
                consumable_item_id AS "consumableItemId",
                item_code_id AS "itemCodeId",
                uom_id AS "uomId",
                unit_price AS "unitPrice",
                current_stock AS "currentStock",
                is_active AS "isActive",
                remarks AS "remarks",
                part_number AS "partNumber"
            FROM consumable_item
            WHERE consumable_item_id = :id
            """ ;

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);

        List<ConsumableItem> results = jdbcTemplate.query(
                sql,
                parameters,
                new BeanPropertyRowMapper<>(ConsumableItem.class)
        );

        return results.stream().findFirst();
    }

    public int updateConsumableItem(Long id, ConsumableItem item) {

        String sql = """
                UPDATE consumable_item
                SET
                    item_code_id = :itemCodeId,
                    uom_id = :uomId,
                    unit_price = :unitPrice,
                    current_stock = :currentStock,
                    is_active = :isActive,
                    remarks = :remarks,
                    part_number = :partNumber
                WHERE consumable_item_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("itemCodeId", item.getItemCodeId())
                .addValue("uomId", item.getUomId())
                .addValue("unitPrice", item.getUnitPrice())
                .addValue("currentStock", item.getCurrentStock())
                .addValue("isActive", item.getIsActive())
                .addValue("remarks", item.getRemarks())
                .addValue("partNumber", item.getPartNumber())
                .addValue("id", id);

        return jdbcTemplate.update(sql, parameters);
    }

    public int deleteById(Long id) {

        String sql = """
                DELETE FROM consumable_item
                WHERE consumable_item_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);

        return jdbcTemplate.update(sql, parameters);
    }
}
