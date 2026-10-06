package com.rr.erp.repository;

import com.rr.erp.entity.ItemOptionalOne;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class ItemOptionalOneRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ItemOptionalOneRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long addOptionalOne(ItemOptionalOne optionalOne) {

        String sql = """
                INSERT INTO item_optional1 (
                    item_optional1_code,
                    item_optional1_name,
                    item_model_id,
                    code_slug,
                    name_slug
                )
                VALUES (
                    :code,
                    :name,
                    :modelId,
                    :codeSlug,
                    :nameSlug
                )
                """;
        String codeSlug = optionalOne.getItemOptionalOneCode().trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        String nameSlug = optionalOne.getItemOptionalOneName().trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("code", optionalOne.getItemOptionalOneCode())
                .addValue("name", optionalOne.getItemOptionalOneName())
                .addValue("modelId", optionalOne.getItemModelId())
                .addValue("codeSlug", codeSlug)
                .addValue("nameSlug", nameSlug);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, parameters, keyHolder, new String[] { "item_optional1_id" });

        return keyHolder.getKey().longValue();
    }

    public boolean existsById(Long id) {

        String sql = """
                SELECT COUNT(*)
                FROM item_optional1
                WHERE item_optional1_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByParentIdAndCode(Long modelId, String code) {

        String sql = """
                SELECT COUNT(*)
                FROM item_optional1
                WHERE item_model_id = :modelId AND item_optional1_code = :code
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("modelId", modelId)
                .addValue("code", code);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByParentIdAndCodeExcludingId(Long modelId, String code, Long excludeId) {

        String sql = """
                SELECT COUNT(*)
                FROM item_optional1
                WHERE item_model_id = :modelId AND item_optional1_code = :code AND item_optional1_id <> :excludeId
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("modelId", modelId)
                .addValue("code", code)
                .addValue("excludeId", excludeId);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public List<ItemOptionalOne> getAllOptionalOnes() {

        String sql = """
                SELECT
                    item_optional1_id AS "itemOptionalOneId",
                    item_optional1_code AS "itemOptionalOneCode",
                    item_optional1_name AS "itemOptionalOneName",
                    item_model_id AS "itemModelId"
                FROM item_optional1
                ORDER BY item_optional1_name
                """;

        return jdbcTemplate.query(
                sql,
                Collections.emptyMap(),
                new BeanPropertyRowMapper<>(ItemOptionalOne.class)
        );
    }

    public int updateOptionalOne(Long id, ItemOptionalOne optionalOne) {

        String sql = """
                UPDATE item_optional1
                SET
                    item_optional1_code = :code,
                    item_optional1_name = :name,
                    item_model_id = :modelId
                WHERE item_optional1_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("code", optionalOne.getItemOptionalOneCode())
                .addValue("name", optionalOne.getItemOptionalOneName())
                .addValue("modelId", optionalOne.getItemModelId());

        return jdbcTemplate.update(sql, parameters);
    }
}
