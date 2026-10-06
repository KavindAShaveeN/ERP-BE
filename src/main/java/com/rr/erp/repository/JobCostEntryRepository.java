package com.rr.erp.repository;

import com.rr.erp.entity.JobCostEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
public class JobCostEntryRepository {

    private final JdbcTemplate jdbcTemplate;

    public JobCostEntryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // POST
    public JobCostEntry createJobCostEntry(JobCostEntry jobCostEntry) {

        UUID jobCostEntryId = UUID.randomUUID();

        String sql = """
                INSERT INTO job_cost_entry (
                    job_cost_entry_id,
                    job_card_id,
                    description,
                    amount,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """;

        jdbcTemplate.update(
                sql,
                jobCostEntryId,
                jobCostEntry.getJobCardId(),
                jobCostEntry.getDescription(),
                jobCostEntry.getAmount()
        );

        jobCostEntry.setJobCostEntryId(jobCostEntryId);

        return jobCostEntry;
    }

    // PUT
    public int updateJobCostEntry(
            UUID jobCostEntryId,
            JobCostEntry jobCostEntry
    ) {

        String sql = """
                UPDATE job_cost_entry
                SET
                    job_card_id = ?,
                    description = ?,
                    amount = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE job_cost_entry_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                jobCostEntry.getJobCardId(),
                jobCostEntry.getDescription(),
                jobCostEntry.getAmount(),
                jobCostEntryId
        );
    }

    // GET BY JOB CARD ID
    public List<JobCostEntry> getJobCostEntriesByJobCardId(UUID jobCardId) {

        String sql = """
                SELECT
                    jce.job_cost_entry_id AS "jobCostEntryId",
                    jce.job_card_id AS "jobCardId",
                    jce.description AS "description",
                    jce.amount AS "amount",
                    jce.created_at AS "createdAt",
                    jce.updated_at AS "updatedAt"
                FROM job_cost_entry jce
                WHERE jce.job_card_id = ?
                ORDER BY jce.created_at DESC
                """;

        return jdbcTemplate.query(
                sql,
                this::mapJobCostEntry,
                jobCardId
        );
    }

    private JobCostEntry mapJobCostEntry(
            ResultSet rs,
            int rowNum
    ) throws SQLException {

        JobCostEntry jobCostEntry = new JobCostEntry();

        jobCostEntry.setJobCostEntryId(
                rs.getObject("jobCostEntryId", UUID.class)
        );

        jobCostEntry.setJobCardId(
                rs.getObject("jobCardId", UUID.class)
        );

        jobCostEntry.setDescription(
                rs.getString("description")
        );

        jobCostEntry.setAmount(
                rs.getBigDecimal("amount")
        );

        jobCostEntry.setCreatedAt(
                rs.getObject("createdAt", java.time.LocalDateTime.class)
        );

        jobCostEntry.setUpdatedAt(
                rs.getObject("updatedAt", java.time.LocalDateTime.class)
        );

        return jobCostEntry;
    }
}