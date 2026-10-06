package com.rr.erp.repository;

import com.rr.erp.entity.StockReturn;
import com.rr.erp.entity.StockReturnItem;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StockReturnRepository {

    private final JdbcTemplate jdbcTemplate;


    public Optional<StockReturn> findById(UUID stockReturnId) {

        String sql = """
                SELECT *
                FROM stock_return
                WHERE stock_return_id = ?
                """;

        try {
            StockReturn stockReturn = jdbcTemplate.queryForObject(
                    sql,
                    (rs, rowNum) -> mapStockReturn(rs),
                    stockReturnId
            );
            return Optional.ofNullable(stockReturn);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    public void createStockReturn(StockReturn stockReturn) {

        String sql = """
                INSERT INTO stock_return (
                    stock_return_id,
                    stock_return_code,
                    from_project_code,
                    to_project_code,
                    gin_id,
                    return_type,
                    po_code,
                    supplier_code,
                    reason,
                    remark,
                    return_date,
                    return_by,
                    approved_date,
                    approved_by,
                    is_approved,
                    gate_verified_by,
                    gate_verified_date,
                    is_gate_verified,
                    arrival_gate_verified_by,
                    arrival_gate_verified_date,
                    is_arrival_gate_verified,
                    received_by,
                    vehicle_asset_code,
                    vehicle_no,
                    for_asset_code,
                    invoice_number,
                    delivery_note_number
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                stockReturn.getStockReturnId(),
                stockReturn.getStockReturnCode(),
                stockReturn.getFromProjectCode(),
                stockReturn.getToProjectCode(),
                stockReturn.getGinId(),
                stockReturn.getReturnType(),
                stockReturn.getPoCode(),
                stockReturn.getSupplierCode(),
                stockReturn.getReason(),
                stockReturn.getRemark(),
                stockReturn.getReturnDate(),
                stockReturn.getReturnBy(),
                stockReturn.getApprovedDate(),
                stockReturn.getApprovedBy(),
                Boolean.TRUE.equals(stockReturn.getIsApproved()),
                stockReturn.getGateVerifiedBy(),
                stockReturn.getGateVerifiedDate(),
                Boolean.TRUE.equals(stockReturn.getIsGateVerified()),
                stockReturn.getArrivalGateVerifiedBy(),
                stockReturn.getArrivalGateVerifiedDate(),
                Boolean.TRUE.equals(stockReturn.getIsArrivalGateVerified()),
                stockReturn.getReceivedBy(),
                stockReturn.getVehicleAssetCode(),
                stockReturn.getVehicleNo(),
                stockReturn.getForAssetCode(),
                stockReturn.getInvoiceNumber(),
                stockReturn.getDeliveryNoteNumber()
        );
    }

    public void createStockReturnItem(
            UUID stockReturnId,
            StockReturnItem item
    ) {

        String sql = """
                INSERT INTO stock_return_item (
                    stock_return_item_id,
                    stock_return_id,
                    item_code,
                    description,
                    size,
                    uom_id,
                    quantity,
                    remarks,
                    unit_price,
                    amount,
                    asset_code
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                item.getStockReturnItemId(),
                stockReturnId,
                item.getItemCode(),
                item.getDescription(),
                item.getSize(),
                item.getUomId(),
                item.getQuantity(),
                item.getRemarks(),
                item.getUnitPrice(),
                item.getAmount(),
                item.getAssetCode()
        );
    }


    public List<StockReturn> getCreatedReturns(
            String fromProjectCode,
            int page,
            int size
    ) {

        String sql = """
                SELECT *
                FROM stock_return
                WHERE from_project_code = ?
                ORDER BY return_date DESC
                LIMIT ? OFFSET ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapStockReturn(rs),
                fromProjectCode,
                size,
                page * size
        );
    }

    public long countCreatedReturns(String fromProjectCode) {

        String sql = """
                SELECT COUNT(*)
                FROM stock_return
                WHERE from_project_code = ?
                """;

        Long count = jdbcTemplate.queryForObject(sql, Long.class, fromProjectCode);

        return count == null ? 0 : count;
    }


    public List<StockReturn> getIncomingReturns(
            String toProjectCode,
            int page,
            int size
    ) {

        String sql = """
                SELECT *
                FROM stock_return
                WHERE to_project_code = ?
                ORDER BY return_date DESC
                LIMIT ? OFFSET ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapStockReturn(rs),
                toProjectCode,
                size,
                page * size
        );
    }

    public long countIncomingReturns(String toProjectCode) {

        String sql = """
                SELECT COUNT(*)
                FROM stock_return
                WHERE to_project_code = ?
                """;

        Long count = jdbcTemplate.queryForObject(sql, Long.class, toProjectCode);

        return count == null ? 0 : count;
    }

    // Every stock return across every project — used by HQ-wide report views.
    public List<StockReturn> getAllReturns(int page, int size) {

        String sql = """
                SELECT *
                FROM stock_return
                ORDER BY return_date DESC
                LIMIT ? OFFSET ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapStockReturn(rs),
                size,
                page * size
        );
    }

    public long countAllReturns() {

        String sql = """
                SELECT COUNT(*)
                FROM stock_return
                """;

        Long count = jdbcTemplate.queryForObject(sql, Long.class);

        return count == null ? 0 : count;
    }

    public List<StockReturnItem> getItems(
            UUID stockReturnId
    ) {

        String sql = """
                SELECT *
                FROM stock_return_item
                WHERE stock_return_id = ?
                """;

        List<StockReturnItem> items = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    StockReturnItem item =
                            new StockReturnItem();

                    item.setStockReturnItemId(
                            rs.getObject(
                                    "stock_return_item_id",
                                    UUID.class
                            )
                    );

                    item.setItemCode(
                            rs.getString("item_code")
                    );

                    item.setDescription(
                            rs.getString("description")
                    );

                    item.setSize(
                            rs.getString("size")
                    );

                    item.setUomId(
                            rs.getObject(
                                    "uom_id",
                                    Integer.class
                            )
                    );

                    item.setQuantity(
                            rs.getBigDecimal("quantity")
                    );

                    item.setRemarks(
                            rs.getString("remarks")
                    );

                    item.setUnitPrice(
                            rs.getBigDecimal("unit_price")
                    );

                    item.setAmount(
                            rs.getBigDecimal("amount")
                    );

                    item.setAssetCode(
                            rs.getString("asset_code")
                    );

                    return item;
                },
                stockReturnId
        );

        AssetPackRepository.attachSelections(jdbcTemplate, "STOCK_RETURN", stockReturnId, items);

        return items;
    }

    public int updateStockReturn(
            UUID stockReturnId,
            StockReturn stockReturn
    ) {

        String sql = """
                UPDATE stock_return
                SET
                    from_project_code = ?,
                    to_project_code = ?,
                    gin_id = ?,
                    return_type = ?,
                    po_code = ?,
                    supplier_code = ?,
                    reason = ?,
                    remark = ?,
                    return_date = ?,
                    return_by = ?,
                    approved_date = ?,
                    approved_by = ?,
                    is_approved = ?,
                    gate_verified_by = ?,
                    gate_verified_date = ?,
                    is_gate_verified = ?,
                    arrival_gate_verified_by = ?,
                    arrival_gate_verified_date = ?,
                    is_arrival_gate_verified = ?,
                    received_by = ?,
                    vehicle_asset_code = ?,
                    vehicle_no = ?,
                    for_asset_code = ?,
                    invoice_number = ?,
                    delivery_note_number = ?
                WHERE stock_return_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                stockReturn.getFromProjectCode(),
                stockReturn.getToProjectCode(),
                stockReturn.getGinId(),
                stockReturn.getReturnType(),
                stockReturn.getPoCode(),
                stockReturn.getSupplierCode(),
                stockReturn.getReason(),
                stockReturn.getRemark(),
                stockReturn.getReturnDate(),
                stockReturn.getReturnBy(),
                stockReturn.getApprovedDate(),
                stockReturn.getApprovedBy(),
                Boolean.TRUE.equals(stockReturn.getIsApproved()),
                stockReturn.getGateVerifiedBy(),
                stockReturn.getGateVerifiedDate(),
                Boolean.TRUE.equals(stockReturn.getIsGateVerified()),
                stockReturn.getArrivalGateVerifiedBy(),
                stockReturn.getArrivalGateVerifiedDate(),
                Boolean.TRUE.equals(stockReturn.getIsArrivalGateVerified()),
                stockReturn.getReceivedBy(),
                stockReturn.getVehicleAssetCode(),
                stockReturn.getVehicleNo(),
                stockReturn.getForAssetCode(),
                stockReturn.getInvoiceNumber(),
                stockReturn.getDeliveryNoteNumber(),
                stockReturnId
        );
    }


    /** Sets only the gate-verification columns — used to record security's confirmation
     * after a stock return is already approved, when the general updateStockReturn path is locked. */
    public int updateGateVerification(
            UUID stockReturnId,
            String gateVerifiedBy,
            java.time.LocalDateTime gateVerifiedDate
    ) {

        String sql = """
                UPDATE stock_return
                SET
                    gate_verified_by = ?,
                    gate_verified_date = ?,
                    is_gate_verified = TRUE
                WHERE stock_return_id = ?
                """;

        return jdbcTemplate.update(sql, gateVerifiedBy, gateVerifiedDate, stockReturnId);
    }

    public void deleteItems(UUID stockReturnId) {

        String sql = """
                DELETE FROM stock_return_item
                WHERE stock_return_id = ?
                """;

        jdbcTemplate.update(sql, stockReturnId);
    }


    private StockReturn mapStockReturn(
            java.sql.ResultSet rs
    ) throws java.sql.SQLException {

        StockReturn stockReturn =
                new StockReturn();

        stockReturn.setStockReturnId(
                rs.getObject(
                        "stock_return_id",
                        UUID.class
                )
        );

        stockReturn.setStockReturnCode(
                rs.getString("stock_return_code")
        );

        stockReturn.setFromProjectCode(
                rs.getString("from_project_code")
        );

        stockReturn.setToProjectCode(
                rs.getString("to_project_code")
        );

        stockReturn.setGinId(
                rs.getObject(
                        "gin_id",
                        UUID.class
                )
        );

        stockReturn.setReturnType(
                rs.getString("return_type")
        );

        stockReturn.setPoCode(
                rs.getString("po_code")
        );

        stockReturn.setSupplierCode(
                rs.getString("supplier_code")
        );

        stockReturn.setReason(
                rs.getString("reason")
        );

        stockReturn.setRemark(
                rs.getString("remark")
        );

        stockReturn.setReturnDate(
                rs.getObject(
                        "return_date",
                        java.time.LocalDateTime.class
                )
        );

        stockReturn.setReturnBy(
                rs.getString("return_by")
        );

        stockReturn.setApprovedDate(
                rs.getObject(
                        "approved_date",
                        java.time.LocalDateTime.class
                )
        );

        stockReturn.setApprovedBy(
                rs.getString("approved_by")
        );

        stockReturn.setIsApproved(
                rs.getBoolean("is_approved")
        );

        stockReturn.setGateVerifiedBy(
                rs.getString("gate_verified_by")
        );

        stockReturn.setGateVerifiedDate(
                rs.getObject(
                        "gate_verified_date",
                        java.time.LocalDateTime.class
                )
        );

        stockReturn.setIsGateVerified(
                (Boolean) rs.getObject("is_gate_verified")
        );

        stockReturn.setArrivalGateVerifiedBy(
                rs.getString("arrival_gate_verified_by")
        );

        stockReturn.setArrivalGateVerifiedDate(
                rs.getObject(
                        "arrival_gate_verified_date",
                        java.time.LocalDateTime.class
                )
        );

        stockReturn.setIsArrivalGateVerified(
                (Boolean) rs.getObject("is_arrival_gate_verified")
        );

        stockReturn.setReceivedBy(rs.getString("received_by"));
        stockReturn.setVehicleAssetCode(rs.getString("vehicle_asset_code"));
        stockReturn.setVehicleNo(rs.getString("vehicle_no"));
        stockReturn.setForAssetCode(rs.getString("for_asset_code"));
        stockReturn.setInvoiceNumber(rs.getString("invoice_number"));
        stockReturn.setDeliveryNoteNumber(rs.getString("delivery_note_number"));

        return stockReturn;
    }

    /** Sets only the arrival gate-verification columns — the receiving project's security
     * confirming these returned goods arrived at their gate, recorded before any GRN exists
     * for this return. */
    public int updateArrivalGateVerification(
            UUID stockReturnId,
            String gateVerifiedBy,
            java.time.LocalDateTime gateVerifiedDate
    ) {

        String sql = """
                UPDATE stock_return
                SET
                    arrival_gate_verified_by = ?,
                    arrival_gate_verified_date = ?,
                    is_arrival_gate_verified = TRUE
                WHERE stock_return_id = ?
                """;

        return jdbcTemplate.update(sql, gateVerifiedBy, gateVerifiedDate, stockReturnId);
    }

    /** Stock returns leaving this project, not yet confirmed leaving through the exit gate —
     * what the sending project's security sees on the Gate Passes page. Shown from the moment
     * a return is created, regardless of approval state — the gate check is independent of
     * the approval workflow. */
    public List<StockReturn> findPendingExitGate(String fromProjectCode) {

        String sql = """
                SELECT *
                FROM stock_return
                WHERE from_project_code = ?
                  AND is_gate_verified = FALSE
                ORDER BY return_date
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapStockReturn(rs), fromProjectCode);
    }

    /** Stock returns addressed to this project that have left their source gate but have not
     * yet been confirmed arriving at this project's gate, and have no GRN raised for them yet —
     * what the receiving project's security sees on the Gate Passes page. */
    public List<StockReturn> findPendingArrivalGate(String toProjectCode) {

        String sql = """
                SELECT *
                FROM stock_return
                WHERE to_project_code = ?
                  AND is_gate_verified = TRUE
                  AND is_arrival_gate_verified = FALSE
                  AND NOT EXISTS (SELECT 1 FROM grn WHERE grn.stock_return_id = stock_return.stock_return_id)
                ORDER BY return_date
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapStockReturn(rs), toProjectCode);
    }
}
