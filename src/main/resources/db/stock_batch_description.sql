-- Adds a per-batch description so distinct physical items sharing one item_code
-- (this matters most for non-stock items, where one code like "NS-001" can mean many
-- different things depending on what was actually typed on the MR/PO/GRN line — e.g.
-- "Office chair, blue" vs "Office chair, red") can be told apart once they're in stock,
-- picked explicitly at issue time (GIN / Stock Adjustment / Intra Project Issue), and
-- shown correctly on the resulting Intra Project Issue Return.
--
-- Populated from the GRN line's own description at the moment a NEW batch is opened
-- (see GRNService#receiveItemIntoBatches / StockBatchService#openNewBatch) — never
-- retroactively backfilled, and never changed once a batch exists (matches batch_code/
-- origin_date/unit_cost, which are likewise permanent once set). Null for batches opened
-- before this column existed, and for item types where the GRN line's description is
-- just the item's own name (harmless either way — it's purely descriptive, not used to
-- scope FIFO for stock/consumable/inventory items).
--
-- Plain JDBC-backed table (see stock_batch_schema.sql), not managed by Hibernate
-- ddl-auto:update. Run this script manually, after stock_batch_schema.sql.

ALTER TABLE stock_batch
    ADD COLUMN IF NOT EXISTS description VARCHAR(255);
