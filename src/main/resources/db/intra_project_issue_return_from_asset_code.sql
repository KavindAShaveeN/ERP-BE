-- Adds an optional "from asset code" to intra_project_issue_return: the registered asset the
-- returned items are coming back from (the counterpart of intra_project_issue.for_asset_code).
--
-- If an earlier version of this change already added the column as for_asset_code, it is
-- renamed (along with its foreign key and index) instead of adding a second column.
--
-- Plain JDBC-backed table, not managed by Hibernate ddl-auto:update.
-- Run this script manually against the application database.
--
-- Safe to re-run: every step is guarded.

BEGIN;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'intra_project_issue_return' AND column_name = 'for_asset_code'
    ) THEN
        ALTER TABLE intra_project_issue_return RENAME COLUMN for_asset_code TO from_asset_code;
    END IF;

    IF EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'intra_project_issue_return_for_asset_code_fkey'
    ) THEN
        ALTER TABLE intra_project_issue_return
            RENAME CONSTRAINT intra_project_issue_return_for_asset_code_fkey
            TO intra_project_issue_return_from_asset_code_fkey;
    END IF;
END $$;

ALTER INDEX IF EXISTS idx_intra_project_issue_return_for_asset_code
    RENAME TO idx_intra_project_issue_return_from_asset_code;

ALTER TABLE intra_project_issue_return
    ADD COLUMN IF NOT EXISTS from_asset_code VARCHAR(20) NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'intra_project_issue_return_from_asset_code_fkey'
    ) THEN
        ALTER TABLE intra_project_issue_return
            ADD CONSTRAINT intra_project_issue_return_from_asset_code_fkey
            FOREIGN KEY (from_asset_code) REFERENCES asset(asset_code);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_intra_project_issue_return_from_asset_code
    ON intra_project_issue_return (from_asset_code)
    WHERE from_asset_code IS NOT NULL;

COMMIT;
