-- Adds a per-item supplier name to GRN items, mirroring po_item.supplier_name so a GRN
-- raised against a multi-supplier PO keeps each line's supplier.
-- Plain JDBC-backed table, not managed by Hibernate ddl-auto:update. Run this script
-- manually against the application database.

ALTER TABLE grn_item
    ADD COLUMN IF NOT EXISTS supplier_name VARCHAR(255);
