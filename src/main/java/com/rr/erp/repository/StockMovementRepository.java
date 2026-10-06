package com.rr.erp.repository;

import com.rr.erp.dto.StockMovementResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Read-only aggregation over the document tables that actually move stock in and out of a
 * project's store — used to build a per-item transaction history and the lifetime
 * received/issued totals shown alongside {@code project_store.quantity_on_hand}. Nothing here
 * writes to project_store; see ProjectStoreRepository for that.
 *
 * Every query here (other than PO, which never moves stock on its own) filters to only the
 * approved/authorized rows: every service in this codebase only calls
 * ProjectStoreService.receiveGoods/issueGoods on the Pending -> Approved (or
 * Submitted -> Authorized, or Submitted -> Approved for plant production) transition — a
 * still-Pending document has not touched the ledger yet, so counting it here would overstate
 * these totals relative to the real quantity_on_hand.
 */
@Repository
public class StockMovementRepository {

    private final JdbcTemplate jdbcTemplate;

    public StockMovementRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<StockMovementResponse> MOVEMENT_MAPPER = (rs, rowNum) -> {
        StockMovementResponse response = new StockMovementResponse();
        response.setType(rs.getString("type"));
        response.setDocCode(rs.getString("doc_code"));
        response.setDate(rs.getDate("movement_date") != null ? rs.getDate("movement_date").toLocalDate() : null);
        response.setProjectCode(rs.getString("project_code"));
        response.setProjectName(rs.getString("project_name"));
        response.setCounterpartyProjectCode(rs.getString("counterparty_project_code"));
        response.setCounterpartyProjectName(rs.getString("counterparty_project_name"));
        response.setDirection(rs.getString("direction"));
        response.setQuantity(rs.getBigDecimal("quantity"));
        response.setUomId(rs.getObject("uom_id", Integer.class));
        response.setIsApproved(rs.getObject("is_approved", Boolean.class));
        response.setRemarks(rs.getString("remarks"));
        response.setSupplierCode(rs.getString("supplier_code"));
        response.setSupplierName(rs.getString("supplier_name"));
        response.setUnitPrice(rs.getBigDecimal("unit_price"));
        response.setAmount(rs.getBigDecimal("amount"));
        return response;
    };

    /*
     * =========================================================
     * ONE PROJECT'S HISTORY FOR ONE ITEM
     * =========================================================
     */
    public List<StockMovementResponse> getHistory(String projectCode, String itemCode) {

        List<StockMovementResponse> movements = new ArrayList<>();
        movements.addAll(getPoHistory(projectCode, itemCode));
        movements.addAll(getGrnHistory(projectCode, itemCode));
        movements.addAll(getGinHistory(projectCode, itemCode));
        movements.addAll(getIntraProjectIssueHistory(projectCode, itemCode));
        movements.addAll(getIntraProjectIssueReturnHistory(projectCode, itemCode));
        movements.addAll(getStockReturnHistory(projectCode, itemCode));
        movements.addAll(getStockAdjustmentHistory(projectCode, itemCode));
        movements.addAll(getPlantProductionOutputHistory(projectCode, itemCode));
        movements.addAll(getPlantProductionConsumptionHistory(projectCode, itemCode));
        movements.addAll(getJobIssueHistory(projectCode, itemCode));
        movements.addAll(getJobIssueReturnHistory(projectCode, itemCode));
        movements.addAll(getFuelIssueHistory(projectCode, itemCode));
        movements.sort(Comparator.comparing(
                StockMovementResponse::getDate,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));
        return movements;
    }

    /*
     * =========================================================
     * EVERY PROJECT'S HISTORY FOR ONE ITEM (HQ) — optionally
     * restricted to projects of one project type.
     * =========================================================
     */
    public List<StockMovementResponse> getHistoryAllProjects(String itemCode, Integer projectTypeId) {

        List<StockMovementResponse> movements = new ArrayList<>();
        movements.addAll(getPoHistoryAllProjects(itemCode, projectTypeId));
        movements.addAll(getGrnHistoryAllProjects(itemCode, projectTypeId));
        movements.addAll(getGinHistoryAllProjects(itemCode, projectTypeId));
        movements.addAll(getIntraProjectIssueHistoryAllProjects(itemCode, projectTypeId));
        movements.addAll(getIntraProjectIssueReturnHistoryAllProjects(itemCode, projectTypeId));
        movements.addAll(getStockReturnHistoryAllProjects(itemCode, projectTypeId));
        movements.addAll(getStockAdjustmentHistoryAllProjects(itemCode, projectTypeId));
        movements.addAll(getPlantProductionOutputHistoryAllProjects(itemCode, projectTypeId));
        movements.addAll(getPlantProductionConsumptionHistoryAllProjects(itemCode, projectTypeId));
        movements.addAll(getJobIssueHistoryAllProjects(itemCode, projectTypeId));
        movements.addAll(getJobIssueReturnHistoryAllProjects(itemCode, projectTypeId));
        movements.addAll(getFuelIssueHistoryAllProjects(itemCode, projectTypeId));
        movements.sort(Comparator.comparing(
                StockMovementResponse::getDate,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));
        return movements;
    }

    public List<StockMovementResponse> getPoHistory(String projectCode, String itemCode) {
        String sql = """
                SELECT
                    'PO' AS type,
                    po.po_code AS doc_code,
                    po.po_date AS movement_date,
                    po.project_code AS project_code,
                    p.projectname AS project_name,
                    NULL AS counterparty_project_code,
                    NULL AS counterparty_project_name,
                    'NEUTRAL' AS direction,
                    pi.quantity AS quantity,
                    pi.uom_id AS uom_id,
                    po.is_approved AS is_approved,
                    po.remarks AS remarks,
                    po.supplier_code AS supplier_code,
                    s.suppliername AS supplier_name,
                    pi.unit_price AS unit_price,
                    COALESCE(pi.amount, pi.quantity * pi.unit_price) AS amount
                FROM po
                JOIN po_item pi ON pi.po_id = po.po_id
                LEFT JOIN project p ON p.project_code = po.project_code
                LEFT JOIN supplier s ON s.suppliercode = po.supplier_code
                WHERE po.project_code = ? AND pi.item_code = ?
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, projectCode, itemCode);
    }

    private List<StockMovementResponse> getPoHistoryAllProjects(String itemCode, Integer projectTypeId) {
        String sql = """
                SELECT
                    'PO' AS type,
                    po.po_code AS doc_code,
                    po.po_date AS movement_date,
                    po.project_code AS project_code,
                    p.projectname AS project_name,
                    NULL AS counterparty_project_code,
                    NULL AS counterparty_project_name,
                    'NEUTRAL' AS direction,
                    pi.quantity AS quantity,
                    pi.uom_id AS uom_id,
                    po.is_approved AS is_approved,
                    po.remarks AS remarks,
                    po.supplier_code AS supplier_code,
                    s.suppliername AS supplier_name,
                    pi.unit_price AS unit_price,
                    COALESCE(pi.amount, pi.quantity * pi.unit_price) AS amount
                FROM po
                JOIN po_item pi ON pi.po_id = po.po_id
                JOIN project p ON p.project_code = po.project_code
                LEFT JOIN supplier s ON s.suppliercode = po.supplier_code
                WHERE pi.item_code = ?
                  AND (?::int4 IS NULL OR p.project_type_id = ?)
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, itemCode, projectTypeId, projectTypeId);
    }

    public List<StockMovementResponse> getGrnHistory(String projectCode, String itemCode) {
        // A supplier GRN's to_project_code is always the fixed HQ-COLOMBO routing destination
        // (see useHqScope.ts on the frontend), NOT the project the goods are actually for —
        // that's from_project_code, the requesting/owning project. An internal-transfer GRN
        // (is_supplier_grn = false) has no such fixed destination, so its to_project_code IS
        // the real receiving project there. This CASE picks the right column either way, and
        // the counterparty is the supplier (already carried via supplier_code/supplier_name)
        // for a supplier GRN, or the sending project for an internal transfer. Only an
        // approved GRN has actually credited project_store (see GRNService).
        String sql = """
                SELECT
                    'GRN' AS type,
                    grn.grn_code AS doc_code,
                    grn.grn_date AS movement_date,
                    CASE WHEN grn.is_supplier_grn THEN grn.from_project_code ELSE grn.to_project_code END AS project_code,
                    p.projectname AS project_name,
                    CASE WHEN grn.is_supplier_grn THEN NULL ELSE grn.from_project_code END AS counterparty_project_code,
                    cp.projectname AS counterparty_project_name,
                    'IN' AS direction,
                    gi.quantity AS quantity,
                    gi.uom_id AS uom_id,
                    grn.is_approved AS is_approved,
                    NULL AS remarks,
                    grn.supplier_code AS supplier_code,
                    s.suppliername AS supplier_name,
                    gi.unit_price AS unit_price,
                    COALESCE(gi.amount, gi.quantity * gi.unit_price) AS amount
                FROM grn
                JOIN grn_item gi ON gi.grn_id = grn.grn_id
                LEFT JOIN project p ON p.project_code =
                    CASE WHEN grn.is_supplier_grn THEN grn.from_project_code ELSE grn.to_project_code END
                LEFT JOIN project cp ON cp.project_code =
                    CASE WHEN grn.is_supplier_grn THEN NULL ELSE grn.from_project_code END
                LEFT JOIN supplier s ON s.suppliercode = grn.supplier_code
                WHERE (CASE WHEN grn.is_supplier_grn THEN grn.from_project_code ELSE grn.to_project_code END) = ?
                  AND gi.item_code = ?
                  AND grn.is_approved = TRUE
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, projectCode, itemCode);
    }

    private List<StockMovementResponse> getGrnHistoryAllProjects(String itemCode, Integer projectTypeId) {
        String sql = """
                SELECT
                    'GRN' AS type,
                    grn.grn_code AS doc_code,
                    grn.grn_date AS movement_date,
                    CASE WHEN grn.is_supplier_grn THEN grn.from_project_code ELSE grn.to_project_code END AS project_code,
                    p.projectname AS project_name,
                    CASE WHEN grn.is_supplier_grn THEN NULL ELSE grn.from_project_code END AS counterparty_project_code,
                    cp.projectname AS counterparty_project_name,
                    'IN' AS direction,
                    gi.quantity AS quantity,
                    gi.uom_id AS uom_id,
                    grn.is_approved AS is_approved,
                    NULL AS remarks,
                    grn.supplier_code AS supplier_code,
                    s.suppliername AS supplier_name,
                    gi.unit_price AS unit_price,
                    COALESCE(gi.amount, gi.quantity * gi.unit_price) AS amount
                FROM grn
                JOIN grn_item gi ON gi.grn_id = grn.grn_id
                JOIN project p ON p.project_code =
                    CASE WHEN grn.is_supplier_grn THEN grn.from_project_code ELSE grn.to_project_code END
                LEFT JOIN project cp ON cp.project_code =
                    CASE WHEN grn.is_supplier_grn THEN NULL ELSE grn.from_project_code END
                LEFT JOIN supplier s ON s.suppliercode = grn.supplier_code
                WHERE gi.item_code = ?
                  AND grn.is_approved = TRUE
                  AND (?::int4 IS NULL OR p.project_type_id = ?)
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, itemCode, projectTypeId, projectTypeId);
    }

    public List<StockMovementResponse> getGinHistory(String projectCode, String itemCode) {
        String sql = """
                SELECT
                    'GIN' AS type,
                    gin.gin_code AS doc_code,
                    gin.issued_date::date AS movement_date,
                    ? AS project_code,
                    reportedProject.projectname AS project_name,
                    CASE WHEN gin.issued_project_code = ? THEN gin.received_project_code ELSE gin.issued_project_code END AS counterparty_project_code,
                    cp.projectname AS counterparty_project_name,
                    CASE WHEN gin.issued_project_code = ? THEN 'OUT' ELSE 'IN' END AS direction,
                    gi.quantity AS quantity,
                    gi.uom_id AS uom_id,
                    gin.is_authorized AS is_approved,
                    gi.remarks AS remarks,
                    NULL AS supplier_code,
                    NULL AS supplier_name,
                    gi.unit_price AS unit_price,
                    COALESCE(gi.amount, gi.quantity * gi.unit_price) AS amount
                FROM gin
                JOIN gin_item gi ON gi.gin_id = gin.gin_id
                LEFT JOIN project reportedProject ON reportedProject.project_code = ?
                LEFT JOIN project cp ON cp.project_code =
                    CASE WHEN gin.issued_project_code = ? THEN gin.received_project_code ELSE gin.issued_project_code END
                WHERE (gin.issued_project_code = ? OR gin.received_project_code = ?) AND gi.item_code = ?
                  AND gin.is_authorized = TRUE
                """;
        return jdbcTemplate.query(
                sql, MOVEMENT_MAPPER,
                projectCode, projectCode, projectCode, projectCode, projectCode,
                projectCode, projectCode, itemCode
        );
    }

    private List<StockMovementResponse> getGinHistoryAllProjects(String itemCode, Integer projectTypeId) {
        String sql = """
                SELECT
                    'GIN' AS type,
                    gin.gin_code AS doc_code,
                    gin.issued_date::date AS movement_date,
                    gin.issued_project_code AS project_code,
                    p.projectname AS project_name,
                    gin.received_project_code AS counterparty_project_code,
                    cp.projectname AS counterparty_project_name,
                    'OUT' AS direction,
                    gi.quantity AS quantity,
                    gi.uom_id AS uom_id,
                    gin.is_authorized AS is_approved,
                    gi.remarks AS remarks,
                    NULL AS supplier_code,
                    NULL AS supplier_name,
                    gi.unit_price AS unit_price,
                    COALESCE(gi.amount, gi.quantity * gi.unit_price) AS amount
                FROM gin
                JOIN gin_item gi ON gi.gin_id = gin.gin_id
                JOIN project p ON p.project_code = gin.issued_project_code
                LEFT JOIN project cp ON cp.project_code = gin.received_project_code
                WHERE gi.item_code = ?
                  AND gin.is_authorized = TRUE
                  AND (?::int4 IS NULL OR p.project_type_id = ?)
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, itemCode, projectTypeId, projectTypeId);
    }

    public List<StockMovementResponse> getIntraProjectIssueHistory(String projectCode, String itemCode) {
        String sql = """
                SELECT
                    'INTRA_ISSUE' AS type,
                    issue.intra_project_issue_code AS doc_code,
                    issue.issued_date::date AS movement_date,
                    issue.issued_project_code AS project_code,
                    p.projectname AS project_name,
                    NULL AS counterparty_project_code,
                    NULL AS counterparty_project_name,
                    'OUT' AS direction,
                    item.quantity AS quantity,
                    item.uom_id AS uom_id,
                    issue.is_authorized AS is_approved,
                    item.remarks AS remarks,
                    NULL AS supplier_code,
                    NULL AS supplier_name,
                    item.unit_price AS unit_price,
                    COALESCE(item.amount, item.quantity * item.unit_price) AS amount
                FROM intra_project_issue issue
                JOIN intra_project_issue_item item ON item.intra_project_issue_id = issue.intra_project_issue_id
                LEFT JOIN project p ON p.project_code = issue.issued_project_code
                WHERE issue.issued_project_code = ? AND item.item_code = ?
                  AND issue.is_authorized = TRUE
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, projectCode, itemCode);
    }

    private List<StockMovementResponse> getIntraProjectIssueHistoryAllProjects(String itemCode, Integer projectTypeId) {
        String sql = """
                SELECT
                    'INTRA_ISSUE' AS type,
                    issue.intra_project_issue_code AS doc_code,
                    issue.issued_date::date AS movement_date,
                    issue.issued_project_code AS project_code,
                    p.projectname AS project_name,
                    NULL AS counterparty_project_code,
                    NULL AS counterparty_project_name,
                    'OUT' AS direction,
                    item.quantity AS quantity,
                    item.uom_id AS uom_id,
                    issue.is_authorized AS is_approved,
                    item.remarks AS remarks,
                    NULL AS supplier_code,
                    NULL AS supplier_name,
                    item.unit_price AS unit_price,
                    COALESCE(item.amount, item.quantity * item.unit_price) AS amount
                FROM intra_project_issue issue
                JOIN intra_project_issue_item item ON item.intra_project_issue_id = issue.intra_project_issue_id
                JOIN project p ON p.project_code = issue.issued_project_code
                WHERE item.item_code = ?
                  AND issue.is_authorized = TRUE
                  AND (?::int4 IS NULL OR p.project_type_id = ?)
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, itemCode, projectTypeId, projectTypeId);
    }

    public List<StockMovementResponse> getIntraProjectIssueReturnHistory(String projectCode, String itemCode) {
        String sql = """
                SELECT
                    'INTRA_ISSUE_RETURN' AS type,
                    ret.intra_project_issue_return_code AS doc_code,
                    ret.return_date AS movement_date,
                    ret.issued_project_code AS project_code,
                    p.projectname AS project_name,
                    NULL AS counterparty_project_code,
                    NULL AS counterparty_project_name,
                    'IN' AS direction,
                    item.quantity AS quantity,
                    item.uom_id AS uom_id,
                    ret.is_approved AS is_approved,
                    item.remarks AS remarks,
                    NULL AS supplier_code,
                    NULL AS supplier_name,
                    item.unit_price AS unit_price,
                    COALESCE(item.amount, item.quantity * item.unit_price) AS amount
                FROM intra_project_issue_return ret
                JOIN intra_project_issue_return_item item ON item.intra_project_issue_return_id = ret.intra_project_issue_return_id
                LEFT JOIN project p ON p.project_code = ret.issued_project_code
                WHERE ret.issued_project_code = ? AND item.item_code = ?
                  AND ret.is_approved = TRUE
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, projectCode, itemCode);
    }

    private List<StockMovementResponse> getIntraProjectIssueReturnHistoryAllProjects(String itemCode, Integer projectTypeId) {
        String sql = """
                SELECT
                    'INTRA_ISSUE_RETURN' AS type,
                    ret.intra_project_issue_return_code AS doc_code,
                    ret.return_date AS movement_date,
                    ret.issued_project_code AS project_code,
                    p.projectname AS project_name,
                    NULL AS counterparty_project_code,
                    NULL AS counterparty_project_name,
                    'IN' AS direction,
                    item.quantity AS quantity,
                    item.uom_id AS uom_id,
                    ret.is_approved AS is_approved,
                    item.remarks AS remarks,
                    NULL AS supplier_code,
                    NULL AS supplier_name,
                    item.unit_price AS unit_price,
                    COALESCE(item.amount, item.quantity * item.unit_price) AS amount
                FROM intra_project_issue_return ret
                JOIN intra_project_issue_return_item item ON item.intra_project_issue_return_id = ret.intra_project_issue_return_id
                JOIN project p ON p.project_code = ret.issued_project_code
                WHERE item.item_code = ?
                  AND ret.is_approved = TRUE
                  AND (?::int4 IS NULL OR p.project_type_id = ?)
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, itemCode, projectTypeId, projectTypeId);
    }

    public List<StockMovementResponse> getStockReturnHistory(String projectCode, String itemCode) {
        String sql = """
                SELECT
                    'STOCK_RETURN' AS type,
                    sr.stock_return_code AS doc_code,
                    sr.return_date::date AS movement_date,
                    ? AS project_code,
                    reportedProject.projectname AS project_name,
                    CASE WHEN sr.from_project_code = ? THEN sr.to_project_code ELSE sr.from_project_code END AS counterparty_project_code,
                    cp.projectname AS counterparty_project_name,
                    CASE WHEN sr.from_project_code = ? THEN 'OUT' ELSE 'IN' END AS direction,
                    item.quantity AS quantity,
                    item.uom_id AS uom_id,
                    sr.is_approved AS is_approved,
                    item.remarks AS remarks,
                    sr.supplier_code AS supplier_code,
                    s.suppliername AS supplier_name,
                    item.unit_price AS unit_price,
                    COALESCE(item.amount, item.quantity * item.unit_price) AS amount
                FROM stock_return sr
                JOIN stock_return_item item ON item.stock_return_id = sr.stock_return_id
                LEFT JOIN project reportedProject ON reportedProject.project_code = ?
                LEFT JOIN project cp ON cp.project_code =
                    CASE WHEN sr.from_project_code = ? THEN sr.to_project_code ELSE sr.from_project_code END
                LEFT JOIN supplier s ON s.suppliercode = sr.supplier_code
                WHERE (sr.from_project_code = ? OR sr.to_project_code = ?) AND item.item_code = ?
                  AND sr.is_approved = TRUE
                """;
        return jdbcTemplate.query(
                sql, MOVEMENT_MAPPER,
                projectCode, projectCode, projectCode, projectCode, projectCode,
                projectCode, projectCode, itemCode
        );
    }

    private List<StockMovementResponse> getStockReturnHistoryAllProjects(String itemCode, Integer projectTypeId) {
        String sql = """
                SELECT
                    'STOCK_RETURN' AS type,
                    sr.stock_return_code AS doc_code,
                    sr.return_date::date AS movement_date,
                    sr.from_project_code AS project_code,
                    p.projectname AS project_name,
                    sr.to_project_code AS counterparty_project_code,
                    cp.projectname AS counterparty_project_name,
                    'OUT' AS direction,
                    item.quantity AS quantity,
                    item.uom_id AS uom_id,
                    sr.is_approved AS is_approved,
                    item.remarks AS remarks,
                    sr.supplier_code AS supplier_code,
                    s.suppliername AS supplier_name,
                    item.unit_price AS unit_price,
                    COALESCE(item.amount, item.quantity * item.unit_price) AS amount
                FROM stock_return sr
                JOIN stock_return_item item ON item.stock_return_id = sr.stock_return_id
                JOIN project p ON p.project_code = sr.from_project_code
                LEFT JOIN project cp ON cp.project_code = sr.to_project_code
                LEFT JOIN supplier s ON s.suppliercode = sr.supplier_code
                WHERE item.item_code = ?
                  AND sr.is_approved = TRUE
                  AND (?::int4 IS NULL OR p.project_type_id = ?)
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, itemCode, projectTypeId, projectTypeId);
    }

    public List<StockMovementResponse> getStockAdjustmentHistory(String projectCode, String itemCode) {
        String sql = """
                SELECT
                    'ADJUSTMENT' AS type,
                    adj.stock_adjustment_code AS doc_code,
                    adj.adjustment_date::date AS movement_date,
                    adj.project_code AS project_code,
                    p.projectname AS project_name,
                    NULL AS counterparty_project_code,
                    NULL AS counterparty_project_name,
                    CASE WHEN item.adjustment_quantity >= 0 THEN 'IN' ELSE 'OUT' END AS direction,
                    ABS(item.adjustment_quantity) AS quantity,
                    item.uom_id AS uom_id,
                    adj.is_approved AS is_approved,
                    COALESCE(item.remarks, adj.reason) AS remarks,
                    NULL AS supplier_code,
                    NULL AS supplier_name,
                    item.unit_price AS unit_price,
                    COALESCE(ABS(item.adjustment_value), ABS(item.adjustment_quantity) * item.unit_price) AS amount
                FROM stock_adjustment adj
                JOIN stock_adjustment_item item ON item.stock_adjustment_id = adj.stock_adjustment_id
                LEFT JOIN project p ON p.project_code = adj.project_code
                WHERE adj.project_code = ? AND item.item_code = ?
                  AND adj.is_approved = TRUE
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, projectCode, itemCode);
    }

    private List<StockMovementResponse> getStockAdjustmentHistoryAllProjects(String itemCode, Integer projectTypeId) {
        String sql = """
                SELECT
                    'ADJUSTMENT' AS type,
                    adj.stock_adjustment_code AS doc_code,
                    adj.adjustment_date::date AS movement_date,
                    adj.project_code AS project_code,
                    p.projectname AS project_name,
                    NULL AS counterparty_project_code,
                    NULL AS counterparty_project_name,
                    CASE WHEN item.adjustment_quantity >= 0 THEN 'IN' ELSE 'OUT' END AS direction,
                    ABS(item.adjustment_quantity) AS quantity,
                    item.uom_id AS uom_id,
                    adj.is_approved AS is_approved,
                    COALESCE(item.remarks, adj.reason) AS remarks,
                    NULL AS supplier_code,
                    NULL AS supplier_name,
                    item.unit_price AS unit_price,
                    COALESCE(ABS(item.adjustment_value), ABS(item.adjustment_quantity) * item.unit_price) AS amount
                FROM stock_adjustment adj
                JOIN stock_adjustment_item item ON item.stock_adjustment_id = adj.stock_adjustment_id
                JOIN project p ON p.project_code = adj.project_code
                WHERE item.item_code = ?
                  AND adj.is_approved = TRUE
                  AND (?::int4 IS NULL OR p.project_type_id = ?)
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, itemCode, projectTypeId, projectTypeId);
    }

    /*
     * =========================================================
     * PLANT PRODUCTION — finished goods produced (IN) and raw
     * materials consumed (OUT). Neither ever goes through a GRN
     * or GIN: a plant production record moves both sides of its
     * own project's store directly (see PlantProductionService),
     * only on the Submitted -> Approved transition, and only for
     * a production still in APPROVED status (a REVERSED one has
     * had both sides undone, so it must not count here either).
     * =========================================================
     */
    public List<StockMovementResponse> getPlantProductionOutputHistory(String projectCode, String itemCode) {
        // The finished-goods batch this output opened carries the unit cost the Java layer
        // computed for it (raw material cost + other expenses, apportioned pro-rata across
        // every non-waste output — see PlantProductionService#applyStockMovements), keyed by
        // source_type='PLANT_PRODUCTION' + source_id=production_id + item_code. Summing
        // original_qty*unit_cost across matching batches (normally just one) and dividing back
        // by the produced quantity is more robust than reading a single row's unit_cost in case
        // a dimensional item ever splits into more than one batch for the same output line.
        String sql = """
                SELECT
                    'PLANT_PRODUCTION' AS type,
                    pp.production_code AS doc_code,
                    pp.production_date AS movement_date,
                    pp.project_code AS project_code,
                    p.projectname AS project_name,
                    NULL AS counterparty_project_code,
                    NULL AS counterparty_project_name,
                    'IN' AS direction,
                    ppo.produced_quantity AS quantity,
                    ppo.uom_id AS uom_id,
                    (pp.status = 'APPROVED') AS is_approved,
                    pp.remarks AS remarks,
                    NULL AS supplier_code,
                    NULL AS supplier_name,
                    CASE WHEN ppo.produced_quantity > 0 THEN batch_cost.total_cost / ppo.produced_quantity END AS unit_price,
                    batch_cost.total_cost AS amount
                FROM plant_production pp
                JOIN plant_production_output ppo ON ppo.production_id = pp.production_id
                LEFT JOIN project p ON p.project_code = pp.project_code
                LEFT JOIN LATERAL (
                    SELECT SUM(sb.original_qty * sb.unit_cost) AS total_cost
                    FROM stock_batch sb
                    WHERE sb.source_type = 'PLANT_PRODUCTION'
                      AND sb.source_id = pp.production_id
                      AND sb.item_code = ppo.item_code
                ) batch_cost ON TRUE
                WHERE pp.project_code = ? AND ppo.item_code = ?
                  AND pp.status = 'APPROVED'
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, projectCode, itemCode);
    }

    private List<StockMovementResponse> getPlantProductionOutputHistoryAllProjects(String itemCode, Integer projectTypeId) {
        String sql = """
                SELECT
                    'PLANT_PRODUCTION' AS type,
                    pp.production_code AS doc_code,
                    pp.production_date AS movement_date,
                    pp.project_code AS project_code,
                    p.projectname AS project_name,
                    NULL AS counterparty_project_code,
                    NULL AS counterparty_project_name,
                    'IN' AS direction,
                    ppo.produced_quantity AS quantity,
                    ppo.uom_id AS uom_id,
                    (pp.status = 'APPROVED') AS is_approved,
                    pp.remarks AS remarks,
                    NULL AS supplier_code,
                    NULL AS supplier_name,
                    CASE WHEN ppo.produced_quantity > 0 THEN batch_cost.total_cost / ppo.produced_quantity END AS unit_price,
                    batch_cost.total_cost AS amount
                FROM plant_production pp
                JOIN plant_production_output ppo ON ppo.production_id = pp.production_id
                JOIN project p ON p.project_code = pp.project_code
                LEFT JOIN LATERAL (
                    SELECT SUM(sb.original_qty * sb.unit_cost) AS total_cost
                    FROM stock_batch sb
                    WHERE sb.source_type = 'PLANT_PRODUCTION'
                      AND sb.source_id = pp.production_id
                      AND sb.item_code = ppo.item_code
                ) batch_cost ON TRUE
                WHERE ppo.item_code = ?
                  AND pp.status = 'APPROVED'
                  AND (?::int4 IS NULL OR p.project_type_id = ?)
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, itemCode, projectTypeId, projectTypeId);
    }

    public List<StockMovementResponse> getPlantProductionConsumptionHistory(String projectCode, String itemCode) {
        // The raw material's cost is whatever FIFO batches issueGoods actually drew from for
        // this production (see stock_action_allocation, keyed by
        // action_type='PLANT_PRODUCTION' + action_id=production_id + item_code) — summing
        // qty_taken*unit_cost gives the true cost of what was consumed, not just an assumed
        // catalog price.
        String sql = """
                SELECT
                    'PLANT_PRODUCTION_CONSUMPTION' AS type,
                    pp.production_code AS doc_code,
                    pp.production_date AS movement_date,
                    pp.project_code AS project_code,
                    p.projectname AS project_name,
                    NULL AS counterparty_project_code,
                    NULL AS counterparty_project_name,
                    'OUT' AS direction,
                    ppi.consumed_quantity AS quantity,
                    ppi.uom_id AS uom_id,
                    (pp.status = 'APPROVED') AS is_approved,
                    pp.remarks AS remarks,
                    NULL AS supplier_code,
                    NULL AS supplier_name,
                    CASE WHEN ppi.consumed_quantity > 0 THEN alloc_cost.total_cost / ppi.consumed_quantity END AS unit_price,
                    alloc_cost.total_cost AS amount
                FROM plant_production pp
                JOIN plant_production_input ppi ON ppi.production_id = pp.production_id
                LEFT JOIN project p ON p.project_code = pp.project_code
                LEFT JOIN LATERAL (
                    SELECT SUM(saa.qty_taken * saa.unit_cost) AS total_cost
                    FROM stock_action_allocation saa
                    WHERE saa.action_type = 'PLANT_PRODUCTION'
                      AND saa.action_id = pp.production_id
                      AND saa.item_code = ppi.item_code
                ) alloc_cost ON TRUE
                WHERE pp.project_code = ? AND ppi.item_code = ?
                  AND pp.status = 'APPROVED'
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, projectCode, itemCode);
    }

    private List<StockMovementResponse> getPlantProductionConsumptionHistoryAllProjects(String itemCode, Integer projectTypeId) {
        String sql = """
                SELECT
                    'PLANT_PRODUCTION_CONSUMPTION' AS type,
                    pp.production_code AS doc_code,
                    pp.production_date AS movement_date,
                    pp.project_code AS project_code,
                    p.projectname AS project_name,
                    NULL AS counterparty_project_code,
                    NULL AS counterparty_project_name,
                    'OUT' AS direction,
                    ppi.consumed_quantity AS quantity,
                    ppi.uom_id AS uom_id,
                    (pp.status = 'APPROVED') AS is_approved,
                    pp.remarks AS remarks,
                    NULL AS supplier_code,
                    NULL AS supplier_name,
                    CASE WHEN ppi.consumed_quantity > 0 THEN alloc_cost.total_cost / ppi.consumed_quantity END AS unit_price,
                    alloc_cost.total_cost AS amount
                FROM plant_production pp
                JOIN plant_production_input ppi ON ppi.production_id = pp.production_id
                JOIN project p ON p.project_code = pp.project_code
                LEFT JOIN LATERAL (
                    SELECT SUM(saa.qty_taken * saa.unit_cost) AS total_cost
                    FROM stock_action_allocation saa
                    WHERE saa.action_type = 'PLANT_PRODUCTION'
                      AND saa.action_id = pp.production_id
                      AND saa.item_code = ppi.item_code
                ) alloc_cost ON TRUE
                WHERE ppi.item_code = ?
                  AND pp.status = 'APPROVED'
                  AND (?::int4 IS NULL OR p.project_type_id = ?)
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, itemCode, projectTypeId, projectTypeId);
    }

    /*
     * =========================================================
     * JOB CARD ISSUE (OUT) and ITS RETURN (IN) — a job card issue
     * debits the issuing employee's active project's store the
     * moment it's created (see JobIssueItemService), with no
     * separate approval step; its return credits the same store
     * back the moment it's recorded (see JobIssueItemReturnService).
     * Neither ever goes through a GIN or intra-project issue.
     * =========================================================
     */
    public List<StockMovementResponse> getJobIssueHistory(String projectCode, String itemCode) {
        String sql = """
                SELECT
                    'JOB_ISSUE' AS type,
                    jc.job_card_code AS doc_code,
                    ji.issued_date::date AS movement_date,
                    ji.project_code AS project_code,
                    p.projectname AS project_name,
                    NULL AS counterparty_project_code,
                    NULL AS counterparty_project_name,
                    'OUT' AS direction,
                    ji.quantity AS quantity,
                    NULL AS uom_id,
                    TRUE AS is_approved,
                    ji.description AS remarks,
                    NULL AS supplier_code,
                    NULL AS supplier_name,
                    ji.unit_price AS unit_price,
                    ji.quantity * ji.unit_price AS amount
                FROM job_issue_item ji
                JOIN job_card jc ON jc.job_card_id = ji.job_card_id
                LEFT JOIN project p ON p.project_code = ji.project_code
                WHERE ji.project_code = ? AND ji.item_code = ?
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, projectCode, itemCode);
    }

    private List<StockMovementResponse> getJobIssueHistoryAllProjects(String itemCode, Integer projectTypeId) {
        String sql = """
                SELECT
                    'JOB_ISSUE' AS type,
                    jc.job_card_code AS doc_code,
                    ji.issued_date::date AS movement_date,
                    ji.project_code AS project_code,
                    p.projectname AS project_name,
                    NULL AS counterparty_project_code,
                    NULL AS counterparty_project_name,
                    'OUT' AS direction,
                    ji.quantity AS quantity,
                    NULL AS uom_id,
                    TRUE AS is_approved,
                    ji.description AS remarks,
                    NULL AS supplier_code,
                    NULL AS supplier_name,
                    ji.unit_price AS unit_price,
                    ji.quantity * ji.unit_price AS amount
                FROM job_issue_item ji
                JOIN job_card jc ON jc.job_card_id = ji.job_card_id
                JOIN project p ON p.project_code = ji.project_code
                WHERE ji.item_code = ?
                  AND (?::int4 IS NULL OR p.project_type_id = ?)
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, itemCode, projectTypeId, projectTypeId);
    }

    public List<StockMovementResponse> getJobIssueReturnHistory(String projectCode, String itemCode) {
        String sql = """
                SELECT
                    'JOB_ISSUE_RETURN' AS type,
                    jc.job_card_code AS doc_code,
                    jir.returned_date::date AS movement_date,
                    jir.project_code AS project_code,
                    p.projectname AS project_name,
                    NULL AS counterparty_project_code,
                    NULL AS counterparty_project_name,
                    'IN' AS direction,
                    jir.quantity AS quantity,
                    NULL AS uom_id,
                    TRUE AS is_approved,
                    jir.remarks AS remarks,
                    NULL AS supplier_code,
                    NULL AS supplier_name,
                    jir.unit_price AS unit_price,
                    jir.quantity * jir.unit_price AS amount
                FROM job_issue_item_return jir
                JOIN job_card jc ON jc.job_card_id = jir.job_card_id
                LEFT JOIN project p ON p.project_code = jir.project_code
                WHERE jir.project_code = ? AND jir.item_code = ?
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, projectCode, itemCode);
    }

    private List<StockMovementResponse> getJobIssueReturnHistoryAllProjects(String itemCode, Integer projectTypeId) {
        String sql = """
                SELECT
                    'JOB_ISSUE_RETURN' AS type,
                    jc.job_card_code AS doc_code,
                    jir.returned_date::date AS movement_date,
                    jir.project_code AS project_code,
                    p.projectname AS project_name,
                    NULL AS counterparty_project_code,
                    NULL AS counterparty_project_name,
                    'IN' AS direction,
                    jir.quantity AS quantity,
                    NULL AS uom_id,
                    TRUE AS is_approved,
                    jir.remarks AS remarks,
                    NULL AS supplier_code,
                    NULL AS supplier_name,
                    jir.unit_price AS unit_price,
                    jir.quantity * jir.unit_price AS amount
                FROM job_issue_item_return jir
                JOIN job_card jc ON jc.job_card_id = jir.job_card_id
                JOIN project p ON p.project_code = jir.project_code
                WHERE jir.item_code = ?
                  AND (?::int4 IS NULL OR p.project_type_id = ?)
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, itemCode, projectTypeId, projectTypeId);
    }

    /*
     * =========================================================
     * FUEL ISSUE (OUT) — fuel_issue has no item_code column; the
     * fuel_type column IS the item code (see FuelIssueService,
     * which passes fuelIssue.getFuelType() straight into
     * ProjectStoreService.issueGoods). Stock only moves once the
     * receiver confirms delivery (is_received = true), not at
     * creation — see FuelIssueService.receiveFuel.
     * =========================================================
     */
    public List<StockMovementResponse> getFuelIssueHistory(String projectCode, String itemCode) {
        String sql = """
                SELECT
                    'FUEL_ISSUE' AS type,
                    fi.fuel_issue_code AS doc_code,
                    COALESCE(fi.fuel_issue_date, fi.issued_date)::date AS movement_date,
                    fi.project_code AS project_code,
                    p.projectname AS project_name,
                    NULL AS counterparty_project_code,
                    NULL AS counterparty_project_name,
                    'OUT' AS direction,
                    fi.quantity AS quantity,
                    NULL AS uom_id,
                    fi.is_received AS is_approved,
                    fi.remarks AS remarks,
                    NULL AS supplier_code,
                    NULL AS supplier_name,
                    NULL AS unit_price,
                    NULL AS amount
                FROM fuel_issue fi
                LEFT JOIN project p ON p.project_code = fi.project_code
                WHERE fi.project_code = ? AND fi.fuel_type = ?
                  AND fi.is_received = TRUE
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, projectCode, itemCode);
    }

    private List<StockMovementResponse> getFuelIssueHistoryAllProjects(String itemCode, Integer projectTypeId) {
        String sql = """
                SELECT
                    'FUEL_ISSUE' AS type,
                    fi.fuel_issue_code AS doc_code,
                    COALESCE(fi.fuel_issue_date, fi.issued_date)::date AS movement_date,
                    fi.project_code AS project_code,
                    p.projectname AS project_name,
                    NULL AS counterparty_project_code,
                    NULL AS counterparty_project_name,
                    'OUT' AS direction,
                    fi.quantity AS quantity,
                    NULL AS uom_id,
                    fi.is_received AS is_approved,
                    fi.remarks AS remarks,
                    NULL AS supplier_code,
                    NULL AS supplier_name,
                    NULL AS unit_price,
                    NULL AS amount
                FROM fuel_issue fi
                JOIN project p ON p.project_code = fi.project_code
                WHERE fi.fuel_type = ?
                  AND fi.is_received = TRUE
                  AND (?::int4 IS NULL OR p.project_type_id = ?)
                """;
        return jdbcTemplate.query(sql, MOVEMENT_MAPPER, itemCode, projectTypeId, projectTypeId);
    }

    /*
     * =========================================================
     * LIFETIME RECEIVED / ISSUED TOTALS FOR ONE PROJECT + ITEM
     * =========================================================
     */
    public BigDecimal[] getTotals(String projectCode, String itemCode) {
        String sql = """
                SELECT
                    COALESCE((
                        SELECT SUM(gi.quantity) FROM grn g
                        JOIN grn_item gi ON gi.grn_id = g.grn_id
                        WHERE (CASE WHEN g.is_supplier_grn THEN g.from_project_code ELSE g.to_project_code END) = ?
                          AND gi.item_code = ? AND g.is_approved = TRUE
                    ), 0) AS total_received,
                    COALESCE((
                        SELECT SUM(item.quantity) FROM intra_project_issue issue
                        JOIN intra_project_issue_item item ON item.intra_project_issue_id = issue.intra_project_issue_id
                        WHERE issue.issued_project_code = ? AND item.item_code = ? AND issue.is_authorized = TRUE
                    ), 0) AS total_issued_intra,
                    COALESCE((
                        SELECT SUM(gi2.quantity) FROM gin g2
                        JOIN gin_item gi2 ON gi2.gin_id = g2.gin_id
                        WHERE g2.issued_project_code = ? AND gi2.item_code = ? AND g2.is_authorized = TRUE
                    ), 0) AS total_issued_inter_project,
                    COALESCE((
                        SELECT SUM(gi3.quantity) FROM gin g3
                        JOIN gin_item gi3 ON gi3.gin_id = g3.gin_id
                        WHERE g3.received_project_code = ? AND gi3.item_code = ? AND g3.is_authorized = TRUE
                    ), 0) AS total_received_inter_project,
                    COALESCE((
                        SELECT SUM(ppo.produced_quantity) FROM plant_production pp
                        JOIN plant_production_output ppo ON ppo.production_id = pp.production_id
                        WHERE pp.project_code = ? AND ppo.item_code = ? AND pp.status = 'APPROVED'
                    ), 0) AS total_produced,
                    COALESCE((
                        SELECT SUM(ppi.consumed_quantity) FROM plant_production pp2
                        JOIN plant_production_input ppi ON ppi.production_id = pp2.production_id
                        WHERE pp2.project_code = ? AND ppi.item_code = ? AND pp2.status = 'APPROVED'
                    ), 0) AS total_consumed,
                    COALESCE((
                        SELECT SUM(ji.quantity) FROM job_issue_item ji
                        WHERE ji.project_code = ? AND ji.item_code = ?
                    ), 0) AS total_job_issued,
                    COALESCE((
                        SELECT SUM(jir.quantity) FROM job_issue_item_return jir
                        WHERE jir.project_code = ? AND jir.item_code = ?
                    ), 0) AS total_job_returned,
                    COALESCE((
                        SELECT SUM(fi.quantity) FROM fuel_issue fi
                        WHERE fi.project_code = ? AND fi.fuel_type = ? AND fi.is_received = TRUE
                    ), 0) AS total_fuel_issued
                """;
        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> new BigDecimal[]{
                        rs.getBigDecimal("total_received"),
                        rs.getBigDecimal("total_issued_intra"),
                        rs.getBigDecimal("total_issued_inter_project"),
                        rs.getBigDecimal("total_received_inter_project"),
                        rs.getBigDecimal("total_produced"),
                        rs.getBigDecimal("total_consumed"),
                        rs.getBigDecimal("total_job_issued"),
                        rs.getBigDecimal("total_job_returned"),
                        rs.getBigDecimal("total_fuel_issued"),
                },
                projectCode, itemCode, projectCode, itemCode, projectCode, itemCode, projectCode, itemCode,
                projectCode, itemCode, projectCode, itemCode,
                projectCode, itemCode, projectCode, itemCode, projectCode, itemCode
        );
    }

    /**
     * Same totals as {@link #getTotals}, but summed across every project (or, with
     * {@code projectTypeId} set, every project of that type) instead of one — used for the
     * HQ "all projects" item summary. Note that {@code total_received_inter_project} and
     * {@code total_issued_inter_project} summed here are two sides of the same internal
     * transfers, so with no project type filter they are equal (a transfer counted as an
     * "issue" for its source project is a "receipt" for its destination project) — they only
     * diverge once a project type filter makes the transfer cross the filter boundary.
     */
    public BigDecimal[] getTotalsAllProjects(String itemCode, Integer projectTypeId) {
        String sql = """
                SELECT
                    COALESCE((
                        SELECT SUM(gi.quantity) FROM grn g
                        JOIN grn_item gi ON gi.grn_id = g.grn_id
                        JOIN project p ON p.project_code =
                            CASE WHEN g.is_supplier_grn THEN g.from_project_code ELSE g.to_project_code END
                        WHERE gi.item_code = ? AND g.is_approved = TRUE AND (?::int4 IS NULL OR p.project_type_id = ?)
                    ), 0) AS total_received,
                    COALESCE((
                        SELECT SUM(item.quantity) FROM intra_project_issue issue
                        JOIN intra_project_issue_item item ON item.intra_project_issue_id = issue.intra_project_issue_id
                        JOIN project p ON p.project_code = issue.issued_project_code
                        WHERE item.item_code = ? AND issue.is_authorized = TRUE AND (?::int4 IS NULL OR p.project_type_id = ?)
                    ), 0) AS total_issued_intra,
                    COALESCE((
                        SELECT SUM(gi2.quantity) FROM gin g2
                        JOIN gin_item gi2 ON gi2.gin_id = g2.gin_id
                        JOIN project p ON p.project_code = g2.issued_project_code
                        WHERE gi2.item_code = ? AND g2.is_authorized = TRUE AND (?::int4 IS NULL OR p.project_type_id = ?)
                    ), 0) AS total_issued_inter_project,
                    COALESCE((
                        SELECT SUM(gi3.quantity) FROM gin g3
                        JOIN gin_item gi3 ON gi3.gin_id = g3.gin_id
                        JOIN project p ON p.project_code = g3.received_project_code
                        WHERE gi3.item_code = ? AND g3.is_authorized = TRUE AND (?::int4 IS NULL OR p.project_type_id = ?)
                    ), 0) AS total_received_inter_project,
                    COALESCE((
                        SELECT SUM(ppo.produced_quantity) FROM plant_production pp
                        JOIN plant_production_output ppo ON ppo.production_id = pp.production_id
                        JOIN project p ON p.project_code = pp.project_code
                        WHERE ppo.item_code = ? AND pp.status = 'APPROVED' AND (?::int4 IS NULL OR p.project_type_id = ?)
                    ), 0) AS total_produced,
                    COALESCE((
                        SELECT SUM(ppi.consumed_quantity) FROM plant_production pp2
                        JOIN plant_production_input ppi ON ppi.production_id = pp2.production_id
                        JOIN project p ON p.project_code = pp2.project_code
                        WHERE ppi.item_code = ? AND pp2.status = 'APPROVED' AND (?::int4 IS NULL OR p.project_type_id = ?)
                    ), 0) AS total_consumed,
                    COALESCE((
                        SELECT SUM(ji.quantity) FROM job_issue_item ji
                        JOIN project p ON p.project_code = ji.project_code
                        WHERE ji.item_code = ? AND (?::int4 IS NULL OR p.project_type_id = ?)
                    ), 0) AS total_job_issued,
                    COALESCE((
                        SELECT SUM(jir.quantity) FROM job_issue_item_return jir
                        JOIN project p ON p.project_code = jir.project_code
                        WHERE jir.item_code = ? AND (?::int4 IS NULL OR p.project_type_id = ?)
                    ), 0) AS total_job_returned,
                    COALESCE((
                        SELECT SUM(fi.quantity) FROM fuel_issue fi
                        JOIN project p ON p.project_code = fi.project_code
                        WHERE fi.fuel_type = ? AND fi.is_received = TRUE AND (?::int4 IS NULL OR p.project_type_id = ?)
                    ), 0) AS total_fuel_issued
                """;
        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> new BigDecimal[]{
                        rs.getBigDecimal("total_received"),
                        rs.getBigDecimal("total_issued_intra"),
                        rs.getBigDecimal("total_issued_inter_project"),
                        rs.getBigDecimal("total_received_inter_project"),
                        rs.getBigDecimal("total_produced"),
                        rs.getBigDecimal("total_consumed"),
                        rs.getBigDecimal("total_job_issued"),
                        rs.getBigDecimal("total_job_returned"),
                        rs.getBigDecimal("total_fuel_issued"),
                },
                itemCode, projectTypeId, projectTypeId,
                itemCode, projectTypeId, projectTypeId,
                itemCode, projectTypeId, projectTypeId,
                itemCode, projectTypeId, projectTypeId,
                itemCode, projectTypeId, projectTypeId,
                itemCode, projectTypeId, projectTypeId,
                itemCode, projectTypeId, projectTypeId,
                itemCode, projectTypeId, projectTypeId,
                itemCode, projectTypeId, projectTypeId
        );
    }
}
