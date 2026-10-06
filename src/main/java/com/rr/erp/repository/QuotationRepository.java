package com.rr.erp.repository;

import com.rr.erp.entity.Quotation;
import com.rr.erp.entity.QuotationItem;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class QuotationRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Quotation> quotationRowMapper = (rs, rowNum) -> {
        Quotation quotation = new Quotation();

        quotation.setQuotationId(rs.getObject("quotation_id", UUID.class));
        quotation.setQuotationCode(rs.getString("quotation_code"));
        quotation.setQuotationRequestId(rs.getObject("quotation_request_id", UUID.class));
        quotation.setSupplierCode(rs.getString("supplier_code"));
        quotation.setQuotationDate(rs.getObject("quotation_date", LocalDate.class));
        quotation.setValidUntil(rs.getObject("valid_until", LocalDate.class));
        quotation.setCurrencyId((Integer) rs.getObject("currency_id"));
        quotation.setPaymentTerm(rs.getString("payment_term"));
        quotation.setDeliveryTerm(rs.getString("delivery_term"));
        quotation.setRemark(rs.getString("remark"));
        quotation.setTotalValue(rs.getBigDecimal("total_value"));
        quotation.setStatus(rs.getString("status"));
        quotation.setReceivedDate(rs.getObject("received_date", LocalDateTime.class));
        quotation.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        quotation.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));

        return quotation;
    };

    private final RowMapper<QuotationItem> quotationItemRowMapper = (rs, rowNum) -> {
        QuotationItem item = new QuotationItem();

        item.setQuotationItemId(rs.getObject("quotation_item_id", UUID.class));
        item.setQuotationId(rs.getObject("quotation_id", UUID.class));
        item.setQuotationRequestItemId(rs.getObject("quotation_request_item_id", UUID.class));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setAmount(rs.getBigDecimal("amount"));
        item.setRemark(rs.getString("remark"));
        item.setIsSelected(rs.getBoolean("is_selected"));

        return item;
    };

    public void insertQuotation(Quotation quotation) {

        String sql = """
                INSERT INTO quotation (
                    quotation_id,
                    quotation_code,
                    quotation_request_id,
                    supplier_code,
                    quotation_date,
                    valid_until,
                    currency_id,
                    payment_term,
                    delivery_term,
                    remark,
                    total_value,
                    status,
                    received_date,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                quotation.getQuotationId(),
                quotation.getQuotationCode(),
                quotation.getQuotationRequestId(),
                quotation.getSupplierCode(),
                quotation.getQuotationDate(),
                quotation.getValidUntil(),
                quotation.getCurrencyId(),
                quotation.getPaymentTerm(),
                quotation.getDeliveryTerm(),
                quotation.getRemark(),
                quotation.getTotalValue(),
                quotation.getStatus(),
                quotation.getReceivedDate(),
                quotation.getCreatedAt(),
                quotation.getUpdatedAt()
        );
    }

    public int updateQuotation(UUID quotationId, Quotation quotation) {

        String sql = """
                UPDATE quotation
                SET
                    quotation_code = ?,
                    supplier_code = ?,
                    quotation_date = ?,
                    valid_until = ?,
                    currency_id = ?,
                    payment_term = ?,
                    delivery_term = ?,
                    remark = ?,
                    total_value = ?,
                    status = ?,
                    received_date = ?,
                    updated_at = ?
                WHERE quotation_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                quotation.getQuotationCode(),
                quotation.getSupplierCode(),
                quotation.getQuotationDate(),
                quotation.getValidUntil(),
                quotation.getCurrencyId(),
                quotation.getPaymentTerm(),
                quotation.getDeliveryTerm(),
                quotation.getRemark(),
                quotation.getTotalValue(),
                quotation.getStatus(),
                quotation.getReceivedDate(),
                quotation.getUpdatedAt(),
                quotationId
        );
    }

    public void insertItem(UUID quotationId, QuotationItem item) {

        String sql = """
                INSERT INTO quotation_item (
                    quotation_item_id,
                    quotation_id,
                    quotation_request_item_id,
                    quantity,
                    unit_price,
                    amount,
                    remark,
                    is_selected
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                item.getQuotationItemId(),
                quotationId,
                item.getQuotationRequestItemId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getAmount(),
                item.getRemark(),
                item.getIsSelected() != null && item.getIsSelected()
        );
    }

    public void deleteItemsByQuotationId(UUID quotationId) {

        jdbcTemplate.update(
                "DELETE FROM quotation_item WHERE quotation_id = ?",
                quotationId
        );
    }

    public List<QuotationItem> findItemsByQuotationId(UUID quotationId) {

        String sql = """
                SELECT
                    quotation_item_id,
                    quotation_id,
                    quotation_request_item_id,
                    quantity,
                    unit_price,
                    amount,
                    remark,
                    is_selected
                FROM quotation_item
                WHERE quotation_id = ?
                ORDER BY quotation_item_id
                """;

        return jdbcTemplate.query(sql, quotationItemRowMapper, quotationId);
    }

    public int updateItemSelection(UUID quotationItemId, boolean isSelected) {

        return jdbcTemplate.update(
                "UPDATE quotation_item SET is_selected = ? WHERE quotation_item_id = ?",
                isSelected,
                quotationItemId
        );
    }

    public List<Quotation> findAll() {

        String sql = """
                SELECT
                    quotation_id,
                    quotation_code,
                    quotation_request_id,
                    supplier_code,
                    quotation_date,
                    valid_until,
                    currency_id,
                    payment_term,
                    delivery_term,
                    remark,
                    total_value,
                    status,
                    received_date,
                    created_at,
                    updated_at
                FROM quotation
                ORDER BY quotation_date DESC, quotation_code DESC
                """;

        return jdbcTemplate.query(sql, quotationRowMapper);
    }

    public List<Quotation> findByQuotationRequestId(UUID quotationRequestId) {

        String sql = """
                SELECT
                    quotation_id,
                    quotation_code,
                    quotation_request_id,
                    supplier_code,
                    quotation_date,
                    valid_until,
                    currency_id,
                    payment_term,
                    delivery_term,
                    remark,
                    total_value,
                    status,
                    received_date,
                    created_at,
                    updated_at
                FROM quotation
                WHERE quotation_request_id = ?
                ORDER BY quotation_date DESC, quotation_code DESC
                """;

        return jdbcTemplate.query(sql, quotationRowMapper, quotationRequestId);
    }

    public Optional<Quotation> findById(UUID quotationId) {

        String sql = """
                SELECT
                    quotation_id,
                    quotation_code,
                    quotation_request_id,
                    supplier_code,
                    quotation_date,
                    valid_until,
                    currency_id,
                    payment_term,
                    delivery_term,
                    remark,
                    total_value,
                    status,
                    received_date,
                    created_at,
                    updated_at
                FROM quotation
                WHERE quotation_id = ?
                """;

        List<Quotation> results = jdbcTemplate.query(sql, quotationRowMapper, quotationId);

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public boolean existsById(UUID quotationId) {

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM quotation WHERE quotation_id = ?",
                Integer.class,
                quotationId
        );

        return count != null && count > 0;
    }

    public void deleteById(UUID quotationId) {

        jdbcTemplate.update(
                "DELETE FROM quotation WHERE quotation_id = ?",
                quotationId
        );
    }

    public List<Quotation> hydrateAll(List<Quotation> quotations) {

        List<Quotation> hydrated = new ArrayList<>();

        for (Quotation quotation : quotations) {
            hydrated.add(hydrate(quotation));
        }

        return hydrated;
    }

    public Quotation hydrate(Quotation quotation) {

        quotation.setItems(findItemsByQuotationId(quotation.getQuotationId()));

        return quotation;
    }
}
