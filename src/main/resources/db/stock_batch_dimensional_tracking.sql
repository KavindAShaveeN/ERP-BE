-- Adds piece/dimension tracking to the batch ledger for items whose stock is
-- physically counted in bars/pieces but consumed by cutting to length (e.g. sheet
-- piles, steel beams). Dimensional-item detection is NOT stored in the DB: the
-- backend recognises these items by a hardcoded category-code list (e.g.
-- 'STBEAMS'). Non-dimensional items simply leave the new columns NULL and behave
-- exactly as before.
--
-- length_value / width_value / piece_count on stock_batch describe the size and
-- bar/piece count the batch was created with:
--   - at GRN time: the supplier-received size (e.g. 6m bars, piece_count = 10)
--   - at cut-return time: a NEW size born from cutting (e.g. 1m pieces, piece_count = 4)
-- width_value is only populated for area-tracked items (length AND width both
-- present => area = length * width); length-only items leave width_value NULL.
--
-- piece_count_remaining on stock_batch_location tracks how many whole pieces of
-- that exact size are still available at a given project store, alongside the
-- existing qty_remaining (which continues to track remaining length/area total).
-- Project store's per-size breakdown (e.g. "6m x 8, 1m x 4, 2m x 1") is a query
-- grouping stock_batch_location by (item_code, length_value, width_value) via its
-- stock_batch — no change needed to project_store itself.
--
-- Plain JDBC-backed tables (see stock_batch_schema.sql), not managed by Hibernate
-- ddl-auto:update. Run this script manually, after stock_batch_schema.sql.

ALTER TABLE stock_batch
    ADD COLUMN IF NOT EXISTS length_value  NUMERIC(10,3),
    ADD COLUMN IF NOT EXISTS width_value   NUMERIC(10,3),
    ADD COLUMN IF NOT EXISTS piece_count   INTEGER;

ALTER TABLE stock_batch
    ADD CONSTRAINT stock_batch_piece_count_check CHECK (piece_count IS NULL OR piece_count > 0);

ALTER TABLE stock_batch
    ADD CONSTRAINT stock_batch_width_requires_length_check CHECK (width_value IS NULL OR length_value IS NOT NULL);

ALTER TABLE stock_batch_location
    ADD COLUMN IF NOT EXISTS piece_count_remaining INTEGER;

ALTER TABLE stock_batch_location
    ADD CONSTRAINT stock_batch_location_piece_count_remaining_check CHECK (piece_count_remaining IS NULL OR piece_count_remaining >= 0);

-- New source type: pieces produced by cutting an issued batch and returning the
-- offcuts to the project store (as opposed to INTRA_PROJECT_ISSUE_RETURN, which
-- returns unused stock of the SAME size it was issued as).
ALTER TABLE stock_batch
    DROP CONSTRAINT IF EXISTS stock_batch_source_type_check;

ALTER TABLE stock_batch
    ADD CONSTRAINT stock_batch_source_type_check CHECK (
        source_type IN (
            'SUPPLIER_GRN', 'INTERNAL_GRN', 'STOCK_RETURN',
            'STOCK_ADJUSTMENT', 'INTRA_PROJECT_ISSUE_RETURN',
            'PLANT_PRODUCTION', 'CUT_RETURN'
        )
    );

-- Records a single cut-return transformation: the pieces of a source batch that
-- were consumed by cutting, and the resulting new batch (a distinct new size)
-- created from that cut. One cut-return event with multiple resulting sizes
-- (e.g. 1m x4 AND 2m x1 from the same source bars) produces one row per
-- resulting size, all sharing the same intra_project_issue_return_id +
-- source_stock_batch_id, so the full transformation can be reassembled for audit.
CREATE TABLE IF NOT EXISTS stock_batch_split (
    stock_batch_split_id         UUID           PRIMARY KEY,
    intra_project_issue_return_id UUID          NOT NULL REFERENCES intra_project_issue_return (intra_project_issue_return_id) ON DELETE RESTRICT,
    source_stock_batch_id        UUID           NOT NULL REFERENCES stock_batch (stock_batch_id) ON DELETE RESTRICT,
    source_piece_count_consumed  INTEGER        NOT NULL CHECK (source_piece_count_consumed > 0),
    source_qty_consumed          NUMERIC(14,3)  NOT NULL CHECK (source_qty_consumed > 0),
    result_stock_batch_id        UUID           REFERENCES stock_batch (stock_batch_id) ON DELETE RESTRICT,
    result_qty_produced          NUMERIC(14,3)  NOT NULL DEFAULT 0 CHECK (result_qty_produced >= 0),
    wastage_qty                  NUMERIC(14,3)  NOT NULL DEFAULT 0 CHECK (wastage_qty >= 0)
);
CREATE INDEX IF NOT EXISTS idx_stock_batch_split_return
    ON stock_batch_split (intra_project_issue_return_id);
CREATE INDEX IF NOT EXISTS idx_stock_batch_split_source_batch
    ON stock_batch_split (source_stock_batch_id);
