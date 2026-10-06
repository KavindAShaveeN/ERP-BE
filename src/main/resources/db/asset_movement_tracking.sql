-- Asset movement tracking through the material-management documents (GIN / GRN / intra-project issue).
-- Run this once manually against the ERPRR database (there is no Flyway; Hibernate does not create these).
--
-- Safe to re-run: every statement is guarded (IF NOT EXISTS).
--
-- asset_code on a document line: when set, the line moves that ONE registered asset (quantity is 1) and
--   is kept out of the quantity stock engine (project_store / stock_batch). The asset's location history
--   is the source of truth for where it is. Lines with no asset_code behave exactly as before.
--
-- asset_location additions: record why/what caused each location change.
--   movement_type   DISPATCH (GIN authorized, asset now IN_TRANSIT) | RECEIPT (GRN approved at destination)
--                   | ISSUE (intra-project issue to a person/subcontractor/loan) | RETURN | MANUAL
--   source_doc_type GIN | GRN | INTRA_PROJECT_ISSUE | INTRA_PROJECT_ISSUE_RETURN
--   source_doc_id   the id of that document
--   from_location   the location the asset left (project code)
-- new_location always stays a valid project code (it is a foreign key to project). While an asset is
-- between a dispatch and its receipt its latest asset_location entry has movement_type DISPATCH
-- (from_location = issuing project, new_location = receiving project), which marks it IN TRANSIT: it is
-- not "at" any project until the GRN is approved and writes a RECEIPT entry.

ALTER TABLE asset_location
    ADD COLUMN IF NOT EXISTS from_location   VARCHAR(150),
    ADD COLUMN IF NOT EXISTS movement_type   VARCHAR(30),
    ADD COLUMN IF NOT EXISTS source_doc_type VARCHAR(40),
    ADD COLUMN IF NOT EXISTS source_doc_id   UUID;

ALTER TABLE gin_item
    ADD COLUMN IF NOT EXISTS asset_code VARCHAR(50);

ALTER TABLE grn_item
    ADD COLUMN IF NOT EXISTS asset_code VARCHAR(50);

ALTER TABLE intra_project_issue_item
    ADD COLUMN IF NOT EXISTS asset_code VARCHAR(50);

CREATE INDEX IF NOT EXISTS idx_gin_item_asset_code                ON gin_item (asset_code)                WHERE asset_code IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_grn_item_asset_code                ON grn_item (asset_code)                WHERE asset_code IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_intra_project_issue_item_asset_code ON intra_project_issue_item (asset_code) WHERE asset_code IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_asset_location_source_doc          ON asset_location (source_doc_type, source_doc_id);
