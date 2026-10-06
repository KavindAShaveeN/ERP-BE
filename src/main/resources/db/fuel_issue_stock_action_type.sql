-- StockBatchService.ACTION_FUEL_ISSUE ("FUEL_ISSUE") is used by FuelIssueService to debit
-- the project store's fuel stock via the same FIFO allocation engine as GIN/job-card issues,
-- but the stock_action_allocation_action_type_check constraint was never widened for it,
-- so every fuel issue failed with "violates check constraint
-- stock_action_allocation_action_type_check" even though stock was available.
-- Run manually, after job_card_stock_action_types.sql.

ALTER TABLE stock_action_allocation
    DROP CONSTRAINT IF EXISTS stock_action_allocation_action_type_check;

ALTER TABLE stock_action_allocation
    ADD CONSTRAINT stock_action_allocation_action_type_check CHECK (
        action_type IN (
            'GIN', 'INTRA_PROJECT_ISSUE', 'STOCK_ADJUSTMENT', 'STOCK_RETURN',
            'PLANT_PRODUCTION', 'JOB_CARD_ISSUE', 'FUEL_ISSUE'
        )
    );
