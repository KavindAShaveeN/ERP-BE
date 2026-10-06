package com.rr.erp.repository;


import com.rr.erp.entity.ItemSubCategory;
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
public class ItemSubCategoryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final JdbcTemplate template;

    public ItemSubCategoryRepository(NamedParameterJdbcTemplate jdbcTemplate, JdbcTemplate template) {
        this.jdbcTemplate = jdbcTemplate;
        this.template = template;
    }

    public Long addSubCategory(ItemSubCategory subCategory) {

        String sql = """
                INSERT INTO item_subcategory (
                    item_subcategory_code,
                    item_subcategory_name,
                    item_category_id,
                    code_slug,
                    name_slug,
                    starting_sequence
                )
                VALUES (
                    :code,
                    :name,
                    :categoryId,
                    :codeSlug,
                    :nameSlug,
                    :startingSequence
                )
                """;
        String codeSlug = subCategory.getItemSubCategoryCode().trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        String nameSlug = subCategory.getItemSubCategoryName().trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("code", subCategory.getItemSubCategoryCode())
                .addValue("name", subCategory.getItemSubCategoryName())
                .addValue("categoryId", subCategory.getItemCategoryId())
                .addValue("codeSlug", codeSlug)
                .addValue("nameSlug",nameSlug)
                .addValue("startingSequence", subCategory.getStartingSequence() == null ? 0 : subCategory.getStartingSequence());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, parameters, keyHolder, new String[] { "item_subcategory_id" });

        return keyHolder.getKey().longValue();
    }

    public boolean existsById(Long id) {

        String sql = """
                SELECT COUNT(*)
                FROM item_subcategory
                WHERE item_subcategory_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByParentIdAndCode(Long categoryId, String code) {

        String sql = """
                SELECT COUNT(*)
                FROM item_subcategory
                WHERE item_category_id = :categoryId AND item_subcategory_code = :code
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("categoryId", categoryId)
                .addValue("code", code);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByParentIdAndCodeExcludingId(Long categoryId, String code, Long excludeId) {

        String sql = """
                SELECT COUNT(*)
                FROM item_subcategory
                WHERE item_category_id = :categoryId AND item_subcategory_code = :code AND item_subcategory_id <> :excludeId
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("categoryId", categoryId)
                .addValue("code", code)
                .addValue("excludeId", excludeId);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public List<ItemSubCategory> getAllSubCategories()
    {

        String sql = """
                SELECT
                    item_subcategory_id AS "itemSubCategoryId",
                    item_subcategory_code AS "itemSubCategoryCode",
                    item_subcategory_name AS "itemSubCategoryName",
                    item_category_id AS "itemCategoryId",
                    starting_sequence AS "startingSequence"
                FROM item_subcategory
                ORDER BY item_subcategory_name
                """;

        return jdbcTemplate.query(
                sql,
                Collections.emptyMap(),
                new BeanPropertyRowMapper<>(ItemSubCategory.class)
        );
    }

    public int updateSubCategory(Long id, ItemSubCategory subCategory) {

        String sql = """
                UPDATE item_subcategory
                SET
                    item_subcategory_code = :code,
                    item_subcategory_name = :name,
                    item_category_id = :categoryId
                WHERE id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("code", subCategory.getItemSubCategoryCode())
                .addValue("name", subCategory.getItemSubCategoryName())
                .addValue("categoryId", subCategory.getItemCategoryId());

        return jdbcTemplate.update(sql, parameters);
    }

    public ItemSubCategory getItemSubCategoryById(Long itemSubCategoryId) {

        String sql = """
                SELECT
                    item_subCategory_id AS "itemSubCategoryId",
                    item_subCategory_name AS "itemSubCategoryName",
                    item_subCategory_code AS "itemSubCategoryCode"
                FROM item_subCategory
                WHERE item_subCategory_id = ?
                """;

        return template.queryForObject(
                sql,
                new BeanPropertyRowMapper<>(ItemSubCategory.class),
                itemSubCategoryId
        );
    }


}
