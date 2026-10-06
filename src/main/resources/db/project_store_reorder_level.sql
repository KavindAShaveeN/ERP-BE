-- Moves reorder level from the item catalog (one value per item, shared by every
-- project) to the project_store ledger (one value per project + item), so each
-- project's store can carry its own reorder threshold for the same item.
--
-- Plain JDBC-backed tables, not managed by Hibernate ddl-auto:update.
-- Run this script manually against the application database.
--
-- Safe to re-run: every step is guarded (IF NOT EXISTS / IF EXISTS).

BEGIN;

-- ===========================================================================
-- STEP 1: add the new per-project-store reorder level column.
-- Defaults to 0 so existing rows (and any row inserted before the backfill
-- below runs) are never left NULL.
-- ===========================================================================
ALTER TABLE project_store
    ADD COLUMN IF NOT EXISTS reorder_level NUMERIC(12,2) NOT NULL DEFAULT 0;

-- ===========================================================================
-- STEP 2: backfill every existing project_store row from the item's old,
-- single global catalog value — so nothing silently drops to 0 for stock
-- that's already being tracked. New project stores created after this
-- migration start at 0 until someone sets a value for that project.
-- ===========================================================================
UPDATE project_store ps
SET reorder_level = ci.minimal_quantity
FROM item_code i
JOIN consumable_item ci ON ci.item_code_id = i.item_code_id
WHERE ps.item_code = i.item_code_code
  AND ps.reorder_level = 0;

UPDATE project_store ps
SET reorder_level = ii.re_order_level
FROM item_code i
JOIN inventory_item ii ON ii.item_code_id = i.item_code_id
WHERE ps.item_code = i.item_code_code
  AND ps.reorder_level = 0;

-- ===========================================================================
-- STEP 3: reorder level no longer lives on the catalog — drop the old
-- single-value columns now that every project_store row has its own copy.
-- ===========================================================================
ALTER TABLE consumable_item DROP COLUMN IF EXISTS minimal_quantity;
ALTER TABLE inventory_item  DROP COLUMN IF EXISTS re_order_level;

COMMIT;
