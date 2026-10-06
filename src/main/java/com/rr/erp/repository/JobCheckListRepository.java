package com.rr.erp.repository;

import com.rr.erp.entity.JobCheckList;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JobCheckListRepository {

    private final JdbcTemplate jdbcTemplate;

    // =========================================================
    // INSERT OR UPDATE CHECKLIST
    // =========================================================
    public int upsertJobCheckList(
            UUID jobCardId,
            JobCheckList jobCheckList) {

        String sql = """
                INSERT INTO job_check_list (
                    job_card_id,
                    q1,
                    q2,
                    q3,
                    q4,
                    q5,
                    q6,
                    q7,
                    q8,
                    q9,
                    q10,
                    q11,
                    q12,
                    q1_remark,
                    q2_remark,
                    q3_remark,
                    q4_remark,
                    q5_remark,
                    q6_remark,
                    q7_remark,
                    q8_remark,
                    q9_remark,
                    q10_remark,
                    q11_remark,
                    q12_remark
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                )
                ON CONFLICT (job_card_id)
                DO UPDATE SET
                    q1 = EXCLUDED.q1,
                    q2 = EXCLUDED.q2,
                    q3 = EXCLUDED.q3,
                    q4 = EXCLUDED.q4,
                    q5 = EXCLUDED.q5,
                    q6 = EXCLUDED.q6,
                    q7 = EXCLUDED.q7,
                    q8 = EXCLUDED.q8,
                    q9 = EXCLUDED.q9,
                    q10 = EXCLUDED.q10,
                    q11 = EXCLUDED.q11,
                    q12 = EXCLUDED.q12,
                    q1_remark = EXCLUDED.q1_remark,
                    q2_remark = EXCLUDED.q2_remark,
                    q3_remark = EXCLUDED.q3_remark,
                    q4_remark = EXCLUDED.q4_remark,
                    q5_remark = EXCLUDED.q5_remark,
                    q6_remark = EXCLUDED.q6_remark,
                    q7_remark = EXCLUDED.q7_remark,
                    q8_remark = EXCLUDED.q8_remark,
                    q9_remark = EXCLUDED.q9_remark,
                    q10_remark = EXCLUDED.q10_remark,
                    q11_remark = EXCLUDED.q11_remark,
                    q12_remark = EXCLUDED.q12_remark
                """;

        return jdbcTemplate.update(
                sql,
                jobCardId,

                jobCheckList.getQ1(),
                jobCheckList.getQ2(),
                jobCheckList.getQ3(),
                jobCheckList.getQ4(),
                jobCheckList.getQ5(),
                jobCheckList.getQ6(),
                jobCheckList.getQ7(),
                jobCheckList.getQ8(),
                jobCheckList.getQ9(),
                jobCheckList.getQ10(),
                jobCheckList.getQ11(),
                jobCheckList.getQ12(),

                jobCheckList.getQ1Remark(),
                jobCheckList.getQ2Remark(),
                jobCheckList.getQ3Remark(),
                jobCheckList.getQ4Remark(),
                jobCheckList.getQ5Remark(),
                jobCheckList.getQ6Remark(),
                jobCheckList.getQ7Remark(),
                jobCheckList.getQ8Remark(),
                jobCheckList.getQ9Remark(),
                jobCheckList.getQ10Remark(),
                jobCheckList.getQ11Remark(),
                jobCheckList.getQ12Remark()
        );
    }


    // =========================================================
    // GET CHECKLIST BY JOB CARD ID
    // =========================================================
    public Optional<JobCheckList> getJobCheckListByJobCardId(
            UUID jobCardId) {

        String sql = """
                SELECT
                    job_card_id AS "jobCardId",
                    q1 AS "q1",
                    q2 AS "q2",
                    q3 AS "q3",
                    q4 AS "q4",
                    q5 AS "q5",
                    q6 AS "q6",
                    q7 AS "q7",
                    q8 AS "q8",
                    q9 AS "q9",
                    q10 AS "q10",
                    q11 AS "q11",
                    q12 AS "q12",
                    q1_remark AS "q1Remark",
                    q2_remark AS "q2Remark",
                    q3_remark AS "q3Remark",
                    q4_remark AS "q4Remark",
                    q5_remark AS "q5Remark",
                    q6_remark AS "q6Remark",
                    q7_remark AS "q7Remark",
                    q8_remark AS "q8Remark",
                    q9_remark AS "q9Remark",
                    q10_remark AS "q10Remark",
                    q11_remark AS "q11Remark",
                    q12_remark AS "q12Remark"
                FROM job_check_list
                WHERE job_card_id = ?
                """;

        List<JobCheckList> result = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapJobCheckList(rs),
                jobCardId
        );

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(result.get(0));
    }


    // =========================================================
    // MAPPER
    // =========================================================
    private JobCheckList mapJobCheckList(ResultSet rs)
            throws SQLException {

        JobCheckList checkList = new JobCheckList();

        checkList.setJobCardId(
                rs.getObject("jobCardId", UUID.class)
        );

        checkList.setQ1(rs.getObject("q1", Boolean.class));
        checkList.setQ2(rs.getObject("q2", Boolean.class));
        checkList.setQ3(rs.getObject("q3", Boolean.class));
        checkList.setQ4(rs.getObject("q4", Boolean.class));
        checkList.setQ5(rs.getObject("q5", Boolean.class));
        checkList.setQ6(rs.getObject("q6", Boolean.class));
        checkList.setQ7(rs.getObject("q7", Boolean.class));
        checkList.setQ8(rs.getObject("q8", Boolean.class));
        checkList.setQ9(rs.getObject("q9", Boolean.class));
        checkList.setQ10(rs.getObject("q10", Boolean.class));
        checkList.setQ11(rs.getObject("q11", Boolean.class));
        checkList.setQ12(rs.getObject("q12", Boolean.class));

        checkList.setQ1Remark(rs.getString("q1Remark"));
        checkList.setQ2Remark(rs.getString("q2Remark"));
        checkList.setQ3Remark(rs.getString("q3Remark"));
        checkList.setQ4Remark(rs.getString("q4Remark"));
        checkList.setQ5Remark(rs.getString("q5Remark"));
        checkList.setQ6Remark(rs.getString("q6Remark"));
        checkList.setQ7Remark(rs.getString("q7Remark"));
        checkList.setQ8Remark(rs.getString("q8Remark"));
        checkList.setQ9Remark(rs.getString("q9Remark"));
        checkList.setQ10Remark(rs.getString("q10Remark"));
        checkList.setQ11Remark(rs.getString("q11Remark"));
        checkList.setQ12Remark(rs.getString("q12Remark"));

        return checkList;
    }
}