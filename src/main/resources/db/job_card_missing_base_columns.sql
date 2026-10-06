-- This database's job_card table was missing several columns the
-- application has always expected (JobCard entity / JobCardRepository).
-- Backfills them so the existing SELECT/INSERT/UPDATE queries work.
-- Run manually against the application database (this codebase has no
-- Hibernate ddl-auto / Flyway — see the other scripts in this folder).

ALTER TABLE job_card
    ADD COLUMN IF NOT EXISTS project_code VARCHAR(60),
    ADD COLUMN IF NOT EXISTS asset_code VARCHAR(60),
    ADD COLUMN IF NOT EXISTS make VARCHAR(100),
    ADD COLUMN IF NOT EXISTS type VARCHAR(100),
    ADD COLUMN IF NOT EXISTS meter_reading NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS informed_by VARCHAR(120),
    ADD COLUMN IF NOT EXISTS phone_number VARCHAR(30),
    ADD COLUMN IF NOT EXISTS is_delivered BOOLEAN NOT NULL DEFAULT FALSE;
