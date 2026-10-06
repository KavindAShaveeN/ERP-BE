-- Adds a manually-entered "hours worked" figure to each job_worker row (one
-- editable hours value per employee assigned to a job card). Used to total
-- labour hours per job card and to drive a labour-hours report. Run manually
-- against the application database (this codebase has no Hibernate ddl-auto /
-- Flyway — see the other scripts in this folder).

ALTER TABLE job_worker
    ADD COLUMN IF NOT EXISTS hours_worked NUMERIC(7, 2) NOT NULL DEFAULT 0;
