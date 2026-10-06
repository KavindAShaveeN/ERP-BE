package com.rr.erp.repository;

import com.rr.erp.entity.AssetPackItem;
import com.rr.erp.entity.PackCarrier;
import com.rr.erp.entity.PackSelection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class AssetPackRepository {

    private final JdbcTemplate jdbcTemplate;

    public AssetPackRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SELECT = """
            SELECT pack_item_id, asset_code, name, description, serial_number, quantity, is_with_asset, remarks
            FROM asset_pack_item
            """;

    private static final RowMapper<AssetPackItem> ROW_MAPPER = (rs, rowNum) -> {
        AssetPackItem p = new AssetPackItem();
        p.setPackItemId(rs.getLong("pack_item_id"));
        p.setAssetCode(rs.getString("asset_code"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setSerialNumber(rs.getString("serial_number"));
        p.setQuantity(rs.getInt("quantity"));
        p.setIsWithAsset(rs.getBoolean("is_with_asset"));
        p.setRemarks(rs.getString("remarks"));
        return p;
    };

    public List<AssetPackItem> findByAssetCode(String assetCode) {
        return jdbcTemplate.query(
                SELECT + " WHERE asset_code = ? AND is_active = TRUE ORDER BY pack_item_id",
                ROW_MAPPER, assetCode);
    }

    public boolean assetExists(String assetCode) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM asset WHERE asset_code = ?", Integer.class, assetCode);
        return n != null && n > 0;
    }

    public Long insert(AssetPackItem p) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO asset_pack_item (asset_code, name, description, serial_number, quantity, remarks)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING pack_item_id
                """,
                Long.class,
                p.getAssetCode(), p.getName(), p.getDescription(), p.getSerialNumber(),
                p.getQuantity() == null ? 1 : p.getQuantity(), p.getRemarks());
    }

    public int update(Long id, String assetCode, AssetPackItem p) {
        return jdbcTemplate.update("""
                UPDATE asset_pack_item
                SET name = ?, description = ?, serial_number = ?, quantity = ?, remarks = ?, updated_at = now()
                WHERE pack_item_id = ? AND asset_code = ?
                """,
                p.getName(), p.getDescription(), p.getSerialNumber(),
                p.getQuantity() == null ? 1 : p.getQuantity(), p.getRemarks(), id, assetCode);
    }

    public int delete(Long id, String assetCode) {
        return jdbcTemplate.update(
                "DELETE FROM asset_pack_item WHERE pack_item_id = ? AND asset_code = ?", id, assetCode);
    }

    public void deleteSelections(String docType, UUID docId, String assetCode) {
        jdbcTemplate.update(
                "DELETE FROM asset_pack_transaction WHERE doc_type = ? AND doc_id = ? AND asset_code = ?",
                docType, docId, assetCode);
    }

    public void deleteSelections(String docType, UUID docId) {
        jdbcTemplate.update(
                "DELETE FROM asset_pack_transaction WHERE doc_type = ? AND doc_id = ?", docType, docId);
    }

    public void insertSelection(String docType, UUID docId, String assetCode, Long packItemId, boolean included) {
        jdbcTemplate.update("""
                INSERT INTO asset_pack_transaction (doc_type, doc_id, asset_code, pack_item_id, included)
                SELECT ?, ?, ?, pack_item_id, ?
                FROM asset_pack_item
                WHERE pack_item_id = ? AND asset_code = ?
                """,
                docType, docId, assetCode, included, packItemId, assetCode);
    }

    public void setWithAsset(Long packItemId, boolean withAsset) {
        jdbcTemplate.update(
                "UPDATE asset_pack_item SET is_with_asset = ?, updated_at = now() WHERE pack_item_id = ?",
                withAsset, packItemId);
    }

    public List<PackSelection> findSelections(String docType, UUID docId, String assetCode) {
        return jdbcTemplate.query("""
                SELECT t.pack_item_id, t.included, p.name, p.quantity
                FROM asset_pack_transaction t
                JOIN asset_pack_item p ON p.pack_item_id = t.pack_item_id
                WHERE t.doc_type = ? AND t.doc_id = ? AND t.asset_code = ?
                ORDER BY t.pack_item_id
                """,
                (rs, rowNum) -> {
                    PackSelection s = new PackSelection();
                    s.setPackItemId(rs.getLong("pack_item_id"));
                    s.setIncluded(rs.getBoolean("included"));
                    s.setName(rs.getString("name"));
                    s.setQuantity(rs.getInt("quantity"));
                    return s;
                },
                docType, docId, assetCode);
    }

    /** Fills packItems on every asset line of a document; lines without an asset are left alone. */
    public static void attachSelections(
            JdbcTemplate jdbcTemplate, String docType, UUID docId, List<? extends PackCarrier> items) {

        if (items == null) {
            return;
        }
        AssetPackRepository repo = new AssetPackRepository(jdbcTemplate);
        for (PackCarrier item : items) {
            if (item.getAssetCode() != null && !item.getAssetCode().isBlank()) {
                item.setPackItems(new ArrayList<>(
                        repo.findSelections(docType, docId, item.getAssetCode().trim())));
            }
        }
    }
}
