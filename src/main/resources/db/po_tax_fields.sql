-- Adds SSCL/VAT tax calculation fields to Purchase Orders.
-- SSCL has no separate on/off toggle in the UI: a filled-in, non-zero SSCL rate means it
-- applies; a blank/zero rate means it doesn't. sscl_applicable is derived server-side
-- from sscl_percentage on every save (POService.applyTaxDefaults) rather than being an
-- independently editable flag, so it defaults to 0/FALSE — not a suggested rate.
-- SSCL Amount = Total Value x SSCL Rate (0 if not applicable).
-- VAT Amount = (Total Value + SSCL Amount) x VAT Rate — VAT is charged on top of SSCL.
-- Final PO Value = Total Value + SSCL Amount + VAT Amount — NOT a stored column: it's
-- fully derivable from total_value/sscl_amount/vat_amount (all stored), so PO.java
-- computes it on read (PO.getFinalPoValue()) instead of persisting a value that could
-- drift from its own inputs.
-- Plain JDBC-backed table (see po/po_item creation), not managed by Hibernate
-- ddl-auto:update. Run this script manually against the application database.

ALTER TABLE po ADD COLUMN IF NOT EXISTS sscl_applicable BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE po ADD COLUMN IF NOT EXISTS sscl_percentage NUMERIC(5,2) NOT NULL DEFAULT 0;
ALTER TABLE po ADD COLUMN IF NOT EXISTS sscl_amount NUMERIC(14,2) NOT NULL DEFAULT 0;
ALTER TABLE po ADD COLUMN IF NOT EXISTS vat_amount NUMERIC(14,2) NOT NULL DEFAULT 0;
ALTER TABLE po ALTER COLUMN vat_percentage SET DEFAULT 18;
