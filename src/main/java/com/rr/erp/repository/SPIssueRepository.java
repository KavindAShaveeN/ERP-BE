package com.rr.erp.repository;

import com.rr.erp.entity.SPIssue;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class SPIssueRepository {

    private final JdbcTemplate jdbcTemplate;

    public SPIssueRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int createSPIssue(SPIssue spIssue) {

        String sql = """
                INSERT INTO sp_issue (
                    sp_issue_id,
                    sp_issue_code,
                    asset_code,
                    item_code,
                    description,
                    requested_by,
                    requested_date,
                    issued_by,
                    issued_date,
                    approved_by,
                    approved_date,
                    is_approved,
                    remarks
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        if (spIssue.getSpIssueId() == null) {
            spIssue.setSpIssueId(UUID.randomUUID());
        }

        if (spIssue.getIsApproved() == null) {
            spIssue.setIsApproved(false);
        }

        return jdbcTemplate.update(
                sql,
                spIssue.getSpIssueId(),
                spIssue.getSpIssueCode(),
                spIssue.getAssetCode(),
                spIssue.getItemCode(),
                spIssue.getDescription(),
                spIssue.getRequestedBy(),
                spIssue.getRequestedDate(),
                spIssue.getIssuedBy(),
                spIssue.getIssuedDate(),
                spIssue.getApprovedBy(),
                spIssue.getApprovedDate(),
                spIssue.getIsApproved(),
                spIssue.getRemarks()
        );
    }

    public SPIssue getSPIssueById(UUID spIssueId) {

        String sql = """
                SELECT
                    sp_issue_id AS "spIssueId",
                    sp_issue_code AS "spIssueCode",
                    asset_code AS "assetCode",
                    item_code AS "itemCode",
                    description AS "description",
                    requested_by AS "requestedBy",
                    requested_date AS "requestedDate",
                    issued_by AS "issuedBy",
                    issued_date AS "issuedDate",
                    approved_by AS "approvedBy",
                    approved_date AS "approvedDate",
                    is_approved AS "isApproved",
                    remarks AS "remarks"
                FROM sp_issue
                WHERE sp_issue_id = ?
                """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> {

                    SPIssue spIssue = new SPIssue();

                    spIssue.setSpIssueId(
                            rs.getObject("spIssueId", UUID.class)
                    );

                    spIssue.setSpIssueCode(rs.getString("spIssueCode"));
                    spIssue.setAssetCode(rs.getObject("assetCode", Integer.class));
                    spIssue.setItemCode(rs.getObject("itemCode", Integer.class));

                    spIssue.setDescription(rs.getString("description"));

                    spIssue.setRequestedBy(rs.getString("requestedBy"));

                    if (rs.getTimestamp("requestedDate") != null) {
                        spIssue.setRequestedDate(
                                rs.getTimestamp("requestedDate").toLocalDateTime()
                        );
                    }

                    spIssue.setIssuedBy(rs.getString("issuedBy"));

                    if (rs.getTimestamp("issuedDate") != null) {
                        spIssue.setIssuedDate(
                                rs.getTimestamp("issuedDate").toLocalDateTime()
                        );
                    }

                    spIssue.setApprovedBy(rs.getString("approvedBy"));

                    if (rs.getTimestamp("approvedDate") != null) {
                        spIssue.setApprovedDate(
                                rs.getTimestamp("approvedDate").toLocalDateTime()
                        );
                    }

                    spIssue.setIsApproved(rs.getBoolean("isApproved"));
                    spIssue.setRemarks(rs.getString("remarks"));

                    return spIssue;
                },
                spIssueId
        );
    }

    public List<SPIssue> getAllSPIssues() {

        String sql = """
                SELECT
                    sp_issue_id AS "spIssueId",
                    sp_issue_code AS "spIssueCode",
                    asset_code AS "assetCode",
                    item_code AS "itemCode",
                    description AS "description",
                    requested_by AS "requestedBy",
                    requested_date AS "requestedDate",
                    issued_by AS "issuedBy",
                    issued_date AS "issuedDate",
                    approved_by AS "approvedBy",
                    approved_date AS "approvedDate",
                    is_approved AS "isApproved",
                    remarks AS "remarks"
                FROM sp_issue
                ORDER BY requested_date DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    SPIssue spIssue = new SPIssue();

                    spIssue.setSpIssueId(
                            rs.getObject("spIssueId", UUID.class)
                    );

                    spIssue.setSpIssueCode(rs.getString("spIssueCode"));
                    spIssue.setAssetCode(rs.getObject("assetCode", Integer.class));
                    spIssue.setItemCode(rs.getObject("itemCode", Integer.class));

                    spIssue.setDescription(rs.getString("description"));
                    spIssue.setRequestedBy(rs.getString("requestedBy"));

                    if (rs.getTimestamp("requestedDate") != null) {
                        spIssue.setRequestedDate(
                                rs.getTimestamp("requestedDate").toLocalDateTime()
                        );
                    }

                    spIssue.setIssuedBy(rs.getString("issuedBy"));

                    if (rs.getTimestamp("issuedDate") != null) {
                        spIssue.setIssuedDate(
                                rs.getTimestamp("issuedDate").toLocalDateTime()
                        );
                    }

                    spIssue.setApprovedBy(rs.getString("approvedBy"));

                    if (rs.getTimestamp("approvedDate") != null) {
                        spIssue.setApprovedDate(
                                rs.getTimestamp("approvedDate").toLocalDateTime()
                        );
                    }

                    spIssue.setIsApproved(rs.getBoolean("isApproved"));
                    spIssue.setRemarks(rs.getString("remarks"));

                    return spIssue;
                }
        );
    }

    public int updateSPIssue(
            UUID spIssueId,
            SPIssue spIssue
    ) {

        String sql = """
                UPDATE sp_issue
                SET
                    sp_issue_code = ?,
                    asset_code = ?,
                    item_code = ?,
                    description = ?,
                    requested_by = ?,
                    requested_date = ?,
                    issued_by = ?,
                    issued_date = ?,
                    approved_by = ?,
                    approved_date = ?,
                    is_approved = ?,
                    remarks = ?
                WHERE sp_issue_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                spIssue.getSpIssueCode(),
                spIssue.getAssetCode(),
                spIssue.getItemCode(),
                spIssue.getDescription(),
                spIssue.getRequestedBy(),
                spIssue.getRequestedDate(),
                spIssue.getIssuedBy(),
                spIssue.getIssuedDate(),
                spIssue.getApprovedBy(),
                spIssue.getApprovedDate(),
                spIssue.getIsApproved(),
                spIssue.getRemarks(),
                spIssueId
        );
    }
}