package com.rr.erp.repository;

import com.rr.erp.entity.ItemCategory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class ItemCategoryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final JdbcTemplate template;

    public ItemCategoryRepository(NamedParameterJdbcTemplate jdbcTemplate,JdbcTemplate template) {
        this.jdbcTemplate = jdbcTemplate;
        this.template = template;
    }

    public Long addCategory(ItemCategory category) {

        String sql = """
                INSERT INTO item_category (
                    item_type_id,
                    item_category_code,
                    item_category_name,
                    code_slug,
                    name_slug
                )
                VALUES (
                    :type_id,
                    :code,
                    :name,
                    :code_slug,
                    :name_slug
                )
                """;
        String name_slug = category.getItemCategoryName().trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        String code_slug = category.getItemCategoryCode().trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("type_id", category.getItemTypeId())
                .addValue("code", category.getItemCategoryCode())
                .addValue("name", category.getItemCategoryName())
                .addValue("code_slug",code_slug)
                .addValue("name_slug", name_slug);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, parameters, keyHolder, new String[] { "item_category_id" });

        return keyHolder.getKey().longValue();
    }

    public boolean existsById(Long id) {

        String sql = """
                SELECT COUNT(*)
                FROM item_category
                WHERE item_category_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByTypeAndCode(Integer type, String code) {

        String sql = """
                SELECT COUNT(*)
                FROM item_category
                WHERE item_type_id = :type AND item_category_code = :code
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("type", type)
                .addValue("code", code);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByTypeAndCodeExcludingId(Integer type, String code, Long excludeId) {

        String sql = """
                SELECT COUNT(*)
                FROM item_category
                WHERE item_type_id = :type AND item_category_code = :code AND item_category_id <> :excludeId
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("type", type)
                .addValue("code", code)
                .addValue("excludeId", excludeId);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public List<ItemCategory> getAllCategories() {

        String sql = """
                SELECT
                    item_category_id AS "itemCategoryId",
                    item_category_code AS "itemCategoryCode",
                    item_category_name AS "itemCategoryName",
                    item_type_id AS "itemTypeId"
                FROM item_category
                ORDER BY item_category_name
                """;

        return jdbcTemplate.query(
                sql,
                Collections.emptyMap(),
                new BeanPropertyRowMapper<>(ItemCategory.class)
        );
    }

    public int updateCategory(Long id, ItemCategory category) {

        String sql = """
                UPDATE item_category
                SET
                    item_category_code = :code,
                    item_category_name = :name,
                    item_type_id = :type
                WHERE item_category_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("code", category.getItemCategoryCode())
                .addValue("name", category.getItemCategoryName())
                .addValue("type", category.getItemTypeId());

        return jdbcTemplate.update(sql, parameters);
    }

    public ItemCategory getItemCategoryById(Long itemCategoryId) {

        String sql = """
                SELECT
                    item_category_id AS "itemCategoryId",
                    item_category_name AS "itemCategoryName",
                    item_category_code AS "itemCategoryCode"
                FROM item_category
                WHERE item_category_id = ?
                """;

        return template.queryForObject(
                sql,
                new BeanPropertyRowMapper<>(ItemCategory.class),
                itemCategoryId
        );
    }
}
