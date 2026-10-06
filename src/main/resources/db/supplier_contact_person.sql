-- Adds a contact person name and phone number to the supplier record.
-- Plain JDBC-backed table (see SupplierRepository), not managed by Hibernate
-- ddl-auto:update. Run this script manually against the application database.

ALTER TABLE supplier
    ADD COLUMN IF NOT EXISTS contactPerson VARCHAR(255),
    ADD COLUMN IF NOT EXISTS contactPersonNumber VARCHAR(50);
