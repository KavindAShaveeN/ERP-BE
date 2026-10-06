package com.rr.erp.repository;

import com.rr.erp.entity.ItemModel;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class ItemModelRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ItemModelRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long addModel(ItemModel model) {

        String sql = """
                INSERT INTO item_model (
                    item_model_code,
                    item_model_name,
                    item_brand_id,
                    code_slug,
                    name_slug
                )
                VALUES (
                    :code,
                    :name,
                    :brandId,
                    :codeSlug,
                    :nameSlug
                )
                """;
        String codeSlug = model.getItemModelCode().trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        String nameSlug = model.getItemModelName().trim().toLowerCase().replaceAll("[^a-z0-9]", "");

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("code", model.getItemModelCode())
                .addValue("name", model.getItemModelName())
                .addValue("brandId", model.getItemBrandId())
                .addValue("codeSlug", codeSlug)
                .addValue("nameSlug", nameSlug);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, parameters, keyHolder, new String[] { "item_model_id" });

        return keyHolder.getKey().longValue();
    }

    public boolean existsById(Long id) {

        String sql = """
                SELECT COUNT(*)
                FROM item_model
                WHERE item_model_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByParentIdAndCode(Long brandId, String code) {

        String sql = """
                SELECT COUNT(*)
                FROM item_model
                WHERE item_brand_id = :brandId AND item_model_code = :code
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("brandId", brandId)
                .addValue("code", code);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByParentIdAndCodeExcludingId(Long brandId, String code, Long excludeId) {

        String sql = """
                SELECT COUNT(*)
                FROM item_model
                WHERE item_brand_id = :brandId AND item_model_code = :code AND item_model_id <> :excludeId
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("brandId", brandId)
                .addValue("code", code)
                .addValue("excludeId", excludeId);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public List<ItemModel> getAllModels() {

        String sql = """
                SELECT
                    item_model_id AS "itemModelId",
                    item_model_code AS "itemModelCode",
                    item_model_name AS "itemModelName",
                    item_brand_id AS "itemBrandId"
                FROM item_model
                ORDER BY item_model_name
                """;

        return jdbcTemplate.query(
                sql,
                Collections.emptyMap(),
                new BeanPropertyRowMapper<>(ItemModel.class)
        );
    }

    public int updateModel(Long id, ItemModel model) {

        String sql = """
                UPDATE item_model
                SET
                    item_model_code = :code,
                    item_model_name = :name,
                    item_brand_id = :brandId
                WHERE id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("code", model.getItemModelCode())
                .addValue("name", model.getItemModelName())
                .addValue("brandId", model.getItemBrandId());

        return jdbcTemplate.update(sql, parameters);
    }
}
