-- FIFO batch costing for project_store, with batch identity independent of location.
-- Plain JDBC-backed tables (matching the code_master_*/GRN/GIN pattern in this
-- codebase), so they are NOT created automatically by Hibernate's ddl-auto:update.
-- Run this script manually against the application database before using the
-- /api/stock-batches/** endpoints or any GRN/GIN/intra-project-issue/stock-adjustment/
-- stock-return flow after this change is deployed.
--
-- stock_batch: a lot's permanent identity. Created ONLY when stock genuinely enters
-- the system with no prior batch to attribute it to — a supplier GRN line (bought
-- from outside) or a positive stock adjustment (found stock nobody can trace). Its
-- batch_code, origin_date and unit_cost never change once created. It is NOT scoped
-- to a project — a lot can have quantity sitting in several projects at once.
--
-- stock_batch_location: how much of a given lot currently sits in a given project's
-- store. Internal transfers and stock returns move quantity between location rows
-- of the SAME batch (crediting the exact batch(es) a FIFO debit drew from) — they
-- never spawn a new stock_batch row, so a lot's identity survives moving between
-- project stores unchanged.
--
-- stock_batch_sequence: a per-(item, day) counter used only to generate batch_code
-- (see StockBatchService.buildBatchCode) — e.g. CEM001-20260904-01, -02 for a second
-- supplier delivery of the same item on the same day.
--
-- stock_action_allocation: one row per (batch, action) pair recording how much of
-- a stock-reducing action — a GIN, an intra-project issue, a negative stock
-- adjustment, or the "from" side of a stock return — was drawn from that batch,
-- and at what cost. Named "action" rather than "issue" because a GIN (Goods
-- Issued Note) is only one of the four action types this table covers.

CREATE TABLE IF NOT EXISTS stock_batch (
    stock_batch_id    UUID           PRIMARY KEY,
    batch_code        VARCHAR(40)    NOT NULL,
    item_code         VARCHAR(50)    NOT NULL REFERENCES item_code (code),
    origin_date       DATE           NOT NULL,
    source_type       VARCHAR(40)    NOT NULL,
    source_id         UUID,
    source_reference  VARCHAR(60),
    original_qty      NUMERIC(14,3)  NOT NULL,
    unit_cost         NUMERIC(14,4)  NOT NULL,
    CONSTRAINT stock_batch_batch_code_key UNIQUE (batch_code),
    CONSTRAINT stock_batch_source_type_check CHECK (
        source_type IN ('SUPPLIER_GRN', 'INTERNAL_GRN', 'STOCK_RETURN', 'STOCK_ADJUSTMENT', 'INTRA_PROJECT_ISSUE_RETURN')
    )
);
CREATE INDEX IF NOT EXISTS idx_stock_batch_item
    ON stock_batch (item_code, origin_date, stock_batch_id);

CREATE TABLE IF NOT EXISTS stock_batch_location (
    stock_batch_id  UUID           NOT NULL REFERENCES stock_batch (stock_batch_id) ON DELETE RESTRICT,
    project_code    VARCHAR(30)    NOT NULL,
    qty_remaining   NUMERIC(14,3)  NOT NULL,
    PRIMARY KEY (stock_batch_id, project_code),
    CONSTRAINT stock_batch_location_qty_remaining_check CHECK (qty_remaining >= 0)
);
CREATE INDEX IF NOT EXISTS idx_stock_batch_location_project
    ON stock_batch_location (project_code);

CREATE TABLE IF NOT EXISTS stock_batch_sequence (
    item_code    VARCHAR(50) NOT NULL REFERENCES item_code (code),
    origin_date  DATE        NOT NULL,
    last_seq     INTEGER     NOT NULL DEFAULT 0,
    PRIMARY KEY (item_code, origin_date)
);

CREATE TABLE IF NOT EXISTS stock_action_allocation (
    stock_action_allocation_id  UUID           PRIMARY KEY,
    stock_batch_id              UUID           NOT NULL REFERENCES stock_batch (stock_batch_id) ON DELETE RESTRICT,
    action_type                 VARCHAR(24)    NOT NULL,
    action_id                   UUID           NOT NULL,
    action_item_id              UUID,
    item_code                   VARCHAR(50)    NOT NULL REFERENCES item_code (code),
    qty_taken                   NUMERIC(14,3)  NOT NULL,
    unit_cost                   NUMERIC(14,4)  NOT NULL,
    CONSTRAINT stock_action_allocation_action_type_check CHECK (
        action_type IN ('GIN', 'INTRA_PROJECT_ISSUE', 'STOCK_ADJUSTMENT', 'STOCK_RETURN')
    )
);
CREATE INDEX IF NOT EXISTS idx_stock_action_allocation_action
    ON stock_action_allocation (action_type, action_id);
CREATE INDEX IF NOT EXISTS idx_stock_action_allocation_batch
    ON stock_action_allocation (stock_batch_id);
