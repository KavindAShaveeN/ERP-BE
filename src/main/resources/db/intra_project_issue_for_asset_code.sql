-- Adds an optional "for asset code" to intra_project_issue: the registered asset the issued
-- items are for (e.g. spare parts fitted to a vehicle). Different from
-- intra_project_issue_item.asset_code, which hands out the asset itself.
--
-- Plain JDBC-backed table, not managed by Hibernate ddl-auto:update.
-- Run this script manually against the application database.
--
-- Safe to re-run: every step is guarded.

BEGIN;

ALTER TABLE intra_project_issue
    ADD COLUMN IF NOT EXISTS for_asset_code VARCHAR(20) NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'intra_project_issue_for_asset_code_fkey'
    ) THEN
        ALTER TABLE intra_project_issue
            ADD CONSTRAINT intra_project_issue_for_asset_code_fkey
            FOREIGN KEY (for_asset_code) REFERENCES asset(asset_code);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_intra_project_issue_for_asset_code
    ON intra_project_issue (for_asset_code)
    WHERE for_asset_code IS NOT NULL;

COMMIT;
