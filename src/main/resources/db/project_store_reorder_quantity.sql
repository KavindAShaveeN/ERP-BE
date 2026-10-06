-- Adds a per-project-store reorder quantity (how much to order once stock
-- falls to the reorder level), alongside the existing reorder_level column.
--
-- Plain JDBC-backed table, not managed by Hibernate ddl-auto:update.
-- Run this script manually against the application database.
--
-- Safe to re-run: guarded with IF NOT EXISTS.

BEGIN;

ALTER TABLE project_store
    ADD COLUMN IF NOT EXISTS reorder_quantity NUMERIC(12,2) NOT NULL DEFAULT 0;

COMMIT;
