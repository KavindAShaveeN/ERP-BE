-- job_check_list table: one row per job card holding the 12-item vehicle/machine
-- checklist (checked flag + remark per item), upserted from the Job Card Checklist
-- page. Plain JDBC-backed table (matching the job_card pattern in this codebase),
-- so it is NOT created automatically by Hibernate's ddl-auto:update. Run this
-- script manually against the application database.

CREATE TABLE IF NOT EXISTS job_check_list (
    job_card_id UUID PRIMARY KEY REFERENCES job_card (job_card_id) ON DELETE CASCADE,
    q1  BOOLEAN, q2  BOOLEAN, q3  BOOLEAN, q4  BOOLEAN,
    q5  BOOLEAN, q6  BOOLEAN, q7  BOOLEAN, q8  BOOLEAN,
    q9  BOOLEAN, q10 BOOLEAN, q11 BOOLEAN, q12 BOOLEAN,
    q1_remark  VARCHAR(200), q2_remark  VARCHAR(200), q3_remark  VARCHAR(200), q4_remark  VARCHAR(200),
    q5_remark  VARCHAR(200), q6_remark  VARCHAR(200), q7_remark  VARCHAR(200), q8_remark  VARCHAR(200),
    q9_remark  VARCHAR(200), q10_remark VARCHAR(200), q11_remark VARCHAR(200), q12_remark VARCHAR(200)
);
