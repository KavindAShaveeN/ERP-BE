package com.rr.erp.repository;

import com.rr.erp.entity.ItemSubSubCategory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class ItemSubSubCategoryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ItemSubSubCategoryRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long addSubSubCategory(ItemSubSubCategory subSubCategory) {

        String sql = """
                INSERT INTO item_subSubCategory (
                    item_subSubCategory_code,
                    item_subSubCategory_name,
                    item_subCategory_id,
                    code_slug,
                    name_slug
                )
                VALUES (
                    :code,
                    :name,
                    :subCategoryId,
                    :codeSlug,
                    :nameSlug
                )
                """;
        String codeSlug = subSubCategory.getItemSubSubCategoryCode().trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        String nameSlug = subSubCategory.getItemSubSubCategoryCode().trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("code", subSubCategory.getItemSubSubCategoryCode())
                .addValue("name", subSubCategory.getItemSubSubCategoryName())
                .addValue("subCategoryId", subSubCategory.getItemSubCategoryId())
                .addValue("codeSlug", codeSlug)
                .addValue("nameSlug", nameSlug);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, parameters, keyHolder, new String[] { "item_subsubcategory_id" });

        return keyHolder.getKey().longValue();
    }

    public boolean existsById(Long id) {

        String sql = """
                SELECT COUNT(*)
                FROM item_subSubCategory
                WHERE item_subSubCategory_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByParentIdAndCode(Long subCategoryId, String code) {

        String sql = """
                SELECT COUNT(*)
                FROM item_subSubCategory
                WHERE item_subCategory_id = :subCategoryId AND item_subSubCategory_code = :code
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("subCategoryId", subCategoryId)
                .addValue("code", code);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByParentIdAndCodeExcludingId(Long subCategoryId, String code, Long excludeId) {

        String sql = """
                SELECT COUNT(*)
                FROM item_subSubCategory
                WHERE item_subCategory_id = :subCategoryId AND item_subSubCategory_code = :code AND item_subSubCategory_id <> :excludeId
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("subCategoryId", subCategoryId)
                .addValue("code", code)
                .addValue("excludeId", excludeId);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public List<ItemSubSubCategory> getAllSubSubCategories() {

        String sql = """
                SELECT
                    item_subSubCategory_id AS "itemSubSubCategoryId",
                    item_subSubCategory_code AS "itemSubSubCategoryCode",
                    item_subSubCategory_name AS "itemSubSubCategoryName",
                    item_subCategory_id AS "ItemSubCategoryId"
                FROM item_subSubCategory
                ORDER BY item_subSubCategory_Name
                """;

        return jdbcTemplate.query(
                sql,
                Collections.emptyMap(),
                new BeanPropertyRowMapper<>(ItemSubSubCategory.class)
        );
    }

    public int updateSubSubCategory(Long id, ItemSubSubCategory subSubCategory) {

        String sql = """
                UPDATE item_subSubCategory
                SET
                    item_subSubCategory_code = :code,
                    item_subSubCategory_name = :name,
                    item_subCategory_id = :subCategoryId
                WHERE item_subSubCategory_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("code", subSubCategory.getItemSubSubCategoryCode())
                .addValue("name", subSubCategory.getItemSubSubCategoryName())
                .addValue("subCategoryId", subSubCategory.getItemSubCategoryId());

        return jdbcTemplate.update(sql, parameters);
    }
}
