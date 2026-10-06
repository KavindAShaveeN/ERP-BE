package com.rr.erp.repository;

import com.rr.erp.dto.DispatchLineResponse;
import com.rr.erp.dto.PlannedAllocationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Data access for main-store issue planning: pending MR lines, the stock position per item,
 * reservations (mr_item_allocation), forward plans (mr_forward_plan) and MR line status.
 * See create_issue_planning_dev.sql for the tables and views this reads.
 */
@Repository
@RequiredArgsConstructor
public class IssuePlanningRepository {

    private final JdbcTemplate jdbcTemplate;

    /** One approved MR line addressed to the store, with everything the grid needs. */
    public record PendingLine(
            UUID mrItemId,
            UUID mrId,
            String mrCode,
            String requestingProjectCode,
            String requestingProjectName,
            LocalDateTime requestedDate,
            String itemCode,
            String description,
            String size,
            Integer uomId,
            String uomName,
            BigDecimal requestedQty,
            BigDecimal issuedQty,
            BigDecimal forwardedQty,
            BigDecimal allocatedQty,
            BigDecimal siteBalance,
            BigDecimal siteInTransit,
            String priority,
            Integer priorityRank,
            LocalDate requiredDate,
            String lineStatus
    ) {
    }

    public record StockPosition(BigDecimal onHand, BigDecimal reorderLevel) {
    }

    private static final String LINE_SELECT = """
            SELECT
                mi.mr_item_id,
                mi.mr_id,
                m.mr_code,
                m.requesting_project_code,
                p.projectname,
                m.requested_date,
                mi.item_code_code,
                COALESCE(NULLIF(mi.description, ''), ic.item_code_name) AS description,
                mi.size,
                mi.uom_id,
                u.uom_name,
                mi.quantity,
                COALESCE(v.issued_qty, 0) AS issued_qty,
                COALESCE((SELECT SUM(f.shortfall_qty) FROM mr_forward_plan f
                           WHERE f.mr_item_id = mi.mr_item_id
                             AND f.action = 'FORWARD'
                             AND f.status IN ('AGREED', 'FORWARDED')), 0) AS forwarded_qty,
                COALESCE((SELECT SUM(a.allocated_qty) FROM mr_item_allocation a
                           WHERE a.mr_item_id = mi.mr_item_id
                             AND a.status = 'ACTIVE'), 0) AS allocated_qty,
                COALESCE(site.quantity_on_hand, 0) AS site_balance,
                COALESCE(it.in_transit_qty, 0) AS site_in_transit,
                mi.priority,
                mi.priority_rank,
                mi.required_date,
                mi.line_status
            FROM mr_item mi
            JOIN mr m ON m.mr_id = mi.mr_id
            LEFT JOIN v_mr_item_issued v ON v.mr_item_id = mi.mr_item_id
            LEFT JOIN uom u ON u.uom_id = mi.uom_id
            LEFT JOIN item_code ic ON ic.item_code_code = mi.item_code_code
            LEFT JOIN project p ON p.project_code = m.requesting_project_code
            LEFT JOIN project_store site
                   ON site.project_code = m.requesting_project_code
                  AND site.item_code = mi.item_code_code
            LEFT JOIN v_in_transit it
                   ON it.issued_project_code = m.destination_project_code
                  AND it.received_project_code = m.requesting_project_code
                  AND it.item_code = mi.item_code_code
            """;

    private PendingLine mapLine(java.sql.ResultSet rs) throws java.sql.SQLException {
        Timestamp requested = rs.getTimestamp("requested_date");
        Date required = rs.getDate("required_date");
        return new PendingLine(
                rs.getObject("mr_item_id", UUID.class),
                rs.getObject("mr_id", UUID.class),
                rs.getString("mr_code"),
                rs.getString("requesting_project_code"),
                rs.getString("projectname"),
                requested == null ? null : requested.toLocalDateTime(),
                rs.getString("item_code_code"),
                rs.getString("description"),
                rs.getString("size"),
                rs.getObject("uom_id", Integer.class),
                rs.getString("uom_name"),
                rs.getBigDecimal("quantity"),
                rs.getBigDecimal("issued_qty"),
                rs.getBigDecimal("forwarded_qty"),
                rs.getBigDecimal("allocated_qty"),
                rs.getBigDecimal("site_balance"),
                rs.getBigDecimal("site_in_transit"),
                rs.getString("priority"),
                rs.getObject("priority_rank", Integer.class),
                required == null ? null : required.toLocalDate(),
                rs.getString("line_status")
        );
    }

    /** Every approved, not-yet-closed MR line addressed to the store. Callers filter further. */
    public List<PendingLine> findOpenLines(String storeCode) {
        String sql = LINE_SELECT + """
                WHERE m.is_approved = TRUE
                  AND m.destination_project_code = ?
                  AND mi.line_status <> 'CLOSED'
                ORDER BY m.requested_date, m.mr_code, mi.required_date
                """;
        return jdbcTemplate.query(sql, (rs, n) -> mapLine(rs), storeCode);
    }

    /** The requested lines (any status), restricted to approved MRs addressed to the store. */
    public List<PendingLine> findLines(String storeCode, List<UUID> mrItemIds) {
        if (mrItemIds.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", Collections.nCopies(mrItemIds.size(), "?"));
        String sql = LINE_SELECT + """
                WHERE m.is_approved = TRUE
                  AND m.destination_project_code = ?
                  AND mi.mr_item_id IN (""" + placeholders + ")";
        Object[] args = new Object[mrItemIds.size() + 1];
        args[0] = storeCode;
        for (int i = 0; i < mrItemIds.size(); i++) {
            args[i + 1] = mrItemIds.get(i);
        }
        return jdbcTemplate.query(sql, (rs, n) -> mapLine(rs), args);
    }

    public Map<String, StockPosition> findStockPositions(String storeCode) {
        Map<String, StockPosition> result = new HashMap<>();
        jdbcTemplate.query(
                "SELECT item_code, quantity_on_hand, reorder_level FROM project_store WHERE project_code = ?",
                rs -> {
                    result.put(
                            rs.getString("item_code"),
                            new StockPosition(rs.getBigDecimal("quantity_on_hand"), rs.getBigDecimal("reorder_level"))
                    );
                },
                storeCode
        );
        return result;
    }

    public Map<String, BigDecimal> findReservedByItem(String storeCode) {
        Map<String, BigDecimal> result = new HashMap<>();
        jdbcTemplate.query(
                "SELECT item_code, reserved_qty FROM v_store_reserved WHERE project_code = ?",
                rs -> {
                    result.put(rs.getString("item_code"), rs.getBigDecimal("reserved_qty"));
                },
                storeCode
        );
        return result;
    }

    public Map<String, BigDecimal> findInTransitByItem(String storeCode) {
        Map<String, BigDecimal> result = new HashMap<>();
        jdbcTemplate.query(
                """
                SELECT item_code, SUM(in_transit_qty) AS qty
                FROM v_in_transit
                WHERE issued_project_code = ?
                GROUP BY item_code
                """,
                rs -> {
                    result.put(rs.getString("item_code"), rs.getBigDecimal("qty"));
                },
                storeCode
        );
        return result;
    }

    /** Open PO quantity per item for POs delivering to the store: ordered minus approved-GRN receipts. */
    public Map<String, BigDecimal> findIncomingPoByItem(String storeCode) {
        Map<String, BigDecimal> result = new HashMap<>();
        jdbcTemplate.query(
                """
                SELECT pi.item_code,
                       SUM(GREATEST(pi.quantity - COALESCE(r.received_qty, 0), 0)) AS qty
                FROM po p
                JOIN po_item pi ON pi.po_id = p.po_id
                LEFT JOIN (
                    SELECT g.po_code, gi.item_code,
                           SUM(COALESCE(gi.po_equivalent_qty, gi.quantity)) AS received_qty
                    FROM grn g
                    JOIN grn_item gi ON gi.grn_id = g.grn_id
                    WHERE g.po_code IS NOT NULL AND g.is_approved = TRUE
                    GROUP BY g.po_code, gi.item_code
                ) r ON r.po_code = p.po_code AND r.item_code = pi.item_code
                WHERE p.project_code = ?
                  AND p.status = 'ACTIVE'
                  AND p.approval_status = 'APPROVED'
                GROUP BY pi.item_code
                """,
                rs -> {
                    result.put(rs.getString("item_code"), rs.getBigDecimal("qty"));
                },
                storeCode
        );
        return result;
    }

    /** Locks the store's stock rows for these items so two planners can't over-reserve at once. */
    public Map<String, BigDecimal> lockOnHand(String storeCode, List<String> itemCodes) {
        Map<String, BigDecimal> result = new HashMap<>();
        for (String itemCode : itemCodes) {
            List<BigDecimal> rows = jdbcTemplate.query(
                    "SELECT quantity_on_hand FROM project_store WHERE project_code = ? AND item_code = ? FOR UPDATE",
                    (rs, n) -> rs.getBigDecimal("quantity_on_hand"),
                    storeCode,
                    itemCode
            );
            result.put(itemCode, rows.isEmpty() ? BigDecimal.ZERO : rows.get(0));
        }
        return result;
    }

    /** Active reservations for the item in the store that do NOT belong to the given MR lines. */
    public BigDecimal sumActiveExcluding(String storeCode, String itemCode, List<UUID> excludedMrItemIds) {
        StringBuilder sql = new StringBuilder("""
                SELECT COALESCE(SUM(allocated_qty), 0)
                FROM mr_item_allocation
                WHERE status = 'ACTIVE' AND source_project_code = ? AND item_code = ?
                """);
        List<Object> args = new ArrayList<>();
        args.add(storeCode);
        args.add(itemCode);
        if (!excludedMrItemIds.isEmpty()) {
            sql.append(" AND mr_item_id NOT IN (")
                    .append(String.join(",", Collections.nCopies(excludedMrItemIds.size(), "?")))
                    .append(")");
            args.addAll(excludedMrItemIds);
        }
        BigDecimal sum = jdbcTemplate.queryForObject(sql.toString(), BigDecimal.class, args.toArray());
        return sum == null ? BigDecimal.ZERO : sum;
    }

    public void releaseActiveForLine(UUID mrItemId, String releasedBy, String reason) {
        jdbcTemplate.update(
                """
                UPDATE mr_item_allocation
                SET status = 'RELEASED', released_by = ?, released_at = now(), release_reason = ?
                WHERE mr_item_id = ? AND status = 'ACTIVE'
                """,
                releasedBy, reason, mrItemId
        );
    }

    public void releaseActiveForMr(UUID mrId, String releasedBy, String reason) {
        jdbcTemplate.update(
                """
                UPDATE mr_item_allocation
                SET status = 'RELEASED', released_by = ?, released_at = now(), release_reason = ?
                WHERE mr_id = ? AND status = 'ACTIVE'
                """,
                releasedBy, reason, mrId
        );
    }

    public void insertAllocation(
            UUID planBatchId, UUID mrId, UUID mrItemId, String itemCode,
            String storeCode, BigDecimal qty, String createdBy
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO mr_item_allocation
                    (plan_batch_id, mr_id, mr_item_id, item_code, source_project_code, allocated_qty, created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                planBatchId, mrId, mrItemId, itemCode, storeCode, qty, createdBy
        );
    }

    /**
     * An authorized GIN issued qty of an item against an MR: converts that much of the MR's
     * active reservation (oldest first) from ACTIVE to ISSUED. A reservation larger than what
     * was issued is split, so the remainder stays reserved.
     */
    public void consumeForGin(UUID mrId, String storeCode, String itemCode, BigDecimal qty, UUID ginId) {
        BigDecimal remaining = qty;
        List<Map<String, Object>> active = jdbcTemplate.queryForList(
                """
                SELECT allocation_id, allocated_qty
                FROM mr_item_allocation
                WHERE mr_id = ? AND source_project_code = ? AND item_code = ? AND status = 'ACTIVE'
                ORDER BY created_at
                FOR UPDATE
                """,
                mrId, storeCode, itemCode
        );
        for (Map<String, Object> row : active) {
            if (remaining.signum() <= 0) {
                break;
            }
            UUID allocationId = (UUID) row.get("allocation_id");
            BigDecimal allocated = (BigDecimal) row.get("allocated_qty");
            if (allocated.compareTo(remaining) <= 0) {
                jdbcTemplate.update(
                        "UPDATE mr_item_allocation SET status = 'ISSUED', gin_id = ? WHERE allocation_id = ?",
                        ginId, allocationId
                );
                remaining = remaining.subtract(allocated);
            } else {
                jdbcTemplate.update(
                        "UPDATE mr_item_allocation SET allocated_qty = allocated_qty - ? WHERE allocation_id = ?",
                        remaining, allocationId
                );
                jdbcTemplate.update(
                        """
                        INSERT INTO mr_item_allocation
                            (plan_batch_id, mr_id, mr_item_id, item_code, source_project_code,
                             allocated_qty, status, gin_id, created_by, created_at)
                        SELECT plan_batch_id, mr_id, mr_item_id, item_code, source_project_code,
                               ?, 'ISSUED', ?, created_by, created_at
                        FROM mr_item_allocation WHERE allocation_id = ?
                        """,
                        remaining, ginId, allocationId
                );
                remaining = BigDecimal.ZERO;
            }
        }
    }

    /** Same as {@link #consumeForGin} but for a GIN line that names its MR line directly. */
    public void consumeForMrItem(UUID mrItemId, String storeCode, BigDecimal qty, UUID ginId) {
        BigDecimal remaining = qty;
        List<Map<String, Object>> active = jdbcTemplate.queryForList(
                """
                SELECT allocation_id, allocated_qty
                FROM mr_item_allocation
                WHERE mr_item_id = ? AND source_project_code = ? AND status = 'ACTIVE'
                ORDER BY created_at
                FOR UPDATE
                """,
                mrItemId, storeCode
        );
        for (Map<String, Object> row : active) {
            if (remaining.signum() <= 0) {
                break;
            }
            UUID allocationId = (UUID) row.get("allocation_id");
            BigDecimal allocated = (BigDecimal) row.get("allocated_qty");
            if (allocated.compareTo(remaining) <= 0) {
                jdbcTemplate.update(
                        "UPDATE mr_item_allocation SET status = 'ISSUED', gin_id = ? WHERE allocation_id = ?",
                        ginId, allocationId
                );
                remaining = remaining.subtract(allocated);
            } else {
                jdbcTemplate.update(
                        "UPDATE mr_item_allocation SET allocated_qty = allocated_qty - ? WHERE allocation_id = ?",
                        remaining, allocationId
                );
                jdbcTemplate.update(
                        """
                        INSERT INTO mr_item_allocation
                            (plan_batch_id, mr_id, mr_item_id, item_code, source_project_code,
                             allocated_qty, status, gin_id, created_by, created_at)
                        SELECT plan_batch_id, mr_id, mr_item_id, item_code, source_project_code,
                               ?, 'ISSUED', ?, created_by, created_at
                        FROM mr_item_allocation WHERE allocation_id = ?
                        """,
                        remaining, ginId, allocationId
                );
                remaining = BigDecimal.ZERO;
            }
        }
    }

    public UUID findMrIdByMrItemId(UUID mrItemId) {
        List<UUID> ids = jdbcTemplate.query(
                "SELECT mr_id FROM mr_item WHERE mr_item_id = ?",
                (rs, n) -> rs.getObject("mr_id", UUID.class),
                mrItemId
        );
        return ids.isEmpty() ? null : ids.get(0);
    }

    /** Every reserved MR line at the store across all sites, for building per-site dispatch GINs. */
    public List<DispatchLineResponse> findDispatchLines(String storeCode) {
        return jdbcTemplate.query(
                """
                SELECT a.mr_item_id, a.mr_id, m.mr_code, m.requesting_project_code, p.projectname,
                       a.item_code,
                       COALESCE(NULLIF(mi.description, ''), ic.item_code_name) AS description,
                       mi.size, mi.uom_id, u.uom_name, mi.priority, mi.required_date,
                       SUM(a.allocated_qty) AS qty
                FROM mr_item_allocation a
                JOIN mr_item mi ON mi.mr_item_id = a.mr_item_id
                JOIN mr m ON m.mr_id = a.mr_id
                LEFT JOIN project p ON p.project_code = m.requesting_project_code
                LEFT JOIN uom u ON u.uom_id = mi.uom_id
                LEFT JOIN item_code ic ON ic.item_code_code = a.item_code
                WHERE a.status = 'ACTIVE' AND a.source_project_code = ? AND m.is_approved = TRUE
                GROUP BY a.mr_item_id, a.mr_id, m.mr_code, m.requesting_project_code, p.projectname,
                         a.item_code, mi.description, ic.item_code_name, mi.size, mi.uom_id, u.uom_name,
                         mi.priority, mi.required_date, m.requested_date
                ORDER BY m.requesting_project_code, m.requested_date, m.mr_code, a.item_code
                """,
                (rs, n) -> {
                    DispatchLineResponse r = new DispatchLineResponse();
                    r.setMrItemId(rs.getObject("mr_item_id", UUID.class));
                    r.setMrId(rs.getObject("mr_id", UUID.class));
                    r.setMrCode(rs.getString("mr_code"));
                    r.setRequestingProjectCode(rs.getString("requesting_project_code"));
                    r.setRequestingProjectName(rs.getString("projectname"));
                    r.setItemCode(rs.getString("item_code"));
                    r.setDescription(rs.getString("description"));
                    r.setSize(rs.getString("size"));
                    r.setUomId(rs.getObject("uom_id", Integer.class));
                    r.setUomName(rs.getString("uom_name"));
                    r.setPriority(rs.getString("priority"));
                    Date required = rs.getDate("required_date");
                    r.setRequiredDate(required == null ? null : required.toLocalDate());
                    r.setAllocatedQty(rs.getBigDecimal("qty"));
                    return r;
                },
                storeCode
        );
    }

    /**
     * Recomputes line_status for an MR's lines from what was issued and forwarded:
     * CLOSED (fully issued), FORWARDED (everything still owed was forwarded to Purchasing),
     * PARTLY_ISSUED, else OPEN. BACK_ORDERED is set explicitly when a forward plan is agreed
     * and is cleared by the next refresh (i.e. the next allocation or issue).
     */
    public void refreshLineStatus(UUID mrId) {
        jdbcTemplate.update(
                """
                UPDATE mr_item mi
                SET line_status = CASE
                        WHEN s.issued >= mi.quantity THEN 'CLOSED'
                        WHEN mi.quantity - s.issued - s.forwarded <= 0 THEN 'FORWARDED'
                        WHEN s.issued > 0 THEN 'PARTLY_ISSUED'
                        ELSE 'OPEN'
                    END
                FROM (
                    SELECT mi2.mr_item_id,
                           COALESCE(v.issued_qty, 0) AS issued,
                           COALESCE((SELECT SUM(f.shortfall_qty) FROM mr_forward_plan f
                                      WHERE f.mr_item_id = mi2.mr_item_id
                                        AND f.action = 'FORWARD'
                                        AND f.status IN ('AGREED', 'FORWARDED')), 0) AS forwarded
                    FROM mr_item mi2
                    LEFT JOIN v_mr_item_issued v ON v.mr_item_id = mi2.mr_item_id
                    WHERE mi2.mr_id = ?
                ) s
                WHERE s.mr_item_id = mi.mr_item_id
                """,
                mrId
        );
    }

    public void markBackOrdered(UUID mrItemId) {
        jdbcTemplate.update(
                "UPDATE mr_item SET line_status = 'BACK_ORDERED' WHERE mr_item_id = ? AND line_status IN ('OPEN', 'PARTLY_ISSUED')",
                mrItemId
        );
    }

    public void insertForwardPlanRow(
            UUID planBatchId, UUID mrId, UUID mrItemId, String itemCode, BigDecimal qty,
            String action, String status, UUID forwardedMrId, String user
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO mr_forward_plan
                    (plan_batch_id, mr_id, mr_item_id, item_code, shortfall_qty, action, status,
                     forwarded_mr_id, created_by, agreed_by, agreed_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())
                """,
                planBatchId, mrId, mrItemId, itemCode, qty, action, status, forwardedMrId, user, user
        );
    }

    /** Next FWD-#### code (same format the issue form's forward button used). */
    public String nextForwardMrCode() {
        Integer max = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(SUBSTRING(mr_code FROM 5)::int), 0) FROM mr WHERE mr_code ~ '^FWD-[0-9]+$'",
                Integer.class
        );
        return String.format("FWD-%04d", (max == null ? 0 : max) + 1);
    }

    /** Active reservations of one MR for the store, one row per MR line — used to pre-fill a GIN. */
    public List<PlannedAllocationResponse> findActiveByMr(UUID mrId, String storeCode) {
        return jdbcTemplate.query(
                """
                SELECT a.mr_item_id, a.item_code, mi.description, mi.size, mi.uom_id, u.uom_name,
                       SUM(a.allocated_qty) AS qty
                FROM mr_item_allocation a
                JOIN mr_item mi ON mi.mr_item_id = a.mr_item_id
                LEFT JOIN uom u ON u.uom_id = mi.uom_id
                WHERE a.mr_id = ? AND a.source_project_code = ? AND a.status = 'ACTIVE'
                GROUP BY a.mr_item_id, a.item_code, mi.description, mi.size, mi.uom_id, u.uom_name
                ORDER BY a.item_code
                """,
                (rs, n) -> {
                    PlannedAllocationResponse r = new PlannedAllocationResponse();
                    r.setMrItemId(rs.getObject("mr_item_id", UUID.class));
                    r.setItemCode(rs.getString("item_code"));
                    r.setDescription(rs.getString("description"));
                    r.setSize(rs.getString("size"));
                    r.setUomId(rs.getObject("uom_id", Integer.class));
                    r.setUomName(rs.getString("uom_name"));
                    r.setAllocatedQty(rs.getBigDecimal("qty"));
                    return r;
                },
                mrId, storeCode
        );
    }
}
