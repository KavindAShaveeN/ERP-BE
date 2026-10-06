-- Adds the specific delivery site (one of the deliver-to project's locations, from
-- project_location) selected for a Purchase Order. Shown on the printable PO under
-- "Deliver To" instead of the project's general area.
-- Plain JDBC-backed table (see po/po_item creation), not managed by Hibernate
-- ddl-auto:update. Run this script manually against the application database.

ALTER TABLE po
    ADD COLUMN IF NOT EXISTS delivery_location VARCHAR(255);
