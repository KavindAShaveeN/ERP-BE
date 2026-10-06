-- Adds the originating project of a Purchase Order's source Material Request.
-- When a PO is created by directing an incoming MR straight to a PO
-- (Purchasing > Incoming Material Requests > "Create PO"), the "Deliver project"
-- and "Bill-to project" fields are left blank for manual entry, so this column
-- preserves the MR's requesting_project_code (the project that originally
-- raised the MR) for reference on the PO.
-- Plain JDBC-backed table (see po/po_item creation), not managed by Hibernate
-- ddl-auto:update. Run this script manually against the application database.

ALTER TABLE po
    ADD COLUMN IF NOT EXISTS mr_requesting_project_code VARCHAR(50) REFERENCES project (project_code);
