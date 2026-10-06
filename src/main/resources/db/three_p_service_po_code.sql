-- Links a third-party service issue to the purchase order it was raised against, so the
-- service charge can be taken from that PO. Stored as the PO's code (po.po_code), nullable
-- because existing rows and services with no PO have none.
--
-- Plain JDBC-backed table, not managed by Hibernate. Run this script manually.

ALTER TABLE three_p_service
    ADD COLUMN IF NOT EXISTS po_code VARCHAR(50);
