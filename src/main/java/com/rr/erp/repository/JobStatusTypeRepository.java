package com.rr.erp.repository;

import com.rr.erp.entity.JobStatusType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JobStatusTypeRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<JobStatusType> getAllJobStatusTypes() {

        String sql = """
                SELECT
                    job_status_type_id AS "jobStatusTypeId",
                    job_status_type_name AS "jobStatusTypeName"
                FROM job_status_type
                ORDER BY job_status_type_id
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    JobStatusType jobStatusType = new JobStatusType();

                    jobStatusType.setJobStatusTypeId(
                            rs.getInt("jobStatusTypeId")
                    );

                    jobStatusType.setJobStatusTypeName(
                            rs.getString("jobStatusTypeName")
                    );

                    return jobStatusType;
                }
        );
    }
}