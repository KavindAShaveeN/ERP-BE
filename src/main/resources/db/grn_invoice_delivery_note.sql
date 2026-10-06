-- Adds the supplier's invoice number and delivery note number to GRNs. Plain JDBC-backed
-- table, not managed by Hibernate ddl-auto:update. Run this script manually.

ALTER TABLE grn
    ADD COLUMN IF NOT EXISTS invoice_number       VARCHAR(100),
    ADD COLUMN IF NOT EXISTS delivery_note_number VARCHAR(100);
