-- Allow fractional quantities (e.g. 7.5 kg, 107.5 L) on MR, GIN, stock return and stock adjustment lines.
-- Widening int4 -> numeric(18,3) preserves existing values. Matches grn_item.quantity.
-- If an ALTER fails with "cannot alter type of a column used by a view", drop/recreate that view around it.
BEGIN;
ALTER TABLE mr_item                ALTER COLUMN quantity            TYPE numeric(18,3);
ALTER TABLE gin_item               ALTER COLUMN quantity            TYPE numeric(18,3);
ALTER TABLE stock_return_item      ALTER COLUMN quantity            TYPE numeric(18,3);
ALTER TABLE stock_adjustment_item  ALTER COLUMN adjustment_quantity TYPE numeric(18,3);
COMMIT;
