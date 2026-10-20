-- Non-stock items only: remember which batch the user picked on a line, so a
-- Pending -> Authorized document draws from that batch.
-- NULL for every stock/consumable line, which keeps using FIFO exactly as before.
ALTER TABLE intra_project_issue_item
    ADD COLUMN IF NOT EXISTS stock_batch_id UUID NULL;

ALTER TABLE gin_item
    ADD COLUMN IF NOT EXISTS stock_batch_id UUID NULL;
