package com.rr.erp.repository;

import com.rr.erp.entity.InventoryItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class InventoryItemRepository {

    private final JdbcTemplate jdbcTemplate;

    public InventoryItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ==============================
    // POST - Create Inventory Item
    // ==============================
    public Integer createInventoryItem(InventoryItem inventoryItem) {

        String sql = """
                INSERT INTO inventory_item (
                    item_code_id,
                    uom_id,
                    quantity_on_hand,
                    maximum_stock_level,
                    average_unit_price,
                    last_purchase_price,
                    remarks,
                    is_active
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING inventory_item_id
                """;

        return jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                inventoryItem.getItemCodeId(),
                inventoryItem.getUomId(),
                inventoryItem.getQuantityOnHand(),
                inventoryItem.getMaximumStockLevel(),
                inventoryItem.getAverageUnitPrice(),
                inventoryItem.getLastPurchasePrice(),
                inventoryItem.getRemarks(),
                inventoryItem.getIsActive()
        );
    }

    // ==============================
    // GET - All Inventory Items
    // ==============================
    public List<InventoryItem> getAllInventoryItems() {

        String sql = """
                SELECT
                    inventory_item_id AS "inventoryItemId",
                    item_code_id AS "itemCodeId",
                    uom_id AS "uomId",
                    quantity_on_hand AS "quantityOnHand",
                    maximum_stock_level AS "maximumStockLevel",
                    average_unit_price AS "averageUnitPrice",
                    last_purchase_price AS "lastPurchasePrice",
                    remarks AS "remarks",
                    is_active AS "isActive"
                FROM inventory_item
                ORDER BY inventory_item_id
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapInventoryItem(rs)
        );
    }

    // ==============================
    // GET - Inventory Item by ID
    // ==============================
    public InventoryItem getInventoryItemById(Integer inventoryItemId) {

        String sql = """
                SELECT
                    inventory_item_id AS "inventoryItemId",
                    item_code_id AS "itemCodeId",
                    uom_id AS "uomId",
                    quantity_on_hand AS "quantityOnHand",
                    maximum_stock_level AS "maximumStockLevel",
                    average_unit_price AS "averageUnitPrice",
                    last_purchase_price AS "lastPurchasePrice",
                    remarks AS "remarks",
                    is_active AS "isActive"
                FROM inventory_item
                WHERE inventory_item_id = ?
                """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> mapInventoryItem(rs),
                inventoryItemId
        );
    }

    // ==============================
    // PUT - Update Inventory Item
    // ==============================
    public int updateInventoryItem(
            Integer inventoryItemId,
            InventoryItem inventoryItem
    ) {

        String sql = """
                UPDATE inventory_item
                SET
                    item_code_id = ?,
                    uom_id = ?,
                    quantity_on_hand = ?,
                    maximum_stock_level = ?,
                    average_unit_price = ?,
                    last_purchase_price = ?,
                    remarks = ?,
                    is_active = ?
                WHERE inventory_item_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                inventoryItem.getItemCodeId(),
                inventoryItem.getUomId(),
                inventoryItem.getQuantityOnHand(),
                inventoryItem.getMaximumStockLevel(),
                inventoryItem.getAverageUnitPrice(),
                inventoryItem.getLastPurchasePrice(),
                inventoryItem.getRemarks(),
                inventoryItem.getIsActive(),
                inventoryItemId
        );
    }

    // ==============================
    // DELETE - Inventory Item
    // ==============================
    public int deleteInventoryItem(Integer inventoryItemId) {

        String sql = """
                DELETE FROM inventory_item
                WHERE inventory_item_id = ?
                """;

        return jdbcTemplate.update(sql, inventoryItemId);
    }

    // ==============================
    // Common Row Mapper
    // ==============================
    private InventoryItem mapInventoryItem(
            java.sql.ResultSet rs
    ) throws java.sql.SQLException {

        InventoryItem inventoryItem = new InventoryItem();

        inventoryItem.setInventoryItemId(
                rs.getInt("inventoryItemId")
        );

        inventoryItem.setItemCodeId(
                rs.getInt("itemCodeId")
        );

        inventoryItem.setUomId(
                rs.getInt("uomId")
        );

        inventoryItem.setQuantityOnHand(
                rs.getBigDecimal("quantityOnHand")
        );

        inventoryItem.setMaximumStockLevel(
                rs.getBigDecimal("maximumStockLevel")
        );

        inventoryItem.setAverageUnitPrice(
                rs.getBigDecimal("averageUnitPrice")
        );

        inventoryItem.setLastPurchasePrice(
                rs.getBigDecimal("lastPurchasePrice")
        );

        inventoryItem.setRemarks(
                rs.getString("remarks")
        );

        inventoryItem.setIsActive(
                rs.getBoolean("isActive")
        );

        return inventoryItem;
    }
}