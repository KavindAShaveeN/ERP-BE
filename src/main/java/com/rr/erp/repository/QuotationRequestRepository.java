package com.rr.erp.repository;

import com.rr.erp.entity.QuotationRequest;
import com.rr.erp.entity.QuotationRequestItem;
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
public class QuotationRequestRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<QuotationRequest> quotationRequestRowMapper = (rs, rowNum) -> {
        QuotationRequest request = new QuotationRequest();

        request.setQuotationRequestId(rs.getObject("quotation_request_id", UUID.class));
        request.setQuotationRequestCode(rs.getString("quotation_request_code"));
        UUID mrId = rs.getObject("mr_id", UUID.class);
        request.setMrId(mrId == null ? "" : mrId.toString());
        request.setRequestDate(rs.getObject("request_date", LocalDateTime.class));
        request.setRequestedBy(rs.getString("requested_by"));
        request.setDueDate(rs.getObject("due_date", LocalDate.class));
        request.setRemark(rs.getString("remark"));
        request.setStatus(rs.getString("status"));
        request.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        request.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));

        return request;
    };

    private final RowMapper<QuotationRequestItem> quotationRequestItemRowMapper = (rs, rowNum) -> {
        QuotationRequestItem item = new QuotationRequestItem();

        item.setQuotationRequestItemId(rs.getObject("quotation_request_item_id", UUID.class));
        item.setQuotationRequestId(rs.getObject("quotation_request_id", UUID.class));
        item.setMrItemId(rs.getObject("mr_item_id", UUID.class));
        item.setItemCode(rs.getString("item_code_code"));
        item.setDescription(rs.getString("description"));
        item.setUomId((Integer) rs.getObject("uom_id"));
        item.setQuantity(rs.getInt("quantity"));

        return item;
    };

    public void insertQuotationRequest(QuotationRequest request) {

        String sql = """
                INSERT INTO quotation_request (
                    quotation_request_id,
                    quotation_request_code,
                    mr_id,
                    request_date,
                    requested_by,
                    due_date,
                    remark,
                    status,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                request.getQuotationRequestId(),
                request.getQuotationRequestCode(),
                parseMrId(request.getMrId()),
                request.getRequestDate(),
                request.getRequestedBy(),
                request.getDueDate(),
                request.getRemark(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }

    // The frontend sends "" (not a missing field) when no MR is selected, since
    // an optional MR is represented as an empty string on QuotationRequestApi.
    private static UUID parseMrId(String mrId) {
        return (mrId == null || mrId.isBlank()) ? null : UUID.fromString(mrId);
    }

    public int updateQuotationRequest(UUID quotationRequestId, QuotationRequest request) {

        String sql = """
                UPDATE quotation_request
                SET
                    quotation_request_code = ?,
                    mr_id = ?,
                    request_date = ?,
                    requested_by = ?,
                    due_date = ?,
                    remark = ?,
                    status = ?,
                    updated_at = ?
                WHERE quotation_request_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                request.getQuotationRequestCode(),
                parseMrId(request.getMrId()),
                request.getRequestDate(),
                request.getRequestedBy(),
                request.getDueDate(),
                request.getRemark(),
                request.getStatus(),
                request.getUpdatedAt(),
                quotationRequestId
        );
    }

    public void insertItem(UUID quotationRequestId, QuotationRequestItem item) {

        String sql = """
                INSERT INTO quotation_request_item (
                    quotation_request_item_id,
                    quotation_request_id,
                    mr_item_id,
                    item_code_code,
                    description,
                    uom_id,
                    quantity
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                item.getQuotationRequestItemId(),
                quotationRequestId,
                item.getMrItemId(),
                item.getItemCode(),
                item.getDescription(),
                item.getUomId(),
                item.getQuantity()
        );
    }

    public void deleteItemsByQuotationRequestId(UUID quotationRequestId) {

        jdbcTemplate.update(
                "DELETE FROM quotation_request_item WHERE quotation_request_id = ?",
                quotationRequestId
        );
    }

    public List<QuotationRequestItem> findItemsByQuotationRequestId(UUID quotationRequestId) {

        String sql = """
                SELECT
                    quotation_request_item_id,
                    quotation_request_id,
                    mr_item_id,
                    item_code_code,
                    description,
                    uom_id,
                    quantity
                FROM quotation_request_item
                WHERE quotation_request_id = ?
                ORDER BY quotation_request_item_id
                """;

        return jdbcTemplate.query(sql, quotationRequestItemRowMapper, quotationRequestId);
    }

    public void insertSupplierCode(UUID quotationRequestId, String supplierCode) {

        jdbcTemplate.update(
                "INSERT INTO quotation_request_supplier (quotation_request_id, supplier_code) VALUES (?, ?)",
                quotationRequestId,
                supplierCode
        );
    }

    public void deleteSuppliersByQuotationRequestId(UUID quotationRequestId) {

        jdbcTemplate.update(
                "DELETE FROM quotation_request_supplier WHERE quotation_request_id = ?",
                quotationRequestId
        );
    }

    public List<String> findSupplierCodesByQuotationRequestId(UUID quotationRequestId) {

        return jdbcTemplate.queryForList(
                "SELECT supplier_code FROM quotation_request_supplier WHERE quotation_request_id = ? ORDER BY supplier_code",
                String.class,
                quotationRequestId
        );
    }

    public List<QuotationRequest> findAll() {

        String sql = """
                SELECT
                    quotation_request_id,
                    quotation_request_code,
                    mr_id,
                    request_date,
                    requested_by,
                    due_date,
                    remark,
                    status,
                    created_at,
                    updated_at
                FROM quotation_request
                ORDER BY request_date DESC, quotation_request_code DESC
                """;

        return jdbcTemplate.query(sql, quotationRequestRowMapper);
    }

    public List<QuotationRequest> findByMrId(UUID mrId) {

        String sql = """
                SELECT
                    quotation_request_id,
                    quotation_request_code,
                    mr_id,
                    request_date,
                    requested_by,
                    due_date,
                    remark,
                    status,
                    created_at,
                    updated_at
                FROM quotation_request
                WHERE mr_id = ?
                ORDER BY request_date DESC, quotation_request_code DESC
                """;

        return jdbcTemplate.query(sql, quotationRequestRowMapper, mrId);
    }

    public Optional<QuotationRequest> findById(UUID quotationRequestId) {

        String sql = """
                SELECT
                    quotation_request_id,
                    quotation_request_code,
                    mr_id,
                    request_date,
                    requested_by,
                    due_date,
                    remark,
                    status,
                    created_at,
                    updated_at
                FROM quotation_request
                WHERE quotation_request_id = ?
                """;

        List<QuotationRequest> results = jdbcTemplate.query(sql, quotationRequestRowMapper, quotationRequestId);

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public boolean existsById(UUID quotationRequestId) {

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM quotation_request WHERE quotation_request_id = ?",
                Integer.class,
                quotationRequestId
        );

        return count != null && count > 0;
    }

    public void deleteById(UUID quotationRequestId) {

        jdbcTemplate.update(
                "DELETE FROM quotation_request WHERE quotation_request_id = ?",
                quotationRequestId
        );
    }

    public List<QuotationRequest> hydrateAll(List<QuotationRequest> requests) {

        List<QuotationRequest> hydrated = new ArrayList<>();

        for (QuotationRequest request : requests) {
            hydrated.add(hydrate(request));
        }

        return hydrated;
    }

    public QuotationRequest hydrate(QuotationRequest request) {

        request.setItems(findItemsByQuotationRequestId(request.getQuotationRequestId()));
        request.setSupplierCodes(findSupplierCodesByQuotationRequestId(request.getQuotationRequestId()));

        return request;
    }
}
