-- Lets a stock adjustment line for a dimensional item (see
-- com.rr.erp.util.DimensionalItems) record the size of the bars/pieces being
-- found (positive adjustment) or written off (negative adjustment), so the
-- new batch opened / the FIFO draw made is scoped to that exact size.
-- stock_adjustment_item has no free-text `size` column today (unlike
-- GRN/GIN/intra-project-issue item lines) — this migration only adds the two
-- structured dimensional columns, nothing to preserve alongside them.
--
-- Plain JDBC-backed table. Run this script manually against the application
-- database.

ALTER TABLE stock_adjustment_item
    ADD COLUMN IF NOT EXISTS length_m NUMERIC(10,3),
    ADD COLUMN IF NOT EXISTS width_m  NUMERIC(10,3);
