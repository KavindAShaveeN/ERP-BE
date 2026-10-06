package com.rr.erp.repository;

import com.rr.erp.entity.AssetSparePart;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AssetSparePartRepository {

    private final JdbcTemplate jdbcTemplate;

    public AssetSparePartRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SELECT = """
            SELECT
                sp.asset_spare_part_id AS "assetSparePartId",
                sp.asset_code AS "assetCode",
                sp.item_code_id AS "itemCodeId",
                sp.part_role AS "partRole",
                sp.part_number AS "partNumber",
                sp.serial_number AS "serialNumber",
                sp.quantity_per_unit AS "quantityPerUnit",
                sp.remarks AS "remarks",
                ic.item_code_code AS "itemCode",
                ic.item_code_name AS "itemName",
                u.uom_name AS "uomName",
                ci.current_stock AS "currentStock",
                ci.unit_price AS "unitPrice"
            FROM asset_spare_part sp
            JOIN item_code ic ON ic.item_code_id = sp.item_code_id
            LEFT JOIN consumable_item ci ON ci.item_code_id = sp.item_code_id
            LEFT JOIN uom u ON u.uom_id = ci.uom_id
            """;

    private static final org.springframework.jdbc.core.RowMapper<AssetSparePart> ROW_MAPPER =
            (rs, rowNum) -> {
                AssetSparePart p = new AssetSparePart();
                p.setAssetSparePartId(rs.getLong("assetSparePartId"));
                p.setAssetCode(rs.getString("assetCode"));
                p.setItemCodeId(rs.getLong("itemCodeId"));
                p.setPartRole(rs.getString("partRole"));
                p.setPartNumber(rs.getString("partNumber"));
                p.setSerialNumber(rs.getString("serialNumber"));
                p.setQuantityPerUnit(rs.getBigDecimal("quantityPerUnit"));
                p.setRemarks(rs.getString("remarks"));
                p.setItemCode(rs.getString("itemCode"));
                p.setItemName(rs.getString("itemName"));
                p.setUomName(rs.getString("uomName"));
                p.setCurrentStock(rs.getBigDecimal("currentStock"));
                p.setUnitPrice(rs.getBigDecimal("unitPrice"));
                return p;
            };

    public List<AssetSparePart> findByAssetCode(String assetCode) {
        return jdbcTemplate.query(
                SELECT + " WHERE sp.asset_code = ? AND sp.is_active = TRUE ORDER BY sp.part_role, ic.item_code_code",
                ROW_MAPPER,
                assetCode);
    }

    public List<AssetSparePart> findByItemCodeId(Long itemCodeId) {
        return jdbcTemplate.query(
                SELECT + " WHERE sp.item_code_id = ? AND sp.is_active = TRUE ORDER BY sp.asset_code",
                ROW_MAPPER,
                itemCodeId);
    }

    public boolean assetExists(String assetCode) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM asset WHERE asset_code = ?", Integer.class, assetCode);
        return n != null && n > 0;
    }

    public boolean itemCodeExists(Long itemCodeId) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM item_code WHERE item_code_id = ?", Integer.class, itemCodeId);
        return n != null && n > 0;
    }

    public Long insert(AssetSparePart p) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO asset_spare_part (asset_code, item_code_id, part_role, part_number, serial_number, quantity_per_unit, remarks)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING asset_spare_part_id
                """,
                Long.class,
                p.getAssetCode(), p.getItemCodeId(), p.getPartRole(), p.getPartNumber(), p.getSerialNumber(),
                p.getQuantityPerUnit(), p.getRemarks());
    }

    public int update(Long id, String assetCode, AssetSparePart p) {
        return jdbcTemplate.update("""
                UPDATE asset_spare_part
                SET item_code_id = ?, part_role = ?, part_number = ?, serial_number = ?, quantity_per_unit = ?, remarks = ?, updated_at = now()
                WHERE asset_spare_part_id = ? AND asset_code = ?
                """,
                p.getItemCodeId(), p.getPartRole(), p.getPartNumber(), p.getSerialNumber(),
                p.getQuantityPerUnit(), p.getRemarks(), id, assetCode);
    }

    public int delete(Long id, String assetCode) {
        return jdbcTemplate.update(
                "DELETE FROM asset_spare_part WHERE asset_spare_part_id = ? AND asset_code = ?", id, assetCode);
    }

    public int deleteAllForAsset(String assetCode) {
        return jdbcTemplate.update("DELETE FROM asset_spare_part WHERE asset_code = ?", assetCode);
    }
}
