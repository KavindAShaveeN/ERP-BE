-- Asset lines on intra project issues and their returns (site stores). Run once manually against the
-- ERPRR database (no Flyway). Safe to re-run.
--
-- An asset line issues / returns ONE registered asset (quantity 1). It does not touch stock and does not
-- write asset_location history - the issue and its return are the only record. item_code is null on those
-- lines, so it must be nullable.

ALTER TABLE intra_project_issue_item        ALTER COLUMN item_code DROP NOT NULL;
ALTER TABLE intra_project_issue_return_item ALTER COLUMN item_code DROP NOT NULL;

ALTER TABLE intra_project_issue_return_item ADD COLUMN IF NOT EXISTS asset_code VARCHAR(50);

CREATE INDEX IF NOT EXISTS idx_intra_project_issue_return_item_asset_code
    ON intra_project_issue_return_item (asset_code) WHERE asset_code IS NOT NULL;
