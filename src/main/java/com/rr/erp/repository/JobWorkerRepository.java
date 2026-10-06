package com.rr.erp.repository;

import com.rr.erp.dto.JobWorkerResponse;
import com.rr.erp.entity.JobWorker;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public class JobWorkerRepository {

    private final JdbcTemplate jdbcTemplate;

    public JobWorkerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // =========================================================
    // POST - CREATE JOB WORKER
    // =========================================================

    public int createJobWorker(JobWorker jobWorker) {

        String sql = """
                INSERT INTO job_worker (
                    job_worker_id,
                    job_card_id,
                    employee_code,
                    hours_worked
                )
                VALUES (?, ?, ?, ?)
                """;

        UUID jobWorkerId = jobWorker.getJobWorkerId() != null
                ? jobWorker.getJobWorkerId()
                : UUID.randomUUID();

        jobWorker.setJobWorkerId(jobWorkerId);

        return jdbcTemplate.update(
                sql,
                jobWorkerId,
                jobWorker.getJobCardId(),
                jobWorker.getEmployeeCode(),
                jobWorker.getHoursWorked() != null
                        ? jobWorker.getHoursWorked()
                        : BigDecimal.ZERO
        );
    }

    // =========================================================
    // PUT - UPDATE JOB WORKER
    // =========================================================

    public int updateJobWorker(
            UUID jobWorkerId,
            JobWorker jobWorker
    ) {

        String sql = """
                UPDATE job_worker
                SET
                    job_card_id = ?,
                    employee_code = ?,
                    hours_worked = ?
                WHERE job_worker_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                jobWorker.getJobCardId(),
                jobWorker.getEmployeeCode(),
                jobWorker.getHoursWorked() != null
                        ? jobWorker.getHoursWorked()
                        : BigDecimal.ZERO,
                jobWorkerId
        );
    }
    // =========================================================
    // DELETE - REMOVE JOB WORKER
    // =========================================================

    public int deleteJobWorker(UUID jobWorkerId) {

        String sql = """
                DELETE FROM job_worker
                WHERE job_worker_id = ?
                """;

        return jdbcTemplate.update(sql, jobWorkerId);
    }

    public List<JobWorkerResponse> getJobWorkersByJobCardId(UUID jobCardId) {

        String sql = """
            SELECT
                jw.job_worker_id AS "jobWorkerId",
                jw.job_card_id AS "jobCardId",
                jw.employee_code AS "employeeCode",
                jw.hours_worked AS "hoursWorked",
                e.full_name AS "fullName",
                d.designationName AS "designationName"
            FROM job_worker jw
            LEFT JOIN employee e
                ON jw.employee_code = e.employee_code
            LEFT JOIN designation d
                ON d.designationId = e.designationId
            WHERE jw.job_card_id = ?
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    JobWorkerResponse jobWorker = new JobWorkerResponse();

                    jobWorker.setJobWorkerId(
                            rs.getObject("jobWorkerId", UUID.class)
                    );

                    jobWorker.setJobCardId(
                            rs.getObject("jobCardId", UUID.class)
                    );

                    jobWorker.setEmployeeCode(
                            rs.getString("employeeCode")
                    );

                    jobWorker.setHoursWorked(
                            rs.getBigDecimal("hoursWorked")
                    );

                    jobWorker.setFullName(
                            rs.getString("fullName")
                    );

                    jobWorker.setDesignationName(
                            rs.getString("designationName")
                    );

                    return jobWorker;
                },
                jobCardId
        );
    }
}