-- Removes the company_ref_no column from Purchase Orders — unused, dropped along
-- with the PO entity/DTO/service/repository/controller field of the same name.
-- Plain JDBC-backed table (see po/po_item creation), not managed by Hibernate
-- ddl-auto:update. Run this script manually against the application database.

ALTER TABLE po
    DROP COLUMN IF EXISTS company_ref_no;
