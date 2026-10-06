package com.rr.erp.repository;

import com.rr.erp.dto.IntraProjectIssueItemSource;
import com.rr.erp.entity.IntraProjectIssue;
import com.rr.erp.entity.IntraProjectIssueItem;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class IntraProjectIssueRepository {

    private final JdbcTemplate jdbcTemplate;

    public IntraProjectIssueRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int createIntraProjectIssue(IntraProjectIssue issue) {

        String sql = """
                INSERT INTO intra_project_issue (
                    intra_project_issue_id,
                    intra_project_issue_code,
                    issued_by,
                    issued_date,
                    issued_project_code,
                    issue_type,
                    received_project_phase_id,
                    received_by,
                    received_date,
                    approved_by,
                    approved_date,
                    is_authorized,
                    is_issued,
                    is_received,
                    subcontractor_id,
                    receiver_name,
                    receiver_nic,
                    expected_return_date,
                    job_card_id,
                    for_asset_code
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                issue.getIntraProjectIssueId(),
                issue.getIntraProjectIssueCode(),
                issue.getIssuedBy(),
                issue.getIssuedDate(),
                issue.getIssuedProjectCode(),
                issue.getIssueType(),
                issue.getReceivedProjectPhaseId(),
                issue.getReceivedBy(),
                issue.getReceivedDate(),
                issue.getApprovedBy(),
                issue.getApprovedDate(),
                issue.getIsAuthorized(),
                issue.getIsIssued(),
                issue.getIsReceived(),
                issue.getSubcontractorId(),
                issue.getReceiverName(),
                issue.getReceiverNic(),
                issue.getExpectedReturnDate(),
                issue.getJobCardId(),
                issue.getForAssetCode()
        );
    }

    public int updateIntraProjectIssue(UUID intraProjectIssueId, IntraProjectIssue issue) {

        String sql = """
                UPDATE intra_project_issue
                SET
                    intra_project_issue_code = ?,
                    issued_by = ?,
                    issued_date = ?,
                    issued_project_code = ?,
                    issue_type = ?,
                    received_project_phase_id = ?,
                    received_by = ?,
                    received_date = ?,
                    approved_by = ?,
                    approved_date = ?,
                    is_authorized = ?,
                    is_issued = ?,
                    is_received = ?,
                    subcontractor_id = ?,
                    receiver_name = ?,
                    receiver_nic = ?,
                    expected_return_date = ?,
                    job_card_id = ?,
                    for_asset_code = ?
                WHERE intra_project_issue_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                issue.getIntraProjectIssueCode(),
                issue.getIssuedBy(),
                issue.getIssuedDate(),
                issue.getIssuedProjectCode(),
                issue.getIssueType(),
                issue.getReceivedProjectPhaseId(),
                issue.getReceivedBy(),
                issue.getReceivedDate(),
                issue.getApprovedBy(),
                issue.getApprovedDate(),
                issue.getIsAuthorized(),
                issue.getIsIssued(),
                issue.getIsReceived(),
                issue.getSubcontractorId(),
                issue.getReceiverName(),
                issue.getReceiverNic(),
                issue.getExpectedReturnDate(),
                issue.getJobCardId(),
                issue.getForAssetCode(),
                intraProjectIssueId
        );
    }

    public void createIntraProjectIssueItem(IntraProjectIssueItem item) {

        String sql = """
                INSERT INTO intra_project_issue_item (
                    intra_project_issue_item_id,
                    intra_project_issue_id,
                    item_code,
                    description,
                    size,
                    uom_id,
                    quantity,
                    remarks,
                    unit_price,
                    amount,
                    issue_item_type_id,
                    length_m,
                    width_m,
                    asset_code
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                item.getIntraProjectIssueItemId(),
                item.getIntraProjectIssueId(),
                item.getItemCode(),
                item.getDescription(),
                item.getSize(),
                item.getUom(),
                item.getQuantity(),
                item.getRemarks(),
                item.getUnitPrice(),
                item.getAmount(),
                item.getIssueItemTypeId(),
                item.getLengthM(),
                item.getWidthM(),
                item.getAssetCode()
        );
    }

    public void deleteIntraProjectIssueItems(UUID intraProjectIssueId) {

        String sql = """
                DELETE FROM intra_project_issue_item
                WHERE intra_project_issue_id = ?
                """;

        jdbcTemplate.update(sql, intraProjectIssueId);
    }

    public List<IntraProjectIssueItem> getIntraProjectIssueItems(UUID intraProjectIssueId) {

        String sql = """
                SELECT
                    intra_project_issue_item_id,
                    intra_project_issue_id,
                    item_code,
                    description,
                    size,
                    uom_id,
                    quantity,
                    remarks,
                    unit_price,
                    amount,
                    issue_item_type_id,
                    length_m,
                    width_m,
                    asset_code
                FROM intra_project_issue_item
                WHERE intra_project_issue_id = ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapIntraProjectIssueItem(rs),
                intraProjectIssueId
        );
    }

    public Optional<IntraProjectIssue> findById(UUID intraProjectIssueId) {

        String sql = """
                SELECT
                    intra_project_issue_id,
                    intra_project_issue_code,
                    issued_by,
                    issued_date,
                    issued_project_code,
                    issue_type,
                    received_project_phase_id,
                    received_by,
                    received_date,
                    approved_by,
                    approved_date,
                    is_authorized,
                    is_issued,
                    is_received,
                    subcontractor_id,
                    receiver_name,
                    receiver_nic,
                    expected_return_date,
                    job_card_id,
                    for_asset_code
                FROM intra_project_issue
                WHERE intra_project_issue_id = ?
                """;

        try {
            IntraProjectIssue issue = jdbcTemplate.queryForObject(
                    sql,
                    (rs, rowNum) -> mapIntraProjectIssue(rs),
                    intraProjectIssueId
            );
            return Optional.ofNullable(issue);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<IntraProjectIssue> getIntraProjectIssues(String issuedProjectCode) {

        String sql = """
                SELECT
                    intra_project_issue_id,
                    intra_project_issue_code,
                    issued_by,
                    issued_date,
                    issued_project_code,
                    issue_type,
                    received_project_phase_id,
                    received_by,
                    received_date,
                    approved_by,
                    approved_date,
                    is_authorized,
                    is_issued,
                    is_received,
                    subcontractor_id,
                    receiver_name,
                    receiver_nic,
                    expected_return_date,
                    job_card_id,
                    for_asset_code
                FROM intra_project_issue
                WHERE issued_project_code = ?
                ORDER BY issued_date DESC
                """;

        List<IntraProjectIssue> issues = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapIntraProjectIssue(rs),
                issuedProjectCode
        );

        for (IntraProjectIssue issue : issues) {
            issue.setItems(getIntraProjectIssueItems(issue.getIntraProjectIssueId()));
        }

        return issues;
    }

    /**
     * How many lines currently hold this asset out: issued lines (a SUBCONTRACTOR / JOB_CARD issue
     * only counts once authorized) that have not been fully returned, Pending returns included.
     * Assets on these issues are not tracked by location, so this is what stops one asset being
     * issued twice. {@code excludingIssueId} leaves out the issue being saved.
     */
    public int countOutstandingIssuesForAsset(String assetCode, UUID excludingIssueId) {

        String sql = """
                SELECT COUNT(*)
                FROM intra_project_issue_item item
                JOIN intra_project_issue issue ON issue.intra_project_issue_id = item.intra_project_issue_id
                WHERE item.asset_code = ?
                  AND issue.intra_project_issue_id <> ?
                  AND (issue.is_authorized = TRUE OR issue.issue_type NOT IN ('SUBCONTRACTOR', 'JOB_CARD'))
                  AND item.quantity - COALESCE((
                        SELECT SUM(ri.quantity)
                        FROM intra_project_issue_return_item ri
                        WHERE ri.intra_project_issue_item_id = item.intra_project_issue_item_id
                      ), 0) > 0
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, assetCode, excludingIssueId);
        return count != null ? count : 0;
    }

    // Resolves which issue (and, in turn, which project) one issued line came from — used
    // when recording a return so a line can be validated and, once approved, credited back
    // to the project that actually issued it, even when other lines on the same return
    // came from a different issue (e.g. a subcontractor or employee return spanning
    // several intra project issues).
    public Optional<IntraProjectIssueItemSource> findItemSource(UUID intraProjectIssueItemId) {

        String sql = """
                SELECT
                    issue.intra_project_issue_id,
                    issue.issued_project_code,
                    issue.issue_type,
                    item.item_code,
                    item.quantity,
                    item.length_m,
                    item.width_m,
                    item.asset_code
                FROM intra_project_issue_item item
                JOIN intra_project_issue issue ON issue.intra_project_issue_id = item.intra_project_issue_id
                WHERE item.intra_project_issue_item_id = ?
                """;

        try {
            IntraProjectIssueItemSource source = jdbcTemplate.queryForObject(
                    sql,
                    (rs, rowNum) -> {
                        IntraProjectIssueItemSource result = new IntraProjectIssueItemSource();
                        result.setIntraProjectIssueId(rs.getObject("intra_project_issue_id", UUID.class));
                        result.setIssuedProjectCode(rs.getString("issued_project_code"));
                        result.setIssueType(rs.getString("issue_type"));
                        result.setItemCode(rs.getString("item_code"));
                        result.setQuantity(rs.getBigDecimal("quantity"));
                        result.setLengthM(rs.getBigDecimal("length_m"));
                        result.setWidthM(rs.getBigDecimal("width_m"));
                        result.setAssetCode(rs.getString("asset_code"));
                        return result;
                    },
                    intraProjectIssueItemId
            );
            return Optional.ofNullable(source);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // Every intra project issue across every project — used by HQ-wide report views and
    // employee-wise summaries (an employee may hold items issued from several projects).
    public List<IntraProjectIssue> getAllIntraProjectIssues() {

        String sql = """
                SELECT
                    intra_project_issue_id,
                    intra_project_issue_code,
                    issued_by,
                    issued_date,
                    issued_project_code,
                    issue_type,
                    received_project_phase_id,
                    received_by,
                    received_date,
                    approved_by,
                    approved_date,
                    is_authorized,
                    is_issued,
                    is_received,
                    subcontractor_id,
                    receiver_name,
                    receiver_nic,
                    expected_return_date,
                    job_card_id,
                    for_asset_code
                FROM intra_project_issue
                ORDER BY issued_date DESC
                """;

        List<IntraProjectIssue> issues = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapIntraProjectIssue(rs)
        );

        for (IntraProjectIssue issue : issues) {
            issue.setItems(getIntraProjectIssueItems(issue.getIntraProjectIssueId()));
        }

        return issues;
    }

    private IntraProjectIssueItem mapIntraProjectIssueItem(ResultSet rs) throws SQLException {

        IntraProjectIssueItem item = new IntraProjectIssueItem();

        item.setIntraProjectIssueItemId(rs.getObject("intra_project_issue_item_id", UUID.class));
        item.setIntraProjectIssueId(rs.getObject("intra_project_issue_id", UUID.class));
        item.setItemCode(rs.getString("item_code"));
        item.setDescription(rs.getString("description"));
        item.setSize(rs.getString("size"));
        item.setUom(rs.getObject("uom_id", Integer.class));
        item.setQuantity(rs.getBigDecimal("quantity"));
        item.setRemarks(rs.getString("remarks"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setAmount(rs.getBigDecimal("amount"));
        item.setIssueItemTypeId(rs.getObject("issue_item_type_id", Integer.class));
        item.setLengthM(rs.getBigDecimal("length_m"));
        item.setWidthM(rs.getBigDecimal("width_m"));
        item.setAssetCode(rs.getString("asset_code"));

        return item;
    }

    private IntraProjectIssue mapIntraProjectIssue(ResultSet rs) throws SQLException {

        IntraProjectIssue issue = new IntraProjectIssue();

        issue.setIntraProjectIssueId(rs.getObject("intra_project_issue_id", UUID.class));
        issue.setIntraProjectIssueCode(rs.getString("intra_project_issue_code"));
        issue.setIssuedBy(rs.getString("issued_by"));

        Timestamp issuedDate = rs.getTimestamp("issued_date");
        issue.setIssuedDate(issuedDate != null ? issuedDate.toLocalDateTime() : null);

        issue.setIssuedProjectCode(rs.getString("issued_project_code"));
        issue.setIssueType(rs.getString("issue_type"));
        issue.setReceivedProjectPhaseId(rs.getObject("received_project_phase_id", Integer.class));
        issue.setReceivedBy(rs.getString("received_by"));

        Timestamp receivedDate = rs.getTimestamp("received_date");
        issue.setReceivedDate(receivedDate != null ? receivedDate.toLocalDateTime() : null);

        issue.setApprovedBy(rs.getString("approved_by"));

        Timestamp approvedDate = rs.getTimestamp("approved_date");
        issue.setApprovedDate(approvedDate != null ? approvedDate.toLocalDateTime() : null);

        issue.setIsAuthorized(rs.getObject("is_authorized", Boolean.class));
        issue.setIsIssued(rs.getObject("is_issued", Boolean.class));
        issue.setIsReceived(rs.getObject("is_received", Boolean.class));

        issue.setSubcontractorId(rs.getObject("subcontractor_id", Integer.class));
        issue.setReceiverName(rs.getString("receiver_name"));
        issue.setReceiverNic(rs.getString("receiver_nic"));
        issue.setExpectedReturnDate(rs.getObject("expected_return_date", java.time.LocalDate.class));
        issue.setJobCardId(rs.getObject("job_card_id", UUID.class));
        issue.setForAssetCode(rs.getString("for_asset_code"));

        return issue;
    }

    // Every JOB_CARD-type issue made against one job card — used by the job card page's
    // read-only "issued via intra project issue" display.
    public List<IntraProjectIssue> getByJobCardId(UUID jobCardId) {

        String sql = """
                SELECT
                    intra_project_issue_id,
                    intra_project_issue_code,
                    issued_by,
                    issued_date,
                    issued_project_code,
                    issue_type,
                    received_project_phase_id,
                    received_by,
                    received_date,
                    approved_by,
                    approved_date,
                    is_authorized,
                    is_issued,
                    is_received,
                    subcontractor_id,
                    receiver_name,
                    receiver_nic,
                    expected_return_date,
                    job_card_id,
                    for_asset_code
                FROM intra_project_issue
                WHERE job_card_id = ?
                ORDER BY issued_date DESC
                """;

        List<IntraProjectIssue> issues = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapIntraProjectIssue(rs),
                jobCardId
        );

        for (IntraProjectIssue issue : issues) {
            issue.setItems(getIntraProjectIssueItems(issue.getIntraProjectIssueId()));
        }

        return issues;
    }
}
