package com.rr.erp.repository;

import com.rr.erp.entity.PO;
import com.rr.erp.entity.POItem;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PORepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<PO> poRowMapper = (rs, rowNum) -> {

        PO po = new PO();

        po.setPoId(rs.getObject("po_id", UUID.class));
        po.setPoCode(rs.getString("po_code"));
        po.setPoDate(rs.getObject("po_date", LocalDate.class));
        po.setSupplierCode(rs.getString("supplier_code"));
        po.setProjectCode(rs.getString("project_code"));
        po.setBillToProjectCode(rs.getString("bill_to_project_code"));
        po.setDeliveryLocation(rs.getString("delivery_location"));
        po.setFreight(rs.getString("freight"));
        po.setOrderDueDate(rs.getObject("order_due_date", LocalDate.class));
        po.setPaymentTerm(rs.getString("payment_term"));
        po.setPaymentType(rs.getString("payment_type"));
        po.setSupplierRefNo(rs.getString("supplier_ref_no"));
        po.setVATRegNo(rs.getString("vat_reg_no"));
        po.setSVATNo(rs.getString("svat_no"));
        po.setSsclApplicable(rs.getBoolean("sscl_applicable"));
        po.setSsclPercentage(rs.getBigDecimal("sscl_percentage"));
        po.setSsclAmount(rs.getBigDecimal("sscl_amount"));
        po.setVatPercentage(rs.getBigDecimal("vat_percentage"));
        po.setVatAmount(rs.getBigDecimal("vat_amount"));
        po.setTotalValue(rs.getBigDecimal("total_value"));
        po.setRequestedDate(rs.getObject("requested_date", LocalDateTime.class));
        po.setRequestedBy(rs.getString("requested_by"));
        po.setApprovedDate(rs.getObject("approved_date", LocalDateTime.class));
        po.setApprovedBy(rs.getString("approved_by"));
        po.setIsApproved(rs.getBoolean("is_approved"));
        po.setApprovalStatus(rs.getString("approval_status"));
        po.setStatus(rs.getString("status"));
        po.setStatusReason(rs.getString("status_reason"));
        po.setStatusChangedBy(rs.getString("status_changed_by"));
        po.setStatusChangedDate(rs.getObject("status_changed_date", LocalDateTime.class));
        po.setCurrencyId(rs.getInt("currency_id"));
        po.setMrId(rs.getObject("mr_id", UUID.class));
        po.setMrRequestingProjectCode(rs.getString("mr_requesting_project_code"));
        po.setRemarks(rs.getString("remarks"));

        return po;
    };

    public int createPO(PO po) {

        String sql = """
                INSERT INTO po (
                    po_id,
                    po_code,
                    po_date,
                    supplier_code,
                    project_code,
                    bill_to_project_code,
                    delivery_location,
                    freight,
                    order_due_date,
                    payment_term,
                    payment_type,
                    currency_id,
                    supplier_ref_no,
                    vat_reg_no,
                    svat_no,
                    sscl_applicable,
                    sscl_percentage,
                    sscl_amount,
                    vat_percentage,
                    vat_amount,
                    total_value,
                    requested_date,
                    requested_by,
                    approved_date,
                    approved_by,
                    is_approved,
                    approval_status,
                    status,
                    status_reason,
                    status_changed_by,
                    status_changed_date,
                    mr_id,
                    mr_requesting_project_code,
                    remarks
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                po.getPoId(),
                po.getPoCode(),
                po.getPoDate(),
                po.getSupplierCode(),
                po.getProjectCode(),
                po.getBillToProjectCode(),
                po.getDeliveryLocation(),
                po.getFreight(),
                po.getOrderDueDate(),
                po.getPaymentTerm(),
                po.getPaymentType(),
                po.getCurrencyId(),
                po.getSupplierRefNo(),
                po.getVATRegNo(),
                po.getSVATNo(),
                po.getSsclApplicable(),
                po.getSsclPercentage(),
                po.getSsclAmount(),
                po.getVatPercentage(),
                po.getVatAmount(),
                po.getTotalValue(),
                po.getRequestedDate(),
                po.getRequestedBy(),
                po.getApprovedDate(),
                po.getApprovedBy(),
                po.getIsApproved(),
                po.getApprovalStatus(),
                po.getStatus(),
                po.getStatusReason(),
                po.getStatusChangedBy(),
                po.getStatusChangedDate(),
                po.getMrId(),
                po.getMrRequestingProjectCode(),
                po.getRemarks()
        );
    }


    public int createPOItem(
            UUID poId,
            POItem item
    ) {

        String sql = """
                INSERT INTO po_item (
                    po_item_id,
                    po_id,
                    item_code,
                    description,
                    part_no,
                    supplier_name,
                    uom_id,
                    quantity,
                    unit_price,
                    amount
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                item.getPoItemId(),
                poId,
                item.getItemCode(),
                item.getDescription(),
                item.getPartNo(),
                item.getSupplierName(),
                item.getUomId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getAmount()
        );
    }


    public List<PO> getCreatedPOs(
            String billToProjectCode
    ) {

        String sql = """
            SELECT *
            FROM po
            WHERE bill_to_project_code = ?
            ORDER BY po_date DESC, po_code DESC
            """;

        return jdbcTemplate.query(
                sql,
                poRowMapper,
                billToProjectCode
        );
    }

    public List<PO> getPOsByDestinationProject(
            String projectCode
    ) {

        String sql = """
            SELECT *
            FROM po
            WHERE project_code = ? and is_approved = TRUE
            ORDER BY po_date DESC, po_code DESC
            """;

        return jdbcTemplate.query(
                sql,
                poRowMapper,
                projectCode
        );
    }

    public PO findById(UUID poId) {

        String sql = """
            SELECT *
            FROM po
            WHERE po_id = ?
            """;

        List<PO> results = jdbcTemplate.query(
                sql,
                poRowMapper,
                poId
        );

        return results.isEmpty() ? null : results.get(0);
    }

    public List<PO> findAll() {

        String sql = """
            SELECT *
            FROM po
            ORDER BY po_date DESC, po_code DESC
            """;

        return jdbcTemplate.query(
                sql,
                poRowMapper
        );
    }

    // Every PO regardless of project — used by the HQ-wide Purchase Orders list and
    // company-wide report views. LIMIT/OFFSET keeps each call to a bounded page instead
    // of the whole table, matching the pagination pattern used by GRN/GIN/MR/etc.
    public List<PO> findAll(int page, int size) {

        String sql = """
            SELECT *
            FROM po
            ORDER BY po_date DESC, po_code DESC
            LIMIT ? OFFSET ?
            """;

        return jdbcTemplate.query(
                sql,
                poRowMapper,
                size,
                page * size
        );
    }

    public long countAll() {

        String sql = """
            SELECT COUNT(*)
            FROM po
            """;

        Long count = jdbcTemplate.queryForObject(sql, Long.class);

        return count == null ? 0 : count;
    }

    public List<POItem> getPOItems(UUID poId) {

        String sql = """
                SELECT *
                FROM po_item
                WHERE po_id = ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    POItem item = new POItem();

                    item.setPoItemId(
                            rs.getObject(
                                    "po_item_id",
                                    UUID.class
                            )
                    );

                    item.setItemCode(
                            rs.getString("item_code")
                    );

                    item.setDescription(
                            rs.getString("description")
                    );

                    item.setPartNo(
                            rs.getString("part_no")
                    );

                    item.setSupplierName(
                            rs.getString("supplier_name")
                    );

                    item.setUomId(
                            rs.getObject(
                                    "uom_id",
                                    Integer.class
                            )
                    );

                    item.setQuantity(
                            rs.getObject(
                                    "quantity",
                                    Integer.class
                            )
                    );

                    item.setUnitPrice(
                            rs.getBigDecimal("unit_price")
                    );

                    item.setAmount(
                            rs.getBigDecimal("amount")
                    );

                    return item;
                },
                poId
        );
    }

    // Batched equivalent of calling getPOItems() once per PO — one query for a whole page
    // of POs instead of one query per PO, which is what made loading many POs slow.
    public Map<UUID, List<POItem>> getPOItemsByPoIds(List<UUID> poIds) {

        if (poIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = poIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT * FROM po_item WHERE po_id IN (" + placeholders + ")";

        Map<UUID, List<POItem>> itemsByPoId = new HashMap<>();

        jdbcTemplate.query(
                sql,
                rs -> {

                    UUID poId = rs.getObject("po_id", UUID.class);

                    POItem item = new POItem();
                    item.setPoItemId(rs.getObject("po_item_id", UUID.class));
                    item.setItemCode(rs.getString("item_code"));
                    item.setDescription(rs.getString("description"));
                    item.setPartNo(rs.getString("part_no"));
                    item.setSupplierName(rs.getString("supplier_name"));
                    item.setUomId(rs.getObject("uom_id", Integer.class));
                    item.setQuantity(rs.getObject("quantity", Integer.class));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setAmount(rs.getBigDecimal("amount"));

                    itemsByPoId.computeIfAbsent(poId, key -> new ArrayList<>()).add(item);
                },
                poIds.toArray()
        );

        return itemsByPoId;
    }


    public int updatePO(
            UUID poId,
            PO po
    ) {

        String sql = """
                UPDATE po
                SET
                    po_code = ?,
                    po_date = ?,
                    supplier_code = ?,
                    project_code = ?,
                    bill_to_project_code = ?,
                    delivery_location = ?,
                    freight = ?,
                    order_due_date = ?,
                    payment_term = ?,
                    payment_type = ?,
                    currency_id = ?,
                    supplier_ref_no = ?,
                    vat_reg_no = ?,
                    svat_no = ?,
                    sscl_applicable = ?,
                    sscl_percentage = ?,
                    sscl_amount = ?,
                    vat_percentage = ?,
                    vat_amount = ?,
                    total_value = ?,
                    requested_date = ?,
                    requested_by = ?,
                    approved_date = ?,
                    approved_by = ?,
                    is_approved = ?,
                    approval_status = ?,
                    status = ?,
                    status_reason = ?,
                    status_changed_by = ?,
                    status_changed_date = ?,
                    mr_id = ?,
                    mr_requesting_project_code = ?,
                    remarks = ?
                WHERE po_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                po.getPoCode(),
                po.getPoDate(),
                po.getSupplierCode(),
                po.getProjectCode(),
                po.getBillToProjectCode(),
                po.getDeliveryLocation(),
                po.getFreight(),
                po.getOrderDueDate(),
                po.getPaymentTerm(),
                po.getPaymentType(),
                po.getCurrencyId(),
                po.getSupplierRefNo(),
                po.getVATRegNo(),
                po.getSVATNo(),
                po.getSsclApplicable(),
                po.getSsclPercentage(),
                po.getSsclAmount(),
                po.getVatPercentage(),
                po.getVatAmount(),
                po.getTotalValue(),
                po.getRequestedDate(),
                po.getRequestedBy(),
                po.getApprovedDate(),
                po.getApprovedBy(),
                po.getIsApproved(),
                po.getApprovalStatus(),
                po.getStatus(),
                po.getStatusReason(),
                po.getStatusChangedBy(),
                po.getStatusChangedDate(),
                po.getMrId(),
                po.getMrRequestingProjectCode(),
                po.getRemarks(),
                poId
        );
    }

    public int updateTotals(
            UUID poId,
            BigDecimal ssclAmount,
            BigDecimal vatAmount
    ) {

        String sql = """
                UPDATE po
                SET
                    sscl_amount = ?,
                    vat_amount = ?
                WHERE po_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                ssclAmount,
                vatAmount,
                poId
        );
    }

    /**
     * The raw supplier_code column for a PO, looked up by its code — blank/null means it's a
     * multiple-supplier PO (see PurchaseOrderFormPage's isMultiSupplier, derived the same way
     * on the frontend). Used by GRNService to validate a GRN raised against it. Null if no PO
     * has that code.
     */
    public String findSupplierCodeByPoCode(String poCode) {

        String sql = "SELECT supplier_code FROM po WHERE po_code = ?";

        List<String> results = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("supplier_code"),
                poCode
        );

        return results.isEmpty() ? null : results.get(0);
    }

    public POItem findPOItemByPoCodeAndItemCode(
            String poCode,
            String itemCode
    ) {

        String sql = """
                SELECT pi.*
                FROM po_item pi
                JOIN po p ON p.po_id = pi.po_id
                WHERE p.po_code = ? AND pi.item_code = ?
                """;

        List<POItem> results = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    POItem item = new POItem();

                    item.setPoItemId(rs.getObject("po_item_id", UUID.class));
                    item.setItemCode(rs.getString("item_code"));
                    item.setDescription(rs.getString("description"));
                    item.setPartNo(rs.getString("part_no"));
                    item.setSupplierName(rs.getString("supplier_name"));
                    item.setUomId(rs.getObject("uom_id", Integer.class));
                    item.setQuantity(rs.getObject("quantity", Integer.class));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setAmount(rs.getBigDecimal("amount"));

                    return item;
                },
                poCode,
                itemCode
        );

        return results.isEmpty() ? null : results.get(0);
    }

    public int deletePOItems(UUID poId) {

        String sql = """
                DELETE FROM po_item
                WHERE po_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                poId
        );
    }
}
