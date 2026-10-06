-- Adds a per-batch expiry date for special items that expire. The expiry is recorded on the
-- GRN line at receipt (see grn_item_expiry_date.sql) and copied onto the batch at the moment a
-- NEW batch is opened from that line, so it travels with the lot through transfers, issues and
-- returns. Never changed once a batch exists (matches batch_code/origin_date/unit_cost/
-- description). Null for items that don't expire.
--
-- Plain JDBC-backed table (see stock_batch_schema.sql), not managed by Hibernate
-- ddl-auto:update. Run this script manually, after stock_batch_schema.sql and
-- grn_item_expiry_date.sql.

ALTER TABLE stock_batch
    ADD COLUMN IF NOT EXISTS expiry_date DATE;

-- Supports listing / alerting on batches nearing expiry.
CREATE INDEX IF NOT EXISTS idx_stock_batch_expiry_date
    ON stock_batch (expiry_date)
    WHERE expiry_date IS NOT NULL;

-- One-time backfill for supplier-GRN batches opened before this column existed.
-- stock_batch.source_id is the grn_id (not the grn_item_id), so a batch is matched to its
-- GRN line by grn_id + item_code. Only filled where that GRN has exactly one distinct expiry
-- date for the item; ambiguous cases (same item on several lines with different expiries)
-- are left null rather than guessed.
UPDATE stock_batch b
SET expiry_date = x.expiry_date
FROM (
    SELECT gi.grn_id, gi.item_code, MIN(gi.expiry_date) AS expiry_date
    FROM grn_item gi
    WHERE gi.expiry_date IS NOT NULL
    GROUP BY gi.grn_id, gi.item_code
    HAVING COUNT(DISTINCT gi.expiry_date) = 1
) x
WHERE b.source_type = 'SUPPLIER_GRN'
  AND b.source_id = x.grn_id
  AND b.item_code = x.item_code
  AND b.expiry_date IS NULL;
