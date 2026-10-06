-- Adds a real Supplier vs Internal type to stock returns, mirroring the
-- isSupplierGRN / poCode / ginId split GRN already has. A SUPPLIER return
-- sends stock back to a vendor (no destination project — po_code/supplier_code
-- carry the reference instead), while an INTERNAL return stays a
-- project-to-project transfer via to_project_code, optionally traceable to the
-- gin_id it reverses. to_project_code is relaxed to nullable because a
-- SUPPLIER return has no receiving project.
-- Plain JDBC-backed tables, not managed by Hibernate ddl-auto:update. Run
-- this script manually against the application database.

ALTER TABLE stock_return
    ADD COLUMN IF NOT EXISTS return_type VARCHAR(20) NOT NULL DEFAULT 'INTERNAL'
        CHECK (return_type IN ('INTERNAL', 'SUPPLIER'));
ALTER TABLE stock_return
    ADD COLUMN IF NOT EXISTS po_code VARCHAR(40);
ALTER TABLE stock_return
    ADD COLUMN IF NOT EXISTS supplier_code VARCHAR(40);
ALTER TABLE stock_return
    ALTER COLUMN to_project_code DROP NOT NULL;

CREATE INDEX IF NOT EXISTS idx_stock_return_po_code ON stock_return (po_code);
