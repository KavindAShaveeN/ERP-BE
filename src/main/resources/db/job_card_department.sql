-- Adds a free-text department field to job cards (which department raised /
-- owns the job). Run manually against the application database (this codebase
-- has no Hibernate ddl-auto / Flyway — see the other scripts in this folder).

ALTER TABLE job_card
    ADD COLUMN IF NOT EXISTS department VARCHAR(80);
