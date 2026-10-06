-- Adds a real lifecycle status to Purchase Orders (ACTIVE / CANCELLED / HOLD,
-- with a shared reason/changed-by/changed-date audit trail) so Cancel and Hold
-- Purchase Order reports can filter on a persisted field instead of inferring
-- state. Also adds grn.po_code, a free-text link from a supplier GRN back to
-- the PO it was received against (grn.po_number is an Integer and PO.po_code
-- is free text like "PO-2026-0012", so po_number silently fails to link for
-- any non-numeric PO code — po_code is the reliable replacement used by the
-- delivery/partial-purchasing reports; po_number is left in place, unused,
-- for backward-compatible display of old rows).
-- Plain JDBC-backed tables (see po/po_item/grn creation), not managed by
-- Hibernate ddl-auto:update. Run this script manually against the application
-- database.

ALTER TABLE po
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE po
    ADD COLUMN IF NOT EXISTS status_reason VARCHAR(500);
ALTER TABLE po
    ADD COLUMN IF NOT EXISTS status_changed_by VARCHAR(50);
ALTER TABLE po
    ADD COLUMN IF NOT EXISTS status_changed_date DATE;

CREATE INDEX IF NOT EXISTS idx_po_status ON po (status);

ALTER TABLE grn
    ADD COLUMN IF NOT EXISTS po_code VARCHAR(40);

CREATE INDEX IF NOT EXISTS idx_grn_po_code ON grn (po_code);
