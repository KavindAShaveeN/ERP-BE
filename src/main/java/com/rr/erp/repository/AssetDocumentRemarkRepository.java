package com.rr.erp.repository;

import com.rr.erp.dto.AssetDocumentRemark;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AssetDocumentRemarkRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AssetDocumentRemarkRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<AssetDocumentRemark> findByAssetCode(String assetCode) {

        return jdbcTemplate.query(
                "SELECT asset_code, remark, updated_at FROM asset_document_remark WHERE asset_code = :assetCode",
                new MapSqlParameterSource("assetCode", assetCode),
                (rs, rowNum) -> new AssetDocumentRemark(
                        rs.getString("asset_code"),
                        rs.getString("remark"),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                )
        ).stream().findFirst();
    }

    public void save(String assetCode, String remark) {

        jdbcTemplate.update("""
                INSERT INTO asset_document_remark (asset_code, remark, updated_at)
                VALUES (:assetCode, :remark, now())
                ON CONFLICT (asset_code) DO UPDATE SET remark = EXCLUDED.remark, updated_at = now()
                """,
                new MapSqlParameterSource()
                        .addValue("assetCode", assetCode)
                        .addValue("remark", remark)
        );
    }
}
