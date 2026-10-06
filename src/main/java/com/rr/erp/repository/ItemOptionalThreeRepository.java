package com.rr.erp.repository;

import com.rr.erp.entity.ItemOptionalThree;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class ItemOptionalThreeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ItemOptionalThreeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long addOptionalThree(ItemOptionalThree optionalThree) {

        String sql = """
                INSERT INTO item_optional3 (
                    item_optional3_code,
                    item_optional3_name,
                    item_optional2_id,
                    code_slug,
                    name_slug
                )
                VALUES (
                    :code,
                    :name,
                    :optionalTwoId,
                    :codeSlug,
                    :nameSlug
                )
                """;
        String codeSlug = optionalThree.getItemOptionalThreeCode().trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        String nameSlug = optionalThree.getItemOptionalThreeName().trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("code", optionalThree.getItemOptionalThreeCode())
                .addValue("name", optionalThree.getItemOptionalThreeName())
                .addValue("optionalTwoId", optionalThree.getItemOptionalTwoId())
                .addValue("codeSlug", codeSlug)
                .addValue("nameSlug", nameSlug);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, parameters, keyHolder, new String[] { "item_optional3_id" });

        return keyHolder.getKey().longValue();
    }

    public boolean existsById(Long id) {

        String sql = """
                SELECT COUNT(*)
                FROM item_optional3
                WHERE item_optional3_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByParentIdAndCode(Long optionalTwoId, String code) {

        String sql = """
                SELECT COUNT(*)
                FROM item_optional3
                WHERE item_optional2_id = :optionalTwoId AND item_optional3_code = :code
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("optionalTwoId", optionalTwoId)
                .addValue("code", code);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByParentIdAndCodeExcludingId(Long optionalTwoId, String code, Long excludeId) {

        String sql = """
                SELECT COUNT(*)
                FROM item_optional3
                WHERE item_optional2_id = :optionalTwoId AND item_optional3_code = :code AND item_optional3_id <> :excludeId
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("optionalTwoId", optionalTwoId)
                .addValue("code", code)
                .addValue("excludeId", excludeId);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public List<ItemOptionalThree> getAllOptionalThrees() {

        String sql = """
                SELECT
                    item_optional3_id AS "itemOptionalThreeId",
                    item_optional3_code AS "itemOptionalThreeCode",
                    item_optional3_name AS "itemOptionalThreeName",
                    item_optional2_id AS "itemOptionalTwoId"
                FROM item_optional3
                ORDER BY item_optional3_name
                """;

        return jdbcTemplate.query(
                sql,
                Collections.emptyMap(),
                new BeanPropertyRowMapper<>(ItemOptionalThree.class)
        );
    }

    public int updateOptionalThree(Long id, ItemOptionalThree optionalThree) {

        String sql = """
                UPDATE item_optional3
                SET
                    item_optional3_code = :code,
                    item_optional3_name = :name,
                    item_optional2_id = :optionalTwoId
                WHERE item_optional3_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("code", optionalThree.getItemOptionalThreeCode())
                .addValue("name", optionalThree.getItemOptionalThreeName())
                .addValue("optionalTwoId", optionalThree.getItemOptionalTwoId());

        return jdbcTemplate.update(sql, parameters);
    }
}
