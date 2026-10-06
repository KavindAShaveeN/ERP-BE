-- Lets a supplier GRN line for a dimensional item (steel beams/sheet piles,
-- item code prefixed 'STBEAMS-' — see com.rr.erp.util.DimensionalItems) record
-- the size the bars/pieces were received at (e.g. 6m bars). GRNItem.quantity
-- continues to mean the piece/bar count for these items (existing business
-- convention, unchanged); length_m/width_m are the new structured companions
-- to the existing free-text `size` column, which is left untouched and still
-- usable as a human label. width_m is only populated for area-tracked (sheet)
-- items; length-only items leave it NULL.
--
-- Plain JDBC-backed table (grn_item has no CREATE TABLE script tracked in this
-- repo's migrations — its DDL predates this migration convention). Run this
-- script manually against the application database.

ALTER TABLE grn_item
    ADD COLUMN IF NOT EXISTS length_m NUMERIC(10,3),
    ADD COLUMN IF NOT EXISTS width_m  NUMERIC(10,3);
