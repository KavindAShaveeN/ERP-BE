-- Adds unit price / line amount to stock return items, mirroring gin_item_pricing.sql —
-- the return form already computes these from the source GIN's FIFO cost allocations
-- but had nowhere to persist them, so the return view page always showed blank.
-- Plain JDBC-backed table (see stock_return/stock_return_item creation), not managed by
-- Hibernate ddl-auto:update. Run this script manually against the application database.

ALTER TABLE stock_return_item
    ADD COLUMN IF NOT EXISTS unit_price NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS amount NUMERIC(14,2);
