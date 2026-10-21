package com.rr.erp.repository;

import com.rr.erp.entity.JobCardOutput;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JobCardOutputRepository {

    private static final String SELECT = """
            SELECT
                job_card_output_id AS "jobCardOutputId",
                job_card_id AS "jobCardId",
                item_code AS "itemCode",
                description AS "description",
                quantity AS "quantity",
                unit_cost AS "unitCost",
                posted_at AS "postedAt",
                created_at AS "createdAt",
                updated_at AS "updatedAt"
            FROM job_card_output
            """;

    private final JdbcTemplate jdbcTemplate;

    public JobCardOutputRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public JobCardOutput insert(JobCardOutput output) {

        UUID id = UUID.randomUUID();

        jdbcTemplate.update(
                """
                INSERT INTO job_card_output (
                    job_card_output_id, job_card_id, item_code, description, quantity,
                    created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """,
                id,
                output.getJobCardId(),
                output.getItemCode(),
                output.getDescription(),
                output.getQuantity()
        );

        output.setJobCardOutputId(id);

        return output;
    }

    public int update(UUID jobCardOutputId, JobCardOutput output) {

        return jdbcTemplate.update(
                """
                UPDATE job_card_output
                SET item_code = ?, description = ?, quantity = ?, updated_at = CURRENT_TIMESTAMP
                WHERE job_card_output_id = ? AND posted_at IS NULL
                """,
                output.getItemCode(),
                output.getDescription(),
                output.getQuantity(),
                jobCardOutputId
        );
    }

    public int delete(UUID jobCardOutputId) {

        return jdbcTemplate.update(
                "DELETE FROM job_card_output WHERE job_card_output_id = ? AND posted_at IS NULL",
                jobCardOutputId
        );
    }

    public int markPosted(UUID jobCardOutputId, BigDecimal unitCost) {

        return jdbcTemplate.update(
                """
                UPDATE job_card_output
                SET unit_cost = ?, posted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                WHERE job_card_output_id = ?
                """,
                unitCost,
                jobCardOutputId
        );
    }

    public Optional<JobCardOutput> findById(UUID jobCardOutputId) {

        return jdbcTemplate.query(
                SELECT + " WHERE job_card_output_id = ?",
                this::map,
                jobCardOutputId
        ).stream().findFirst();
    }

    public List<JobCardOutput> getByJobCardId(UUID jobCardId) {

        return jdbcTemplate.query(
                SELECT + " WHERE job_card_id = ? ORDER BY created_at",
                this::map,
                jobCardId
        );
    }

    private JobCardOutput map(ResultSet rs, int rowNum) throws SQLException {

        JobCardOutput output = new JobCardOutput();

        output.setJobCardOutputId(rs.getObject("jobCardOutputId", UUID.class));
        output.setJobCardId(rs.getObject("jobCardId", UUID.class));
        output.setItemCode(rs.getString("itemCode"));
        output.setDescription(rs.getString("description"));
        output.setQuantity(rs.getBigDecimal("quantity"));
        output.setUnitCost(rs.getBigDecimal("unitCost"));
        output.setPostedAt(rs.getObject("postedAt", java.time.LocalDateTime.class));
        output.setCreatedAt(rs.getObject("createdAt", java.time.LocalDateTime.class));
        output.setUpdatedAt(rs.getObject("updatedAt", java.time.LocalDateTime.class));

        return output;
    }
}
