-- Asset lines on Stock Return and Stock Adjustment (same idea as asset_movement_tracking.sql for GIN / GRN).
-- Run once manually against the ERPRR database (no Flyway). Safe to re-run.
--
-- asset_code on a line: the line moves / changes ONE registered asset (quantity 1) and is kept out of the
--   quantity stock engine. item_code is null on those lines, so it must be nullable.
--
--   Stock Return (INTERNAL)  approved -> asset DISPATCHed to the receiving project (IN_TRANSIT until its GRN)
--   Stock Return (SUPPLIER)  approved -> asset.status = 'Inactive', location unchanged
--   Stock Adjustment  -1     approved -> ADJUSTMENT entry, asset.status = 'Disposed'
--   Stock Adjustment  +1     approved -> ADJUSTMENT entry at the project, asset.status = 'Active'
--
-- asset_location.movement_type gains ADJUSTMENT; source_doc_type gains STOCK_RETURN / STOCK_ADJUSTMENT.

ALTER TABLE stock_return_item     ADD COLUMN IF NOT EXISTS asset_code VARCHAR(50);
ALTER TABLE stock_adjustment_item ADD COLUMN IF NOT EXISTS asset_code VARCHAR(50);

ALTER TABLE stock_return_item     ALTER COLUMN item_code DROP NOT NULL;
ALTER TABLE stock_adjustment_item ALTER COLUMN item_code DROP NOT NULL;

CREATE INDEX IF NOT EXISTS idx_stock_return_item_asset_code     ON stock_return_item (asset_code)     WHERE asset_code IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_stock_adjustment_item_asset_code ON stock_adjustment_item (asset_code) WHERE asset_code IS NOT NULL;
