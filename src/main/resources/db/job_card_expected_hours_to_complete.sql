-- Adds a job-card level field for the total hours the assigned labours are
-- expected to take to complete the job. Run manually against the application
-- database (this codebase has no Hibernate ddl-auto / Flyway — see the other
-- scripts in this folder).

ALTER TABLE job_card
    ADD COLUMN IF NOT EXISTS expected_hours_to_complete NUMERIC(12, 2);
