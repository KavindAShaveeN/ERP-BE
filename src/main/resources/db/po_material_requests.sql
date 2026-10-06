-- Links zero or more Material Requests to a Purchase Order (many-to-many): a single PO
-- commonly consolidates items from several MRs. This supplements — does not replace —
-- po.mr_id, which stays the single "originating" MR captured automatically by the
-- Forward-to-Purchasing / Create-PO-from-MR flows (see po_mr_requesting_project_code.sql).
-- This table lets a PO's related MRs be freely added/removed from the PO form (synced in
-- full on every create/update — see POService.createPO/updatePO), independent of how the
-- PO was created.
-- Plain JDBC-backed table (see po/po_item creation), not managed by Hibernate
-- ddl-auto:update. Run this script manually against the application database.

CREATE TABLE IF NOT EXISTS po_material_request (
    po_id UUID NOT NULL REFERENCES po(po_id),
    mr_id UUID NOT NULL REFERENCES mr(mr_id),
    linked_date TIMESTAMP NOT NULL DEFAULT now(),
    linked_by VARCHAR(120),
    PRIMARY KEY (po_id, mr_id)
);

CREATE INDEX IF NOT EXISTS idx_po_material_request_mr ON po_material_request (mr_id);
