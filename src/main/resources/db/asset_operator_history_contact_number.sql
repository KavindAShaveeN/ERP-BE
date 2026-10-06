-- A per-entry contact number for the operator on an asset_operator_history row — defaults to the
-- operator employee's own contact number when the entry is created, but can be corrected
-- afterwards (e.g. the operator was reachable on a different number during that assignment)
-- without touching the employee record itself. Editable, along with remarks, only on the latest
-- entry for an asset (enforced by the frontend, same rule as deleting an entry).
--
-- Run manually against the application database (this codebase has no Hibernate ddl-auto / Flyway).
-- Safe to re-run: guarded with IF NOT EXISTS.

ALTER TABLE asset_operator_history
    ADD COLUMN IF NOT EXISTS contact_number VARCHAR(30);
