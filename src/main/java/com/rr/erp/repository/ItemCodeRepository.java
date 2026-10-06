package com.rr.erp.repository;

import com.rr.erp.dto.ItemCodeDetailResponse;
import com.rr.erp.entity.ItemCode;
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
public class ItemCodeRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ItemCodeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long addItemCode(ItemCode itemCode) {

        String sql = """
                INSERT INTO item_code (
                    item_code_code,
                    item_code_name,
                    item_category_id,
                    item_subcategory_id,
                    item_subSubCategory_id,
                    item_brand_id,
                    item_model_id,
                    item_optional1_id,
                    item_optional2_id,
                    item_optional3_id
                )
                VALUES (
                    :code,
                    :name,
                    :categoryId,
                    :subCategoryId,
                    :subSubCategoryId,
                    :brandId,
                    :modelId,
                    :optionalOneId,
                    :optionalTwoId,
                    :optionalThreeId
                )
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("code", itemCode.getItemCodeCode())
                .addValue("name", itemCode.getItemCodeName())
                .addValue("categoryId", itemCode.getItemCategoryId())
                .addValue("subCategoryId", itemCode.getItemSubCategoryId())
                .addValue("subSubCategoryId", itemCode.getItemSubSubCategoryId())
                .addValue("brandId", itemCode.getItemBrandId())
                .addValue("modelId", itemCode.getItemModelId())
                .addValue("optionalOneId", itemCode.getItemOptionalOneId())
                .addValue("optionalTwoId", itemCode.getItemOptionalTwoId())
                .addValue("optionalThreeId", itemCode.getItemOptionalThreeId());
        jdbcTemplate.update(sql, parameters, keyHolder, new String[] { "item_code_id" });

        return keyHolder.getKey().longValue();
    }

    public boolean existsById(Long id) {

        String sql = """
                SELECT COUNT(*)
                FROM item_code
                WHERE item_code_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean existsByCode(String code) {

        String sql = """
                SELECT COUNT(*)
                FROM item_code
                WHERE item_code_code = :code
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("code", code);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public List<ItemCodeDetailResponse> getAllItemCodes() {

        String sql = """
            SELECT
                ic.item_code_id AS "itemCodeId",
                ic.item_code_code AS "itemCodeCode",
                ic.item_code_name AS "itemCodeName",
                it.item_type_id AS "itemTypeId",
                it.item_type_name AS "itemTypeName",
                ic.item_category_id AS "itemCategoryId",
                c.item_category_name AS "itemCategoryName",
                ic.item_subcategory_id AS "itemSubCategoryId",
                sc.item_subcategory_name AS "itemSubCategoryName",
                ic.item_subsubcategory_id AS "itemSubSubCategoryId",
                ssc.item_subsubcategory_name AS "itemSubSubCategoryName",
                ic.item_brand_id AS "itemBrandId",
                b.item_brand_name AS "itemBrandName",
                ic.item_model_id AS "itemModelId",
                m.item_model_name AS "itemModelName",
                ic.item_optional1_id AS "itemOptionalOneId",
                o1.item_optional1_name AS "itemOptionalOneName",
                ic.item_optional2_id AS "itemOptionalTwoId",
                o2.item_optional2_name AS "itemOptionalTwoName",
                ic.item_optional3_id AS "itemOptionalThreeId",
                o3.item_optional3_name AS "itemOptionalThreeName"
            FROM item_code ic
            LEFT JOIN item_category c ON c.item_category_id = ic.item_category_id
            LEFT JOIN item_type it ON it.item_type_id = c.item_type_id
            LEFT JOIN item_subcategory sc ON sc.item_subcategory_id = ic.item_subcategory_id
            LEFT JOIN item_subsubcategory ssc ON ssc.item_subsubcategory_id = ic.item_subsubcategory_id
            LEFT JOIN item_brand b ON b.item_brand_id = ic.item_brand_id
            LEFT JOIN item_model m ON m.item_model_id = ic.item_model_id
            LEFT JOIN item_optional1 o1 ON o1.item_optional1_id = ic.item_optional1_id
            LEFT JOIN item_optional2 o2 ON o2.item_optional2_id = ic.item_optional2_id
            LEFT JOIN item_optional3 o3 ON o3.item_optional3_id = ic.item_optional3_id
            ORDER BY ic.item_code_name
            """;

        return jdbcTemplate.query(
                sql,
                Collections.emptyMap(),
                new BeanPropertyRowMapper<>(ItemCodeDetailResponse.class)
        );
    }

    public Optional<ItemCode> getItemCodeById(Long id) {

        String sql = """
            SELECT
                item_code_id AS "itemCodeId",
                item_code_code AS "itemCodeCode",
                item_code_name AS "itemCodeName",
                item_category_id AS "itemCategoryId",
                item_subcategory_id AS "itemSubCategoryId",
                item_subSubCategory_id AS "itemSubSubCategoryId",
                item_brand_id AS "itemBrandId",
                item_model_id AS "itemModelId",
                item_optional1_id AS "itemOptionalOneId",
                item_optional2_id AS "itemOptionalTwoId",
                item_optional3_id AS "itemOptionalThreeId"
            FROM item_code 
            WHERE item_code_id = :id
            """;

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);

        List<ItemCode> results = jdbcTemplate.query(
                sql,
                parameters,
                new BeanPropertyRowMapper<>(ItemCode.class)
        );

        return results.stream().findFirst();
    }
    public Long getIdByCode(String itemCodeCode) {
        String sql = """
            SELECT item_code_id
            FROM item_code
            WHERE item_code_code = :code
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("code", itemCodeCode);

        return jdbcTemplate.queryForObject(
                sql,
                params,
                Long.class
        );
    }

    public int updateName(Long id, String name) {

        String sql = """
                UPDATE item_code
                SET item_code_name = :name
                WHERE item_code_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("name", name);

        return jdbcTemplate.update(sql, parameters);
    }

    public int deleteById(Long id) {

        String sql = """
                DELETE FROM item_code
                WHERE item_code_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("id", id);

        return jdbcTemplate.update(sql, parameters);
    }
}
