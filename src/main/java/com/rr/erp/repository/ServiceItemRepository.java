package com.rr.erp.repository;

import com.rr.erp.entity.ServiceItem;
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
public class ServiceItemRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ServiceItemRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long addServiceItem(ServiceItem item) {

        String sql = """
                INSERT INTO service_item (
                    item_code_id,
                    is_active,
                    remarks
                )
                VALUES (
                    :itemCodeId,
                    :isActive,
                    :remarks
                )
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("itemCodeId", item.getItemCodeId())
                .addValue("isActive", item.getIsActive())
                .addValue("remarks", item.getRemarks());
        jdbcTemplate.update(sql, parameters, keyHolder, new String[] { "service_item_id" });

        return keyHolder.getKey().longValue();
    }

    public boolean existsById(Long id) {

        String sql = """
                SELECT COUNT(*)
                FROM service_item
                WHERE service_item_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByItemCodeId(Long itemCodeId) {

        String sql = """
                SELECT COUNT(*)
                FROM service_item
                WHERE item_code_id = :itemCodeId
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("itemCodeId", itemCodeId);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    /**
     * Whether the given item code (e.g. a GRN line's item_code) is a registered Service item --
     * used by GRNService to route GRN lines away from the quantity stock engine. Joins on
     * item_code rather than requiring the caller to resolve item_code_id first.
     */
    public boolean existsByItemCodeCode(String itemCodeCode) {

        String sql = """
                SELECT COUNT(*)
                FROM service_item si
                JOIN item_code ic ON ic.item_code_id = si.item_code_id
                WHERE ic.item_code_code = :code
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("code", itemCodeCode);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByItemCodeIdExcludingId(Long itemCodeId, Long excludeId) {

        String sql = """
                SELECT COUNT(*)
                FROM service_item
                WHERE item_code_id = :itemCodeId AND service_item_id <> :excludeId
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("itemCodeId", itemCodeId)
                .addValue("excludeId", excludeId);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public List<ServiceItem> getAllServiceItems() {

        String sql = """
            SELECT
                service_item_id AS "serviceItemId",
                item_code_id AS "itemCodeId",
                is_active AS "isActive",
                remarks AS "remarks"
            FROM service_item
            ORDER BY service_item_id
            """ ;

        return jdbcTemplate.query(
                sql,
                Collections.emptyMap(),
                new BeanPropertyRowMapper<>(ServiceItem.class)
        );
    }

    public Optional<ServiceItem> getServiceItemById(Long id) {

        String sql = """
            SELECT
                service_item_id AS "serviceItemId",
                item_code_id AS "itemCodeId",
                is_active AS "isActive",
                remarks AS "remarks"
            FROM service_item
            WHERE service_item_id = :id
            """ ;

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);

        List<ServiceItem> results = jdbcTemplate.query(
                sql,
                parameters,
                new BeanPropertyRowMapper<>(ServiceItem.class)
        );

        return results.stream().findFirst();
    }

    public int updateServiceItem(Long id, ServiceItem item) {

        String sql = """
                UPDATE service_item
                SET
                    item_code_id = :itemCodeId,
                    is_active = :isActive,
                    remarks = :remarks
                WHERE service_item_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("itemCodeId", item.getItemCodeId())
                .addValue("isActive", item.getIsActive())
                .addValue("remarks", item.getRemarks())
                .addValue("id", id);

        return jdbcTemplate.update(sql, parameters);
    }

    public int deleteById(Long id) {

        String sql = """
                DELETE FROM service_item
                WHERE service_item_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);

        return jdbcTemplate.update(sql, parameters);
    }
}
