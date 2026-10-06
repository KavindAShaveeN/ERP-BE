-- Adds a "JOB_CARD" intra project issue/return type: workshop projects can issue
-- (and later return) project store items directly against a job card, instead of
-- using the job card's own direct issue/return actions. job_card_id identifies
-- which job card the issue/return belongs to (NULL for every other issue/return
-- type). Run manually against the application database (this codebase has no
-- Hibernate ddl-auto / Flyway — see the other scripts in this folder).

ALTER TABLE intra_project_issue
    ADD COLUMN IF NOT EXISTS job_card_id UUID NULL REFERENCES job_card (job_card_id);

ALTER TABLE intra_project_issue_return
    ADD COLUMN IF NOT EXISTS job_card_id UUID NULL REFERENCES job_card (job_card_id);

CREATE INDEX IF NOT EXISTS idx_intra_project_issue_job_card_id ON intra_project_issue (job_card_id);
CREATE INDEX IF NOT EXISTS idx_intra_project_issue_return_job_card_id ON intra_project_issue_return (job_card_id);

-- Both type CHECK constraints were created before JOB_CARD existed (see
-- intra_project_issue_extended_types.sql / intra_project_issue_return_multi_source.sql)
-- and only got created "if not exists", so they were never widened. Drop and recreate them.
ALTER TABLE intra_project_issue DROP CONSTRAINT IF EXISTS intra_project_issue_type_check;
ALTER TABLE intra_project_issue
    ADD CONSTRAINT intra_project_issue_type_check
    CHECK (issue_type IN ('GENERAL', 'SUBCONTRACTOR', 'PERSONAL', 'LOAN', 'JOB_CARD'));

ALTER TABLE intra_project_issue_return DROP CONSTRAINT IF EXISTS intra_project_issue_return_type_check;
ALTER TABLE intra_project_issue_return
    ADD CONSTRAINT intra_project_issue_return_type_check
    CHECK (return_type IN ('GENERAL', 'SUBCONTRACTOR', 'PERSONAL', 'LOAN', 'JOB_CARD'));
