-- Links a GRN back to the stock return it's receiving, mirroring grn.gin_id: a normal
-- (internal) GRN can be filed either against a GIN (goods issued to this project) or a
-- stock return (goods sent back to this project), never both. Approving a stock return
-- only debits the returning project (StockReturnService.applyToStock) — nothing credits
-- the receiving project until it files a GRN referencing this column, at which point
-- GRNService.receiveItemIntoBatches replays the exact batch(es) that debit drew from,
-- exactly how a GIN-linked GRN already works today.
-- Plain JDBC-backed tables, not managed by Hibernate ddl-auto:update. Run this script
-- manually against the application database.

ALTER TABLE grn
    ADD COLUMN IF NOT EXISTS stock_return_id UUID;

CREATE INDEX IF NOT EXISTS idx_grn_stock_return_id ON grn (stock_return_id);
