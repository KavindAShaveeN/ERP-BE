-- Widens the stock_batch/stock_action_allocation pseudo-enum CHECK constraints
-- to allow the Plant production module to open finished-goods batches and
-- consume raw-material batches through the same FIFO engine GRN/GIN use.
-- Run manually, after plant_schema.sql, following the precedent set by the
-- original alter_stock_batch_source_type_widen.sql.

ALTER TABLE stock_batch
    DROP CONSTRAINT IF EXISTS stock_batch_source_type_check;

ALTER TABLE stock_batch
    ADD CONSTRAINT stock_batch_source_type_check CHECK (
        source_type IN (
            'SUPPLIER_GRN', 'INTERNAL_GRN', 'STOCK_RETURN',
            'STOCK_ADJUSTMENT', 'INTRA_PROJECT_ISSUE_RETURN',
            'PLANT_PRODUCTION'
        )
    );

ALTER TABLE stock_action_allocation
    DROP CONSTRAINT IF EXISTS stock_action_allocation_action_type_check;

ALTER TABLE stock_action_allocation
    ADD CONSTRAINT stock_action_allocation_action_type_check CHECK (
        action_type IN (
            'GIN', 'INTRA_PROJECT_ISSUE', 'STOCK_ADJUSTMENT', 'STOCK_RETURN',
            'PLANT_PRODUCTION'
        )
    );
