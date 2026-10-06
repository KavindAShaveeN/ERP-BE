package com.rr.erp.repository;

import com.rr.erp.dto.AssetCodeRequestDTO;
import com.rr.erp.entity.AssetCode;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AssetCodeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AssetCodeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AssetCode save(AssetCodeRequestDTO request) {

        String sql = """
                INSERT INTO asset_code (
                    asset_code_code,
                    item_code_id
                )
                VALUES (
                    :code,
                    :itemCodeId
                )
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("code", request.getAssetCodeCode())
                .addValue("itemCodeId", request.getItemCodeId());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, parameters, keyHolder, new String[] { "asset_code_id" });

        return new AssetCode(
                keyHolder.getKey().longValue(),
                request.getAssetCodeCode(),
                request.getItemCodeId()
        );
    }

    public List<AssetCode> getAllAssetCodes() {

        String sql = """
                SELECT
                    asset_code_id AS "assetCodeId",
                    asset_code_code AS "assetCodeCode",
                    item_code_id AS "itemCodeId"
                FROM asset_code
                ORDER BY asset_code_code
                """;

        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource(),
                new BeanPropertyRowMapper<>(AssetCode.class)
        );
    }

    public Optional<AssetCode> findByAssetCodeCode(String assetCodeCode) {

        String sql = """
                SELECT
                    asset_code_id AS "assetCodeId",
                    asset_code_code AS "assetCodeCode",
                    item_code_id AS "itemCodeId"
                FROM asset_code
                WHERE asset_code_code = :code
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("code", assetCodeCode);

        List<AssetCode> results = jdbcTemplate.query(
                sql,
                parameters,
                new BeanPropertyRowMapper<>(AssetCode.class)
        );

        return results.stream().findFirst();
    }

    public boolean existsById(Long assetCodeId) {

        String sql = """
                SELECT COUNT(*)
                FROM asset_code
                WHERE asset_code_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", assetCodeId);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public Optional<AssetCode> findById(Long assetCodeId) {

        String sql = """
                SELECT
                    asset_code_id AS "assetCodeId",
                    asset_code_code AS "assetCodeCode",
                    item_code_id AS "itemCodeId"
                FROM asset_code
                WHERE asset_code_id = :id
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", assetCodeId);

        List<AssetCode> results = jdbcTemplate.query(
                sql,
                parameters,
                new BeanPropertyRowMapper<>(AssetCode.class)
        );

        return results.stream().findFirst();
    }

    public boolean existsByAssetCodeCode(String assetCodeCode) {

        String sql = """
                SELECT COUNT(*)
                FROM asset_code
                WHERE asset_code_code = :code
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("code", assetCodeCode);

        Integer count = jdbcTemplate.queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    public boolean deleteByAssetCodeCode(String assetCodeCode) {

        String sql = """
                DELETE FROM asset_code
                WHERE asset_code_code = :code
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("code", assetCodeCode);

        int affectedRows = jdbcTemplate.update(sql, parameters);

        return affectedRows > 0;
    }
}
