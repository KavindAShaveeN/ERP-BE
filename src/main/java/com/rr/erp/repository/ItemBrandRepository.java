package com.rr.erp.repository;

import com.rr.erp.entity.ItemBrand;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class ItemBrandRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ItemBrandRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long addBrand(ItemBrand brand) {

        String sql = """
                INSERT INTO item_brand (
                    item_brand_code,
                    item_brand_name,
                    item_subSubCategory_id,
                    code_slug,
                    name_slug
                )
                VALUES (
                    :code,
                    :name,
                    :subSubCategoryId,
                    :codeSlug,
                    :nameSlug
                )
                """;
        String codeSlug = brand.getItemBrandCode().trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        String nameSlug = brand.getItemBrandName().trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("code", brand.getItemBrandCode())
                .addValue("name", brand.getItemBrandName())
                .addValue("subSubCategoryId", brand.getItemSubSubCategoryId())
                .addValue("codeSlug", codeSlug)
                .addValue("nameSlug", nameSlug);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, parameters, keyHolder, new String[] { "item_brand_id" });

        return keyHolder.getKey().longValue();
    }

    public boolean existsById(Long id) {

        String sql = """
                SELECT COUNT(*)
                FROM item_brand
                WHERE item_brand_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByParentIdAndCode(Long subSubCategoryId, String code) {

        String sql = """
                SELECT COUNT(*)
                FROM item_brand
                WHERE item_subSubCategory_id = :subSubCategoryId AND item_brand_code = :code
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("subSubCategoryId", subSubCategoryId)
                .addValue("code", code);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByParentIdAndCodeExcludingId(Long subSubCategoryId, String code, Long excludeId) {

        String sql = """
                SELECT COUNT(*)
                FROM item_brand
                WHERE item_subSubCategory_id = :subSubCategoryId AND item_brand_code = :code AND item_brand_id <> :excludeId
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("subSubCategoryId", subSubCategoryId)
                .addValue("code", code)
                .addValue("excludeId", excludeId);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public List<ItemBrand> getAllBrands() {

        String sql = """
                SELECT
                    item_brand_id AS "itemBrandId",
                    item_brand_code AS "itemBrandCode",
                    item_brand_name AS "itemBrandName",
                    item_subSubCategory_id AS "itemSubSubCategoryId"
                FROM item_brand
                ORDER BY item_brand_name
                """;

        return jdbcTemplate.query(
                sql,
                Collections.emptyMap(),
                new BeanPropertyRowMapper<>(ItemBrand.class)
        );
    }

    public int updateBrand(Long id, ItemBrand brand) {

        String sql = """
                UPDATE item_brand
                SET
                    item_brand_code = :code,
                    item_brand_name = :name,
                    item_subSubCategory_id = :subSubCategoryId
                WHERE item_brand_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("code", brand.getItemBrandCode())
                .addValue("name", brand.getItemBrandName())
                .addValue("subSubCategoryId", brand.getItemSubSubCategoryId());

        return jdbcTemplate.update(sql, parameters);
    }
}
