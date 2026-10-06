-- Adds a free-text remarks field to Purchase Orders (shown on the printable PO
-- above the items table when populated). The PO form already collects this
-- value locally but never persisted it — this column makes it durable.
-- Plain JDBC-backed table (see po/po_item creation), not managed by Hibernate
-- ddl-auto:update. Run this script manually against the application database.

ALTER TABLE po
    ADD COLUMN IF NOT EXISTS remarks VARCHAR(500);
