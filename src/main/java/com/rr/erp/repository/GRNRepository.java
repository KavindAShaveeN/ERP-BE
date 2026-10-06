package com.rr.erp.repository;

import com.rr.erp.entity.GRN;
import com.rr.erp.entity.GRNItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class GRNRepository {

    private final JdbcTemplate jdbcTemplate;

    public GRNRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<GRN> grnRowMapper = (rs, rowNum) -> {
        GRN grn = new GRN();

        grn.setGrnId(rs.getObject("grn_id", UUID.class));
        grn.setGrnCode(rs.getString("grn_code"));
        grn.setFromProjectCode(rs.getString("from_project_code"));
        grn.setIsSupplierGRN(
                (Boolean) rs.getObject("is_supplier_grn")
        );
        grn.setSupplierCode(rs.getString("supplier_code"));
        grn.setInvoiceDate(
                rs.getObject("invoice_date", java.time.LocalDate.class)
        );
        grn.setInvoiceNumber(rs.getString("invoice_number"));
        grn.setDeliveryNoteNumber(rs.getString("delivery_note_number"));
        grn.setToProjectCode(rs.getString("to_project_code"));
        grn.setCheckedDate(
                rs.getObject("checked_date", java.time.LocalDateTime.class)
        );
        grn.setCheckedBy(rs.getString("checked_by"));
        grn.setApprovedDate(
                rs.getObject("approved_date", java.time.LocalDateTime.class)
        );
        grn.setApprovedBy(rs.getString("approved_by"));
        grn.setGrnDate(
                rs.getObject("grn_date", java.time.LocalDate.class)
        );
        grn.setPoCode(rs.getString("po_code"));
        grn.setIsApproved(
                (Boolean) rs.getObject("is_approved")
        );
        grn.setGinId(rs.getObject("gin_id", UUID.class));
        grn.setStockReturnId(rs.getObject("stock_return_id", UUID.class));
        grn.setVehicleAssetCode(rs.getString("vehicle_asset_code"));
        grn.setVehicleNo(rs.getString("vehicle_no"));
        grn.setGateVerifiedBy(rs.getString("gate_verified_by"));
        grn.setGateVerifiedDate(
                rs.getObject("gate_verified_date", java.time.LocalDateTime.class)
        );
        grn.setIsGateVerified(
                (Boolean) rs.getObject("is_gate_verified")
        );

        return grn;
    };

    private final RowMapper<GRNItem> grnItemRowMapper = (rs, rowNum) ->
            new GRNItem(
                    rs.getObject("grn_item_id", UUID.class),
                    rs.getObject("grn_id", UUID.class),
                    rs.getString("item_code"),
                    rs.getString("description"),
                    rs.getString("supplier_name"),
                    rs.getString("size"),
                    rs.getInt("uom_id"),
                    rs.getBigDecimal("quantity"),
                    rs.getInt("unit_price"),
                    rs.getBigDecimal("amount"),
                    rs.getBigDecimal("conversion_factor"),
                    rs.getBigDecimal("po_equivalent_qty"),
                    rs.getBigDecimal("length_m"),
                    rs.getBigDecimal("width_m"),
                    rs.getString("asset_code"),
                    rs.getObject("expiry_date", java.time.LocalDate.class),
                    null
            );

    public GRN insertGRN(GRN grn) {

        UUID grnId = grn.getGrnId() != null ? grn.getGrnId() : UUID.randomUUID();
        grn.setGrnId(grnId);

        String sql = """
                INSERT INTO grn (
                    grn_id,
                    grn_code,
                    from_project_code,
                    is_supplier_grn,
                    supplier_code,
                    invoice_date,
                    invoice_number,
                    delivery_note_number,
                    to_project_code,
                    checked_date,
                    checked_by,
                    approved_date,
                    approved_by,
                    grn_date,
                    po_code,
                    is_approved,
                    gin_id,
                    stock_return_id,
                    vehicle_asset_code,
                    vehicle_no,
                    gate_verified_by,
                    gate_verified_date,
                    is_gate_verified
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                grn.getGrnId(),
                grn.getGrnCode(),
                grn.getFromProjectCode(),
                grn.getIsSupplierGRN(),
                grn.getSupplierCode(),
                grn.getInvoiceDate(),
                grn.getInvoiceNumber(),
                grn.getDeliveryNoteNumber(),
                grn.getToProjectCode(),
                grn.getCheckedDate(),
                grn.getCheckedBy(),
                grn.getApprovedDate(),
                grn.getApprovedBy(),
                grn.getGrnDate(),
                grn.getPoCode(),
                grn.getIsApproved(),
                grn.getGinId(),
                grn.getStockReturnId(),
                grn.getVehicleAssetCode(),
                grn.getVehicleNo(),
                grn.getGateVerifiedBy(),
                grn.getGateVerifiedDate(),
                grn.getIsGateVerified()
        );

        insertItems(grnId, grn.getItems());

        return grn;
    }

    public void insertItems(UUID grnId, List<GRNItem> items) {

        String sql = """
                INSERT INTO grn_item (
                    grn_item_id,
                    grn_id,
                    item_code,
                    description,
                    supplier_name,
                    size,
                    uom_id,
                    quantity,
                    unit_price,
                    amount,
                    conversion_factor,
                    po_equivalent_qty,
                    length_m,
                    width_m,
                    asset_code,
                    expiry_date
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        for (GRNItem item : items) {

            UUID grnItemId = UUID.randomUUID();

            item.setGrnItemId(grnItemId);
            item.setGrnId(grnId);

            jdbcTemplate.update(
                    sql,
                    item.getGrnItemId(),
                    item.getGrnId(),
                    item.getItemCode(),
                    item.getDescription(),
                    item.getSupplierName(),
                    item.getSize(),
                    item.getUomId(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getAmount(),
                    item.getConversionFactor(),
                    item.getPoEquivalentQty(),
                    item.getLengthM(),
                    item.getWidthM(),
                    item.getAssetCode(),
                    item.getExpiryDate()
            );
        }
    }

    public List<GRN> findByFromProjectCode(
            String fromProjectCode,
            int page,
            int size
    ) {

        String sql = """
                SELECT *
                FROM grn
                WHERE from_project_code = ?
                ORDER BY grn_date DESC, grn_code DESC
                LIMIT ? OFFSET ?
                """;

        List<GRN> grns = jdbcTemplate.query(
                sql,
                grnRowMapper,
                fromProjectCode,
                size,
                page * size
        );

        addItemsToGRNs(grns);

        return grns;
    }

    public long countByFromProjectCode(String fromProjectCode) {

        String sql = """
                SELECT COUNT(*)
                FROM grn
                WHERE from_project_code = ?
                """;

        Long count = jdbcTemplate.queryForObject(sql, Long.class, fromProjectCode);

        return count == null ? 0 : count;
    }

    public List<GRN> findByToProjectCode(
            String toProjectCode,
            int page,
            int size
    ) {

        String sql = """
                SELECT *
                FROM grn
                WHERE to_project_code = ? AND is_approved = TRUE
                ORDER BY grn_date DESC, grn_code DESC
                LIMIT ? OFFSET ?
                """;

        List<GRN> grns = jdbcTemplate.query(
                sql,
                grnRowMapper,
                toProjectCode,
                size,
                page * size
        );

        addItemsToGRNs(grns);

        return grns;
    }

    public long countByToProjectCode(String toProjectCode) {

        String sql = """
                SELECT COUNT(*)
                FROM grn
                WHERE to_project_code = ? AND is_approved = TRUE
                """;

        Long count = jdbcTemplate.queryForObject(sql, Long.class, toProjectCode);

        return count == null ? 0 : count;
    }

    // Every GRN regardless of project or type — used by HQ-wide report views.
    public List<GRN> findAll(int page, int size) {

        String sql = """
                SELECT *
                FROM grn
                ORDER BY grn_date DESC, grn_code DESC
                LIMIT ? OFFSET ?
                """;

        List<GRN> grns = jdbcTemplate.query(
                sql,
                grnRowMapper,
                size,
                page * size
        );

        addItemsToGRNs(grns);

        return grns;
    }

    public long countAll() {

        String sql = """
                SELECT COUNT(*)
                FROM grn
                """;

        Long count = jdbcTemplate.queryForObject(sql, Long.class);

        return count == null ? 0 : count;
    }

    public List<GRN> findSupplierGRNs(int page, int size) {

        String sql = """
                SELECT *
                FROM grn
                WHERE is_supplier_grn = TRUE and is_approved = TRUE
                ORDER BY grn_date DESC, grn_code DESC
                LIMIT ? OFFSET ?
                """;

        List<GRN> grns = jdbcTemplate.query(
                sql,
                grnRowMapper,
                size,
                page * size
        );

        addItemsToGRNs(grns);

        return grns;
    }

    public long countSupplierGRNs() {

        String sql = """
                SELECT COUNT(*)
                FROM grn
                WHERE is_supplier_grn = TRUE and is_approved = TRUE
                """;

        Long count = jdbcTemplate.queryForObject(sql, Long.class);

        return count == null ? 0 : count;
    }

    public Optional<GRN> findById(UUID grnId) {

        String sql = """
                SELECT *
                FROM grn
                WHERE grn_id = ?
                """;

        List<GRN> results = jdbcTemplate.query(
                sql,
                grnRowMapper,
                grnId
        );

        if (results.isEmpty()) {
            return Optional.empty();
        }

        GRN grn = results.get(0);
        grn.setItems(findItemsByGrnId(grnId));

        return Optional.of(grn);
    }

    /** The GRN already raised for a given GIN, if any — used to push a late arrival gate
     * confirmation onto a GRN that was created before security ticked it. */
    public Optional<GRN> findByGinId(UUID ginId) {

        String sql = """
                SELECT *
                FROM grn
                WHERE gin_id = ?
                """;

        List<GRN> results = jdbcTemplate.query(sql, grnRowMapper, ginId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /** The GRN already raised for a given stock return, if any — mirrors findByGinId. */
    public Optional<GRN> findByStockReturnId(UUID stockReturnId) {

        String sql = """
                SELECT *
                FROM grn
                WHERE stock_return_id = ?
                """;

        List<GRN> results = jdbcTemplate.query(sql, grnRowMapper, stockReturnId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /** Sets only the gate-verification columns — used to push a GIN's/return's arrival gate
     * confirmation onto a GRN that already existed at the time security ticked it. */
    public int updateGateVerification(UUID grnId, String gateVerifiedBy, java.time.LocalDateTime gateVerifiedDate) {

        String sql = """
                UPDATE grn
                SET
                    gate_verified_by = ?,
                    gate_verified_date = ?,
                    is_gate_verified = TRUE
                WHERE grn_id = ?
                """;

        return jdbcTemplate.update(sql, gateVerifiedBy, gateVerifiedDate, grnId);
    }

    public List<GRNItem> findItemsByGrnId(UUID grnId) {

        String sql = """
                SELECT
                    grn_item_id,
                    grn_id,
                    item_code,
                    description,
                    supplier_name,
                    size,
                    uom_id,
                    quantity,
                    unit_price,
                    amount,
                    conversion_factor,
                    po_equivalent_qty,
                    length_m,
                    width_m,
                    asset_code,
                    expiry_date
                FROM grn_item
                WHERE grn_id = ?
                ORDER BY grn_item_id
                """;

        List<GRNItem> items = jdbcTemplate.query(
                sql,
                grnItemRowMapper,
                grnId
        );

        AssetPackRepository.attachSelections(jdbcTemplate, "GRN", grnId, items);

        return items;
    }

    public int updateGRN(UUID grnId, GRN grn) {

        String sql = """
                UPDATE grn
                SET
                    grn_code = ?,
                    from_project_code = ?,
                    is_supplier_grn = ?,
                    supplier_code = ?,
                    invoice_date = ?,
                    invoice_number = ?,
                    delivery_note_number = ?,
                    to_project_code = ?,
                    checked_date = ?,
                    checked_by = ?,
                    approved_date = ?,
                    approved_by = ?,
                    grn_date = ?,
                    po_code = ?,
                    is_approved = ?,
                    gin_id = ?,
                    stock_return_id = ?,
                    vehicle_asset_code = ?,
                    vehicle_no = ?,
                    gate_verified_by = ?,
                    gate_verified_date = ?,
                    is_gate_verified = ?
                WHERE grn_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                grn.getGrnCode(),
                grn.getFromProjectCode(),
                grn.getIsSupplierGRN(),
                grn.getSupplierCode(),
                grn.getInvoiceDate(),
                grn.getInvoiceNumber(),
                grn.getDeliveryNoteNumber(),
                grn.getToProjectCode(),
                grn.getCheckedDate(),
                grn.getCheckedBy(),
                grn.getApprovedDate(),
                grn.getApprovedBy(),
                grn.getGrnDate(),
                grn.getPoCode(),
                grn.getIsApproved(),
                grn.getGinId(),
                grn.getStockReturnId(),
                grn.getVehicleAssetCode(),
                grn.getVehicleNo(),
                grn.getGateVerifiedBy(),
                grn.getGateVerifiedDate(),
                grn.getIsGateVerified(),
                grnId
        );
    }

    public void deleteItemsByGrnId(UUID grnId) {

        String sql = """
                DELETE FROM grn_item
                WHERE grn_id = ?
                """;

        jdbcTemplate.update(sql, grnId);
    }

    public boolean existsById(UUID grnId) {

        String sql = """
                SELECT COUNT(*)
                FROM grn
                WHERE grn_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                grnId
        );

        return count != null && count > 0;
    }

    private void addItemsToGRNs(List<GRN> grns) {

        for (GRN grn : grns) {
            grn.setItems(findItemsByGrnId(grn.getGrnId()));
        }
    }
}