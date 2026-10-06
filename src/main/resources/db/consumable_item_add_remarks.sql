-- Adds a remarks field to consumable_item, matching the one inventory_item already has, so
-- the consumable view/edit page can show the same item code / description / UOM / status /
-- remarks set as the inventory view/edit page.
-- Plain JDBC-backed table, not managed by Hibernate ddl-auto:update. Run this script manually
-- against the application database.

ALTER TABLE consumable_item
    ADD COLUMN IF NOT EXISTS remarks VARCHAR(500);
