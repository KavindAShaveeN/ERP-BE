package com.rr.erp.repository;

import com.rr.erp.entity.ItemOptionalTwo;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class ItemOptionalTwoRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ItemOptionalTwoRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long addOptionalTwo(ItemOptionalTwo optionalTwo) {

        String sql = """
                INSERT INTO item_optional2 (
                    item_optional2_code,
                    item_optional2_name,
                    item_optional1_id,
                    code_slug,
                    name_slug
                )
                VALUES (
                    :code,
                    :name,
                    :optionalOneId,
                    :codeSlug,
                    :nameSlug
                )
                """;
        String codeSlug = optionalTwo.getItemOptionalTwoCode().trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        String nameSlug = optionalTwo.getItemOptionalTwoName().trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("code", optionalTwo.getItemOptionalTwoCode())
                .addValue("name", optionalTwo.getItemOptionalTwoName())
                .addValue("optionalOneId", optionalTwo.getItemOptionalOneId())
                .addValue("codeSlug", codeSlug)
                .addValue("nameSlug", nameSlug);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, parameters, keyHolder, new String[] { "item_optional2_id" });

        return keyHolder.getKey().longValue();
    }

    public boolean existsById(Long id) {

        String sql = """
                SELECT COUNT(*)
                FROM item_optional2
                WHERE item_optional2_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByParentIdAndCode(Long optionalOneId, String code) {

        String sql = """
                SELECT COUNT(*)
                FROM item_optional2
                WHERE item_optional1_id = :optionalOneId AND item_optional2_code = :code
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("optionalOneId", optionalOneId)
                .addValue("code", code);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByParentIdAndCodeExcludingId(Long optionalOneId, String code, Long excludeId) {

        String sql = """
                SELECT COUNT(*)
                FROM item_optional2
                WHERE item_optional1_id = :optionalOneId AND item_optional2_code = :code AND item_optional2_id <> :excludeId
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("optionalOneId", optionalOneId)
                .addValue("code", code)
                .addValue("excludeId", excludeId);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public List<ItemOptionalTwo> getAllOptionalTwos() {

        String sql = """
                SELECT
                    item_optional2_id AS "itemOptionalTwoId",
                    item_optional2_code AS "itemOptionalTwoCode",
                    item_optional2_name AS "itemOptionalTwoName",
                    item_optional1_id AS "itemOptionalOneId"
                FROM item_optional2
                ORDER BY item_optional2_name
                """;

        return jdbcTemplate.query(
                sql,
                Collections.emptyMap(),
                new BeanPropertyRowMapper<>(ItemOptionalTwo.class)
        );
    }

    public int updateOptionalTwo(Long id, ItemOptionalTwo optionalTwo) {

        String sql = """
                UPDATE item_optional2
                SET
                    item_optional2_code = :code,
                    item_optional2_name = :name,
                    item_optional1_id = :optionalOneId
                WHERE item_optional2_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("code", optionalTwo.getItemOptionalTwoCode())
                .addValue("name", optionalTwo.getItemOptionalTwoName())
                .addValue("optionalOneId", optionalTwo.getItemOptionalOneId());

        return jdbcTemplate.update(sql, parameters);
    }
}
