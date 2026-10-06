package com.rr.erp.repository;

import com.rr.erp.entity.IntraProjectIssueReturn;
import com.rr.erp.entity.IntraProjectIssueReturnItem;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class IntraProjectIssueReturnRepository {

    private final JdbcTemplate jdbcTemplate;

    public IntraProjectIssueReturnRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<IntraProjectIssueReturn> findById(UUID intraProjectIssueReturnId) {

        String sql = """
                SELECT *
                FROM intra_project_issue_return
                WHERE intra_project_issue_return_id = ?
                """;

        try {
            IntraProjectIssueReturn stockReturn = jdbcTemplate.queryForObject(
                    sql,
                    (rs, rowNum) -> mapReturn(rs),
                    intraProjectIssueReturnId
            );
            return Optional.ofNullable(stockReturn);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void createReturn(IntraProjectIssueReturn stockReturn) {

        String sql = """
                INSERT INTO intra_project_issue_return (
                    intra_project_issue_return_id,
                    intra_project_issue_return_code,
                    intra_project_issue_id,
                    issued_project_code,
                    return_type,
                    subcontractor_id,
                    employee_code,
                    return_date,
                    returned_by,
                    remarks,
                    approved_by,
                    approved_date,
                    is_approved,
                    job_card_id,
                    from_asset_code
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                stockReturn.getIntraProjectIssueReturnId(),
                stockReturn.getIntraProjectIssueReturnCode(),
                stockReturn.getIntraProjectIssueId(),
                stockReturn.getIssuedProjectCode(),
                stockReturn.getReturnType(),
                stockReturn.getSubcontractorId(),
                stockReturn.getEmployeeCode(),
                stockReturn.getReturnDate(),
                stockReturn.getReturnedBy(),
                stockReturn.getRemarks(),
                stockReturn.getApprovedBy(),
                stockReturn.getApprovedDate(),
                Boolean.TRUE.equals(stockReturn.getIsApproved()),
                stockReturn.getJobCardId(),
                stockReturn.getFromAssetCode()
        );
    }

    public int updateReturn(UUID intraProjectIssueReturnId, IntraProjectIssueReturn stockReturn) {

        String sql = """
                UPDATE intra_project_issue_return
                SET
                    return_date = ?,
                    returned_by = ?,
                    remarks = ?,
                    approved_by = ?,
                    approved_date = ?,
                    is_approved = ?,
                    from_asset_code = ?
                WHERE intra_project_issue_return_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                stockReturn.getReturnDate(),
                stockReturn.getReturnedBy(),
                stockReturn.getRemarks(),
                stockReturn.getApprovedBy(),
                stockReturn.getApprovedDate(),
                Boolean.TRUE.equals(stockReturn.getIsApproved()),
                stockReturn.getFromAssetCode(),
                intraProjectIssueReturnId
        );
    }

    public void createReturnItem(UUID intraProjectIssueReturnId, IntraProjectIssueReturnItem item) {

        String sql = """
                INSERT INTO intra_project_issue_return_item (
                    intra_project_issue_return_item_id,
                    intra_project_issue_return_id,
                    intra_project_issue_item_id,
                    item_code,
                    description,
                    size,
                    uom_id,
                    quantity,
                    unit_price,
                    amount,
                    remarks,
                    length_m,
                    width_m,
                    asset_code
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                item.getIntraProjectIssueReturnItemId(),
                intraProjectIssueReturnId,
                item.getIntraProjectIssueItemId(),
                item.getItemCode(),
                item.getDescription(),
                item.getSize(),
                item.getUomId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getAmount(),
                item.getRemarks(),
                item.getLengthM(),
                item.getWidthM(),
                item.getAssetCode()
        );
    }

    public void deleteItems(UUID intraProjectIssueReturnId) {

        String sql = """
                DELETE FROM intra_project_issue_return_item
                WHERE intra_project_issue_return_id = ?
                """;

        jdbcTemplate.update(sql, intraProjectIssueReturnId);
    }

    public List<IntraProjectIssueReturnItem> getItems(UUID intraProjectIssueReturnId) {

        String sql = """
                SELECT *
                FROM intra_project_issue_return_item
                WHERE intra_project_issue_return_id = ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapReturnItem(rs),
                intraProjectIssueReturnId
        );
    }

    // Sum of everything already returned against one issued line, across every return
    // (Pending or Approved — a Pending return still claims that quantity so two returns
    // can't both draw against the same outstanding stock), optionally excluding one return
    // (the one being edited, so it doesn't count against itself).
    public java.math.BigDecimal getReturnedQuantityForItem(UUID intraProjectIssueItemId, UUID excludingReturnId) {

        java.math.BigDecimal total;

        if (excludingReturnId == null) {
            String sql = """
                    SELECT COALESCE(SUM(ri.quantity), 0)
                    FROM intra_project_issue_return_item ri
                    WHERE ri.intra_project_issue_item_id = ?
                    """;
            total = jdbcTemplate.queryForObject(sql, java.math.BigDecimal.class, intraProjectIssueItemId);
        } else {
            String sql = """
                    SELECT COALESCE(SUM(ri.quantity), 0)
                    FROM intra_project_issue_return_item ri
                    WHERE ri.intra_project_issue_item_id = ?
                      AND ri.intra_project_issue_return_id <> ?
                    """;
            total = jdbcTemplate.queryForObject(sql, java.math.BigDecimal.class, intraProjectIssueItemId, excludingReturnId);
        }

        return total != null ? total : java.math.BigDecimal.ZERO;
    }

    public List<IntraProjectIssueReturn> getReturnsByProject(String issuedProjectCode) {

        String sql = """
                SELECT *
                FROM intra_project_issue_return
                WHERE issued_project_code = ?
                ORDER BY return_date DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapReturn(rs),
                issuedProjectCode
        );
    }

    public List<IntraProjectIssueReturn> getReturnsByIssue(UUID intraProjectIssueId) {

        String sql = """
                SELECT *
                FROM intra_project_issue_return
                WHERE intra_project_issue_id = ?
                ORDER BY return_date DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapReturn(rs),
                intraProjectIssueId
        );
    }

    // Every intra project issue return across every project — used by HQ-wide report views.
    public List<IntraProjectIssueReturn> getAllReturns() {

        String sql = """
                SELECT *
                FROM intra_project_issue_return
                ORDER BY return_date DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapReturn(rs)
        );
    }

    private IntraProjectIssueReturnItem mapReturnItem(ResultSet rs) throws SQLException {

        IntraProjectIssueReturnItem item = new IntraProjectIssueReturnItem();

        item.setIntraProjectIssueReturnItemId(rs.getObject("intra_project_issue_return_item_id", UUID.class));
        item.setIntraProjectIssueReturnId(rs.getObject("intra_project_issue_return_id", UUID.class));
        item.setIntraProjectIssueItemId(rs.getObject("intra_project_issue_item_id", UUID.class));
        item.setItemCode(rs.getString("item_code"));
        item.setDescription(rs.getString("description"));
        item.setSize(rs.getString("size"));
        item.setUomId(rs.getObject("uom_id", Integer.class));
        item.setQuantity(rs.getBigDecimal("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setAmount(rs.getBigDecimal("amount"));
        item.setRemarks(rs.getString("remarks"));
        item.setLengthM(rs.getBigDecimal("length_m"));
        item.setWidthM(rs.getBigDecimal("width_m"));
        item.setAssetCode(rs.getString("asset_code"));

        return item;
    }

    private IntraProjectIssueReturn mapReturn(ResultSet rs) throws SQLException {

        IntraProjectIssueReturn stockReturn = new IntraProjectIssueReturn();

        stockReturn.setIntraProjectIssueReturnId(rs.getObject("intra_project_issue_return_id", UUID.class));
        stockReturn.setIntraProjectIssueReturnCode(rs.getString("intra_project_issue_return_code"));
        stockReturn.setIntraProjectIssueId(rs.getObject("intra_project_issue_id", UUID.class));
        stockReturn.setIssuedProjectCode(rs.getString("issued_project_code"));
        stockReturn.setReturnType(rs.getString("return_type"));
        stockReturn.setSubcontractorId(rs.getObject("subcontractor_id", Integer.class));
        stockReturn.setEmployeeCode(rs.getString("employee_code"));
        stockReturn.setReturnDate(rs.getObject("return_date", java.time.LocalDate.class));
        stockReturn.setReturnedBy(rs.getString("returned_by"));
        stockReturn.setRemarks(rs.getString("remarks"));
        stockReturn.setApprovedBy(rs.getString("approved_by"));
        stockReturn.setApprovedDate(rs.getObject("approved_date", java.time.LocalDate.class));
        stockReturn.setIsApproved(rs.getBoolean("is_approved"));
        stockReturn.setJobCardId(rs.getObject("job_card_id", UUID.class));
        stockReturn.setFromAssetCode(rs.getString("from_asset_code"));

        return stockReturn;
    }

    // Every JOB_CARD-type return recorded against one job card — used by the job card
    // page's read-only "returned via intra project issue" display.
    public List<IntraProjectIssueReturn> getReturnsByJobCard(UUID jobCardId) {

        String sql = """
                SELECT *
                FROM intra_project_issue_return
                WHERE job_card_id = ?
                ORDER BY return_date DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapReturn(rs),
                jobCardId
        );
    }
}
