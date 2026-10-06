-- Lets a GIN (Goods Issue Note, warehouse main-store issue) line for a
-- dimensional item (see com.rr.erp.util.DimensionalItems) record which exact
-- size of the item the store is issuing (e.g. draw down from the 6m bars,
-- not the 1m offcuts), so StockBatchService can scope FIFO to that size only.
-- The existing free-text `size` column is left untouched.
--
-- Plain JDBC-backed table (gin_item has no CREATE TABLE script tracked in
-- this repo's migrations — its DDL predates this migration convention). Run
-- this script manually against the application database.

ALTER TABLE gin_item
    ADD COLUMN IF NOT EXISTS length_m NUMERIC(10,3),
    ADD COLUMN IF NOT EXISTS width_m  NUMERIC(10,3);
