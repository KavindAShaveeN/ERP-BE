package com.rr.erp.repository;

import com.rr.erp.entity.Defect;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class DefectRepository {

    private final JdbcTemplate jdbcTemplate;

    public DefectRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    // =========================================================
    // POST - ADD MULTIPLE DEFECTS
    // =========================================================

    public void createDefects(List<Defect> defects) {

        String sql = """
                INSERT INTO defect (
                    defect_id,
                    job_card_id,
                    defect_description
                )
                VALUES (?, ?, ?)
                """;

        for (Defect defect : defects) {

            UUID defectId = defect.getDefectId() != null
                    ? defect.getDefectId()
                    : UUID.randomUUID();

            jdbcTemplate.update(
                    sql,
                    defectId,
                    defect.getJobCardId(),
                    defect.getDefectDescription()
            );
        }
    }


    // =========================================================
    // PUT - UPDATE DEFECT
    // =========================================================

    public int updateDefect(UUID defectId, Defect defect) {

        String sql = """
                UPDATE defect
                SET
                    job_card_id = ?,
                    defect_description = ?
                WHERE defect_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                defect.getJobCardId(),
                defect.getDefectDescription(),
                defectId
        );
    }

    // =========================================================
    // DELETE - REMOVE DEFECT
    // =========================================================

    public int deleteDefect(UUID defectId) {

        String sql = """
                DELETE FROM defect
                WHERE defect_id = ?
                """;

        return jdbcTemplate.update(sql, defectId);
    }

    public List<Defect> getDefectsByJobCardId(UUID jobCardId) {

        String sql = """
            SELECT
                defect_id AS "defectId",
                job_card_id AS "jobCardId",
                defect_description AS "defectDescription",
                outcome,
                EXISTS(SELECT 1 FROM job_defect_source s WHERE s.defect_id=defect.defect_id) AS reported_fault
            FROM defect
            WHERE job_card_id = ?
              -- Rows created from reported faults are shown as reported work, not as inspection findings.
              AND NOT EXISTS(SELECT 1 FROM job_defect_source s WHERE s.defect_id=defect.defect_id)
            ORDER BY defect_id
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Defect defect = new Defect();

                    defect.setDefectId(
                            rs.getObject("defectId", UUID.class)
                    );

                    defect.setJobCardId(
                            rs.getObject("jobCardId", UUID.class)
                    );

                    defect.setDefectDescription(
                            rs.getString("defectDescription")
                    );

                    defect.setReportedFault(rs.getBoolean("reported_fault"));
                    defect.setOutcome(rs.getString("outcome"));
                    return defect;
                },
                jobCardId
        );
    }
}