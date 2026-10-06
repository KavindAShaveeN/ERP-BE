-- Adds a per-item supplier name to Purchase Order items.
-- Plain JDBC-backed table (see po/po_item creation), not managed by Hibernate
-- ddl-auto:update. Run this script manually against the application database.

ALTER TABLE po_item
    ADD COLUMN IF NOT EXISTS supplier_name VARCHAR(255);
