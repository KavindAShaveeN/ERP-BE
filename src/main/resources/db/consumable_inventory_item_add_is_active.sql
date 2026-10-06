-- Adds an active/inactive status to consumable and inventory items, so items
-- no longer in use can be hidden from new transactions without deleting their history.
-- Plain JDBC-backed tables (see item_code_consumable_item_schema.sql / dummy_item_seed_data.sql),
-- not managed by Hibernate ddl-auto:update. Run this script manually against the application database.

ALTER TABLE consumable_item
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE inventory_item
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;
