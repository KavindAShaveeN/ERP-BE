package com.rr.erp.repository;

import com.rr.erp.dto.ItemCodeResponse;
import com.rr.erp.dto.ProjectWiseQuantityResponse;
import com.rr.erp.entity.ProjectStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class ProjectStoreRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProjectStoreRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /*
     * =========================================================
     * GOODS RECEIVED
     * Increase quantity_on_hand
     *
     * If project + item already exists:
     *      quantity_on_hand = quantity_on_hand + received quantity
     *
     * If it does not exist:
     *      create a new ProjectStore record
     * =========================================================
     */
    public void receiveGoods(
            String projectCode,
            String itemCode,
            BigDecimal quantity,
            LocalDate receivedDate
    ) {

        String sql = """
                INSERT INTO project_store (
                    project_code,
                    item_code,
                    quantity_on_hand,
                    last_received_date
                )
                VALUES (?, ?, ?, ?)
                ON CONFLICT (project_code, item_code)
                DO UPDATE SET
                    quantity_on_hand =
                        project_store.quantity_on_hand + EXCLUDED.quantity_on_hand,
                    last_received_date =
                        EXCLUDED.last_received_date
                """;

        jdbcTemplate.update(
                sql,
                projectCode,
                itemCode,
                quantity,
                receivedDate
        );
    }


    /*
     * =========================================================
     * GOODS ISSUED
     * Decrease quantity_on_hand
     *
     * Stock will only be reduced when enough stock exists.
     * =========================================================
     */
    public int issueGoods(
            String projectCode,
            String itemCode,
            BigDecimal quantity,
            LocalDate issuedDate
    ) {

        String sql = """
                UPDATE project_store
                SET
                    quantity_on_hand = quantity_on_hand - ?,
                    last_issued_date = ?
                WHERE project_code = ?
                  AND item_code = ?
                  AND quantity_on_hand >= ?
                """;

        return jdbcTemplate.update(
                sql,
                quantity,
                issuedDate,
                projectCode,
                itemCode,
                quantity
        );
    }


    /*
     * =========================================================
     * REVERSE GOODS RECEIVED
     * Decrease quantity_on_hand unconditionally — used only to undo a
     * production record's own just-opened batch during a reversal, where
     * the caller (StockBatchService#reverseOwnBatch) has already verified
     * the full quantity is still on hand, so no sufficiency guard is needed
     * here the way issueGoods needs one for a normal debit.
     * =========================================================
     */
    public void reverseReceivedGoods(
            String projectCode,
            String itemCode,
            BigDecimal quantity
    ) {

        String sql = """
                UPDATE project_store
                SET quantity_on_hand = quantity_on_hand - ?
                WHERE project_code = ?
                  AND item_code = ?
                """;

        jdbcTemplate.update(sql, quantity, projectCode, itemCode);
    }


    /*
     * =========================================================
     * GET CURRENT QUANTITY
     * =========================================================
     */
    public Optional<BigDecimal> getQuantityOnHand(
            String projectCode,
            String itemCode
    ) {

        String sql = """
                SELECT
                    quantity_on_hand AS "quantityOnHand"
                FROM project_store
                WHERE project_code = ?
                  AND item_code = ?
                """;

        return jdbcTemplate.query(
                sql,
                rs -> {
                    if (rs.next()) {
                        return Optional.ofNullable(
                                rs.getBigDecimal("quantityOnHand")
                        );
                    }

                    return Optional.empty();
                },
                projectCode,
                itemCode
        );
    }


    /*
     * =========================================================
     * GET PROJECT STORE ITEM
     * =========================================================
     */
    public Optional<ProjectStore> getProjectStoreItem(
            String projectCode,
            String itemCode
    ) {

        String sql = """
                SELECT
                    project_code AS "projectCode",
                    item_code AS "itemCode",
                    quantity_on_hand AS "quantityOnHand",
                    reorder_level AS "reorderLevel",
                    reorder_quantity AS "reorderQuantity",
                    bin_location AS "binLocation",
                    last_received_date AS "lastReceivedDate",
                    last_issued_date AS "lastIssuedDate"
                FROM project_store
                WHERE project_code = ?
                  AND item_code = ?
                """;

        return jdbcTemplate.query(
                sql,
                rs -> {

                    if (!rs.next()) {
                        return Optional.empty();
                    }

                    ProjectStore projectStore = new ProjectStore();

                    projectStore.setProjectCode(
                            rs.getString("projectCode")
                    );

                    projectStore.setItemCode(
                            rs.getString("itemCode")
                    );

                    projectStore.setQuantityOnHand(
                            rs.getBigDecimal("quantityOnHand")
                    );

                    projectStore.setReorderLevel(
                            rs.getBigDecimal("reorderLevel")
                    );

                    projectStore.setReorderQuantity(
                            rs.getBigDecimal("reorderQuantity")
                    );

                    projectStore.setBinLocation(
                            rs.getString("binLocation")
                    );

                    if (rs.getDate("lastReceivedDate") != null) {
                        projectStore.setLastReceivedDate(
                                rs.getDate("lastReceivedDate").toLocalDate()
                        );
                    }

                    if (rs.getDate("lastIssuedDate") != null) {
                        projectStore.setLastIssuedDate(
                                rs.getDate("lastIssuedDate").toLocalDate()
                        );
                    }

                    return Optional.of(projectStore);
                },
                projectCode,
                itemCode
        );
    }

    public List<ItemCodeResponse> getItemCodesByProjectCode(String projectCode) {

        String sql = """
                    SELECT
                        ps.item_code,
                        i.item_code_name,
                        COALESCE(ci.uom_id, ii.uom_id, nsi.uom_id) AS uom_id,
                        ps.quantity_on_hand,
                        ps.reorder_level,
                        ps.reorder_quantity,
                        ps.bin_location
                    FROM project_store ps
                    LEFT JOIN item_code i
                        ON ps.item_code = i.item_code_code
                    LEFT JOIN consumable_item ci
                        ON i.item_code_id = ci.item_code_id
                    LEFT JOIN inventory_item ii
                        ON i.item_code_id = ii.item_code_id
                    LEFT JOIN non_stock_item nsi
                        ON i.item_code_id = nsi.item_code_id
                    WHERE ps.project_code = ?
                    ORDER BY ps.item_code
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    ItemCodeResponse response = new ItemCodeResponse();

                    response.setItemCode(rs.getString("item_code"));
                    response.setItemCodeName(rs.getString("item_code_name"));
                    response.setUomId(rs.getObject("uom_id", Integer.class));
                    response.setQuantityOnHand(rs.getBigDecimal("quantity_on_hand"));
                    response.setReorderLevel(rs.getBigDecimal("reorder_level"));
                    response.setReorderQuantity(rs.getBigDecimal("reorder_quantity"));
                    response.setBinLocation(rs.getString("bin_location"));

                    return response;
                },
                projectCode
        );
    }


    /*
     * =========================================================
     * SET REORDER LEVEL + REORDER QUANTITY FOR A PROJECT + ITEM
     *
     * Upserts so reorder settings can be set for an item that has not
     * yet had any stock received into this project's store — the row
     * starts at 0 quantity_on_hand until goods actually arrive.
     * =========================================================
     */
    public void setReorderLevel(
            String projectCode,
            String itemCode,
            BigDecimal reorderLevel,
            BigDecimal reorderQuantity
    ) {

        String sql = """
                INSERT INTO project_store (
                    project_code,
                    item_code,
                    quantity_on_hand,
                    reorder_level,
                    reorder_quantity
                )
                VALUES (?, ?, 0, ?, ?)
                ON CONFLICT (project_code, item_code)
                DO UPDATE SET
                    reorder_level = EXCLUDED.reorder_level,
                    reorder_quantity = EXCLUDED.reorder_quantity
                """;

        jdbcTemplate.update(
                sql,
                projectCode,
                itemCode,
                reorderLevel,
                reorderQuantity
        );
    }


    /*
     * =========================================================
     * SET BIN LOCATION FOR A PROJECT + ITEM
     *
     * Where the item physically sits in this project's store. Upserts
     * like setReorderLevel, so a bin can be assigned before any stock
     * has been received into this project's store.
     * =========================================================
     */
    public void setBinLocation(
            String projectCode,
            String itemCode,
            String binLocation
    ) {

        String sql = """
                INSERT INTO project_store (
                    project_code,
                    item_code,
                    quantity_on_hand,
                    bin_location
                )
                VALUES (?, ?, 0, ?)
                ON CONFLICT (project_code, item_code)
                DO UPDATE SET
                    bin_location = EXCLUDED.bin_location
                """;

        jdbcTemplate.update(
                sql,
                projectCode,
                itemCode,
                binLocation
        );
    }


    /*
     * =========================================================
     * GET ITEM QUANTITIES ACROSS ALL PROJECTS (SUMMED)
     * Used for the HQ-Colombo "all projects" stock view.
     * =========================================================
     */
    public List<ItemCodeResponse> getAllItemQuantities(Integer projectTypeId) {

        String sql = """
                    SELECT
                        ps.item_code,
                        i.item_code_name,
                        COALESCE(ci.uom_id, ii.uom_id, nsi.uom_id) AS uom_id,
                        SUM(ps.quantity_on_hand) AS quantity_on_hand
                    FROM project_store ps
                    JOIN project pr
                        ON pr.project_code = ps.project_code
                    LEFT JOIN item_code i
                        ON ps.item_code = i.item_code_code
                    LEFT JOIN consumable_item ci
                        ON i.item_code_id = ci.item_code_id
                    LEFT JOIN inventory_item ii
                        ON i.item_code_id = ii.item_code_id
                    LEFT JOIN non_stock_item nsi
                        ON i.item_code_id = nsi.item_code_id
                    WHERE ?::int4 IS NULL OR pr.project_type_id = ?
                    GROUP BY ps.item_code, i.item_code_name, ci.uom_id, ii.uom_id, nsi.uom_id
                    ORDER BY ps.item_code
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    ItemCodeResponse response = new ItemCodeResponse();

                    response.setItemCode(rs.getString("item_code"));
                    response.setItemCodeName(rs.getString("item_code_name"));
                    response.setUomId(rs.getObject("uom_id", Integer.class));
                    response.setQuantityOnHand(rs.getBigDecimal("quantity_on_hand"));

                    return response;
                },
                projectTypeId, projectTypeId
        );
    }


    /*
     * =========================================================
     * GET PROJECT WISE QUANTITY FOR AN ITEM CODE
     * =========================================================
     */
    public List<ProjectWiseQuantityResponse> getProjectWiseQuantityByItemCode(String itemCode) {

        String sql = """
                    SELECT
                        p.project_code,
                        p.projectName AS project_name,
                        p.project_type_id AS project_type_id,
                        pt.project_type_name AS project_type_name,
                        COALESCE(ci.uom_id, ii.uom_id, nsi.uom_id) AS uom_id,
                        ps.quantity_on_hand,
                        ps.reorder_level,
                        ps.reorder_quantity,
                        ps.bin_location
                    FROM project_store ps
                    JOIN project p
                        ON ps.project_code = p.project_code
                    LEFT JOIN project_type pt
                        ON pt.project_type_id = p.project_type_id
                    LEFT JOIN item_code i
                        ON ps.item_code = i.item_code_code
                    LEFT JOIN consumable_item ci
                        ON i.item_code_id = ci.item_code_id
                    LEFT JOIN inventory_item ii
                        ON i.item_code_id = ii.item_code_id
                    LEFT JOIN non_stock_item nsi
                        ON i.item_code_id = nsi.item_code_id
                    WHERE ps.item_code = ?
                    ORDER BY p.project_code
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    ProjectWiseQuantityResponse response = new ProjectWiseQuantityResponse();

                    response.setProjectCode(rs.getString("project_code"));
                    response.setProjectName(rs.getString("project_name"));
                    response.setProjectTypeId(rs.getObject("project_type_id", Integer.class));
                    response.setProjectTypeName(rs.getString("project_type_name"));
                    response.setUomId(rs.getObject("uom_id", Integer.class));
                    response.setQuantityOnHand(rs.getBigDecimal("quantity_on_hand"));
                    response.setReorderLevel(rs.getBigDecimal("reorder_level"));
                    response.setReorderQuantity(rs.getBigDecimal("reorder_quantity"));
                    response.setBinLocation(rs.getString("bin_location"));

                    return response;
                },
                itemCode
        );
    }
}