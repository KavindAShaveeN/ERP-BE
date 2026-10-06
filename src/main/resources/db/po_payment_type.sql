-- Adds a structured Cash/Credit payment type to Purchase Orders, replacing the previous
-- free-text-only guessing (po.payment_term is arbitrary text like "30 days from invoice"
-- and can't be reliably matched against "cash" vs "credit"). Powers the Cash Purchase
-- Order Report filter.
-- Plain JDBC-backed table (see po/po_item creation), not managed by Hibernate
-- ddl-auto:update. Run this script manually against the application database.

ALTER TABLE po
    ADD COLUMN IF NOT EXISTS payment_type VARCHAR(20) NOT NULL DEFAULT 'Credit';

CREATE INDEX IF NOT EXISTS idx_po_payment_type ON po (payment_type);
