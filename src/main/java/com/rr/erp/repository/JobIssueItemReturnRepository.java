package com.rr.erp.repository;

import com.rr.erp.entity.JobIssueItemReturn;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JobIssueItemReturnRepository {

    private final JdbcTemplate jdbcTemplate;


    // =========================================================
    // INSERT
    // =========================================================
    public int insert(JobIssueItemReturn item) {

        String sql = """
                INSERT INTO job_issue_item_return (
                    job_issue_item_return_id,
                    job_card_id,
                    job_issue_item_id,
                    project_code,
                    item_code,
                    quantity,
                    unit_price,
                    returned_by,
                    returned_date,
                    remarks,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                item.getJobIssueItemReturnId(),
                item.getJobCardId(),
                item.getJobIssueItemId(),
                item.getProjectCode(),
                item.getItemCode(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getReturnedBy(),
                item.getReturnedDate(),
                item.getRemarks(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }


    // =========================================================
    // GET BY JOB CARD ID
    // =========================================================
    public List<JobIssueItemReturn> getByJobCardId(UUID jobCardId) {

        String sql = """
                SELECT
                    job_issue_item_return_id AS "jobIssueItemReturnId",
                    job_card_id AS "jobCardId",
                    job_issue_item_id AS "jobIssueItemId",
                    project_code AS "projectCode",
                    item_code AS "itemCode",
                    quantity,
                    unit_price AS "unitPrice",
                    returned_by AS "returnedBy",
                    returned_date AS "returnedDate",
                    remarks,
                    created_at AS "createdAt",
                    updated_at AS "updatedAt"
                FROM job_issue_item_return
                WHERE job_card_id = ?
                ORDER BY created_at DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapReturn(rs), jobCardId);
    }


    // =========================================================
    // TOTAL QUANTITY ALREADY RETURNED FOR A GIVEN ISSUE LINE
    // =========================================================
    public BigDecimal getReturnedQuantity(UUID jobIssueItemId) {

        String sql = """
                SELECT COALESCE(SUM(quantity), 0)
                FROM job_issue_item_return
                WHERE job_issue_item_id = ?
                """;

        BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class, jobIssueItemId);
        return result != null ? result : BigDecimal.ZERO;
    }


    // =========================================================
    // MAPPER
    // =========================================================
    private JobIssueItemReturn mapReturn(ResultSet rs) throws SQLException {

        JobIssueItemReturn item = new JobIssueItemReturn();

        item.setJobIssueItemReturnId(rs.getObject("jobIssueItemReturnId", UUID.class));
        item.setJobCardId(rs.getObject("jobCardId", UUID.class));
        item.setJobIssueItemId(rs.getObject("jobIssueItemId", UUID.class));
        item.setProjectCode(rs.getString("projectCode"));
        item.setItemCode(rs.getString("itemCode"));
        item.setQuantity(rs.getBigDecimal("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unitPrice"));
        item.setReturnedBy(rs.getString("returnedBy"));
        item.setReturnedDate(rs.getObject("returnedDate", LocalDateTime.class));
        item.setRemarks(rs.getString("remarks"));
        item.setCreatedAt(rs.getObject("createdAt", LocalDateTime.class));
        item.setUpdatedAt(rs.getObject("updatedAt", LocalDateTime.class));

        return item;
    }
}
