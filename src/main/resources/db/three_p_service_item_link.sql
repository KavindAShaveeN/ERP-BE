-- Lets a third-party service issue reference a real stock item or registered asset instead of
-- only a free-text item name, mirroring how a GIN line references item_code/asset_code.
-- Purely a job-card-scoped record still: no FIFO stock deduction or asset location-history
-- entry is created from this table, unlike GIN/GRN.
--
-- asset_code: set when the despatched item is a registered asset (asset_code.asset_code_code).
-- item_code: set when the despatched item is a stock item (item_code.item_code). Mutually
--   exclusive with asset_code in practice, both nullable for free-text/legacy rows.
-- quantity: defaults to 1 (registered assets are always quantity 1); stock items may be > 1.

ALTER TABLE three_p_service
    ADD COLUMN asset_code VARCHAR(50),
    ADD COLUMN item_code VARCHAR(50),
    ADD COLUMN quantity NUMERIC(12, 2) NOT NULL DEFAULT 1;
