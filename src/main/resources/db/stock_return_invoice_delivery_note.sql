-- Adds an invoice number and delivery note number to Stock Return Notes. Plain JDBC-backed
-- table, not managed by Hibernate ddl-auto:update. Run this script manually.

ALTER TABLE stock_return
    ADD COLUMN IF NOT EXISTS invoice_number       VARCHAR(100),
    ADD COLUMN IF NOT EXISTS delivery_note_number VARCHAR(100);
