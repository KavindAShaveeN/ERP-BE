package com.rr.erp.repository;

import com.rr.erp.dto.JobIssueItemResponse;
import com.rr.erp.entity.JobIssueItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JobIssueItemRepository {

    private final JdbcTemplate jdbcTemplate;

    public JobIssueItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // =========================================================
    // POST - Insert list of Job Issue Items
    // =========================================================
    public void saveAll(List<JobIssueItem> items) {

        String sql = """
                INSERT INTO job_issue_item (
                    job_issue_item_id,
                    job_card_id,
                    project_code,
                    item_code,
                    part_number,
                    serial_number,
                    description,
                    unit_price,
                    quantity,
                    issued_by,
                    issued_date,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.batchUpdate(
                sql,
                items,
                items.size(),
                (ps, item) -> {

                    if (item.getJobIssueItemId() == null) {
                        item.setJobIssueItemId(UUID.randomUUID());
                    }

                    LocalDateTime now = LocalDateTime.now();

                    item.setCreatedAt(now);
                    item.setUpdatedAt(now);

                    ps.setObject(1, item.getJobIssueItemId());
                    ps.setObject(2, item.getJobCardId());
                    ps.setString(3, item.getProjectCode());
                    ps.setString(4, item.getItemCode());
                    ps.setString(5, item.getPartNumber());
                    ps.setString(6, item.getSerialNumber());
                    ps.setString(7, item.getDescription());
                    ps.setBigDecimal(8, item.getUnitPrice());
                    ps.setBigDecimal(9, item.getQuantity());
                    ps.setString(10, item.getIssuedBy());

                    if (item.getIssuedDate() != null) {
                        ps.setTimestamp(
                                11,
                                Timestamp.valueOf(item.getIssuedDate())
                        );
                    } else {
                        ps.setTimestamp(11, null);
                    }

                    ps.setTimestamp(
                            12,
                            Timestamp.valueOf(item.getCreatedAt())
                    );

                    ps.setTimestamp(
                            13,
                            Timestamp.valueOf(item.getUpdatedAt())
                    );
                }
        );
    }


    // =========================================================
    // PUT - Update Job Issue Item
    // =========================================================
    public int update(
            UUID jobIssueItemId,
            JobIssueItem item
    ) {

        String sql = """
                UPDATE job_issue_item
                SET
                    job_card_id = ?,
                    item_code = ?,
                    part_number = ?,
                    serial_number = ?,
                    description = ?,
                    unit_price = ?,
                    quantity = ?,
                    issued_by = ?,
                    issued_date = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE job_issue_item_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                item.getJobCardId(),
                item.getItemCode(),
                item.getPartNumber(),
                item.getSerialNumber(),
                item.getDescription(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getIssuedBy(),
                item.getIssuedDate(),
                jobIssueItemId
        );
    }


    // =========================================================
    // GET - Get one job issue item by its ID
    // =========================================================
    public Optional<JobIssueItem> findById(UUID jobIssueItemId) {

        String sql = """
                SELECT
                    job_issue_item_id AS "jobIssueItemId",
                    job_card_id AS "jobCardId",
                    project_code AS "projectCode",
                    item_code AS "itemCode",
                    part_number AS "partNumber",
                    serial_number AS "serialNumber",
                    description AS "description",
                    unit_price AS "unitPrice",
                    quantity AS "quantity",
                    issued_by AS "issuedBy",
                    issued_date AS "issuedDate",
                    created_at AS "createdAt",
                    updated_at AS "updatedAt"
                FROM job_issue_item
                WHERE job_issue_item_id = ?
                """;

        List<JobIssueItem> result = jdbcTemplate.query(sql, (rs, rowNum) -> mapJobIssueItem(rs), jobIssueItemId);

        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    private JobIssueItem mapJobIssueItem(ResultSet rs) throws SQLException {

        JobIssueItem item = new JobIssueItem();
        item.setJobIssueItemId(rs.getObject("jobIssueItemId", UUID.class));
        item.setJobCardId(rs.getObject("jobCardId", UUID.class));
        item.setProjectCode(rs.getString("projectCode"));
        item.setItemCode(rs.getString("itemCode"));
        item.setPartNumber(rs.getString("partNumber"));
        item.setSerialNumber(rs.getString("serialNumber"));
        item.setDescription(rs.getString("description"));
        item.setUnitPrice(rs.getBigDecimal("unitPrice"));
        item.setQuantity(rs.getBigDecimal("quantity"));
        item.setIssuedBy(rs.getString("issuedBy"));
        item.setIssuedDate(rs.getObject("issuedDate", LocalDateTime.class));
        item.setCreatedAt(rs.getObject("createdAt", LocalDateTime.class));
        item.setUpdatedAt(rs.getObject("updatedAt", LocalDateTime.class));
        return item;
    }


    // =========================================================
    // GET - Get items by Job Card ID
    // =========================================================

    public List<JobIssueItemResponse> getByJobCardId(UUID jobCardId) {

        String sql = """
                SELECT
                    ji.job_issue_item_id AS "jobIssueItemId",
                    ji.job_card_id AS "jobCardId",
                    ji.project_code AS "projectCode",
                    ji.item_code AS "itemCode",
                    ji.part_number AS "partNumber",
                    ji.serial_number AS "serialNumber",
                    ji.description AS "description",
                    ji.unit_price AS "unitPrice",
                    ji.quantity AS "quantity",
                    ji.issued_by AS "issuedBy",
                    e.full_name AS "issuedPerson",
                    ji.issued_date AS "issuedDate",
                    ji.created_at AS "createdAt",
                    ji.updated_at AS "updatedAt"
                FROM job_issue_item ji
                LEFT JOIN employee e
                    ON e.employee_code = ji.issued_by
                WHERE ji.job_card_id = ?
                ORDER BY ji.created_at DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    JobIssueItemResponse item = new JobIssueItemResponse();

                    item.setJobIssueItemId(
                            rs.getObject("jobIssueItemId", java.util.UUID.class)
                    );

                    item.setJobCardId(
                            rs.getObject("jobCardId", java.util.UUID.class)
                    );

                    item.setProjectCode(
                            rs.getString("projectCode")
                    );

                    item.setItemCode(
                            rs.getString("itemCode")
                    );

                    item.setPartNumber(
                            rs.getString("partNumber")
                    );

                    item.setSerialNumber(
                            rs.getString("serialNumber")
                    );

                    item.setDescription(
                            rs.getString("description")
                    );

                    item.setUnitPrice(
                            rs.getBigDecimal("unitPrice")
                    );

                    item.setQuantity(
                            rs.getBigDecimal("quantity")
                    );

                    item.setIssuedBy(
                            rs.getString("issuedBy")
                    );

                    item.setIssuedPerson(
                            rs.getString("issuedPerson")
                    );

                    item.setIssuedDate(
                            rs.getObject("issuedDate", java.time.LocalDateTime.class)
                    );

                    item.setCreatedAt(
                            rs.getObject("createdAt", java.time.LocalDateTime.class)
                    );

                    item.setUpdatedAt(
                            rs.getObject("updatedAt", java.time.LocalDateTime.class)
                    );

                    return item;
                },
                jobCardId
        );
    }
}