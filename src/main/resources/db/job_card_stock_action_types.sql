-- Widens the stock_batch/stock_action_allocation pseudo-enum CHECK constraints
-- so job cards can issue stock (debiting the project store, action type
-- JOB_CARD_ISSUE) and receive returned items back (opening a new batch,
-- source type JOB_CARD_ITEM_RETURN), through the same FIFO engine GRN/GIN/
-- intra-project-issue use. Run manually, after alter_stock_enums_for_plant.sql.

ALTER TABLE stock_batch
    DROP CONSTRAINT IF EXISTS stock_batch_source_type_check;

ALTER TABLE stock_batch
    ADD CONSTRAINT stock_batch_source_type_check CHECK (
        source_type IN (
            'SUPPLIER_GRN', 'INTERNAL_GRN', 'STOCK_RETURN',
            'STOCK_ADJUSTMENT', 'INTRA_PROJECT_ISSUE_RETURN',
            'PLANT_PRODUCTION', 'JOB_CARD_ITEM_RETURN'
        )
    );

ALTER TABLE stock_action_allocation
    DROP CONSTRAINT IF EXISTS stock_action_allocation_action_type_check;

ALTER TABLE stock_action_allocation
    ADD CONSTRAINT stock_action_allocation_action_type_check CHECK (
        action_type IN (
            'GIN', 'INTRA_PROJECT_ISSUE', 'STOCK_ADJUSTMENT', 'STOCK_RETURN',
            'PLANT_PRODUCTION', 'JOB_CARD_ISSUE'
        )
    );
