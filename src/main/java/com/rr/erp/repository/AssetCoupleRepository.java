package com.rr.erp.repository;

import com.rr.erp.entity.AssetCouple;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AssetCoupleRepository {

    private final JdbcTemplate jdbcTemplate;

    public AssetCoupleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<AssetCouple> ROW_MAPPER = (rs, rowNum) -> {
        AssetCouple c = new AssetCouple();
        c.setCoupleId(rs.getLong("couple_id"));
        c.setCoupledAssetCode(rs.getString("coupled_asset_code"));
        c.setAssetClass(rs.getString("asset_class"));
        c.setDescription(rs.getString("description"));
        c.setStatus(rs.getString("status"));
        return c;
    };

    public List<AssetCouple> findByAssetCode(String assetCode) {
        return jdbcTemplate.query("""
                SELECT c.couple_id, a.asset_code AS coupled_asset_code, a.asset_class, a.description, a.status
                FROM asset_couple c
                JOIN asset a ON a.asset_code = CASE WHEN c.asset_code_a = ? THEN c.asset_code_b ELSE c.asset_code_a END
                WHERE c.asset_code_a = ? OR c.asset_code_b = ?
                ORDER BY a.asset_code
                """,
                ROW_MAPPER, assetCode, assetCode, assetCode);
    }

    public boolean assetExists(String assetCode) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM asset WHERE asset_code = ?", Integer.class, assetCode);
        return n != null && n > 0;
    }

    /** Inserts the pair (already ordered a &lt; b); false when the pair already exists. */
    public boolean insert(String assetCodeA, String assetCodeB) {
        return jdbcTemplate.update("""
                INSERT INTO asset_couple (asset_code_a, asset_code_b)
                VALUES (?, ?)
                ON CONFLICT DO NOTHING
                """, assetCodeA, assetCodeB) > 0;
    }

    public int delete(Long coupleId, String assetCode) {
        return jdbcTemplate.update("""
                DELETE FROM asset_couple
                WHERE couple_id = ? AND (asset_code_a = ? OR asset_code_b = ?)
                """, coupleId, assetCode, assetCode);
    }
}
