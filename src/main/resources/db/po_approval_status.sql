-- Adds a dedicated approval-workflow status (DRAFT / PENDING / APPROVED) to Purchase
-- Orders. This is independent of the two other PO status dimensions already in place:
--   - po.status (ACTIVE / HOLD / CANCELLED) — operational lifecycle
--   - delivery status (Pending / Partially / Fully delivered) — computed from GRNs,
--     not stored
-- Each is filterable on its own rather than being folded into one combined field.
-- is_approved (boolean) is left in place and kept in sync by the application (true
-- only once approval_status = APPROVED), so the existing approve workflow / edit-lock
-- logic keeps working unchanged.
-- Plain JDBC-backed table (see po/po_item creation), not managed by Hibernate
-- ddl-auto:update. Run this script manually against the application database.

ALTER TABLE po
    ADD COLUMN IF NOT EXISTS approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

UPDATE po SET approval_status = 'APPROVED' WHERE is_approved = TRUE;

CREATE INDEX IF NOT EXISTS idx_po_approval_status ON po (approval_status);
