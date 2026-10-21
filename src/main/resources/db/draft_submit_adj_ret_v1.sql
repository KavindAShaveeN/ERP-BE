-- Draft / Requested lifecycle for stock adjustments (ADJ), stock returns (RET), GIN, GRN and PO, same approval engine as MR.
-- Re-runnable. Run after rbac_workflow_v1.sql and rbac_adj_ret_permissions_v1.sql, before deploying the backend.
--
-- Status shown to users:  Draft = not submitted | Requested = submitted, waiting in "My approvals"
--                         Approved = is_approved | Rejected = approved_by set while is_approved is false

ALTER TABLE stock_adjustment ADD COLUMN IF NOT EXISTS is_submitted boolean NOT NULL DEFAULT false;
ALTER TABLE stock_return     ADD COLUMN IF NOT EXISTS is_submitted boolean NOT NULL DEFAULT false;
ALTER TABLE gin              ADD COLUMN IF NOT EXISTS is_submitted boolean NOT NULL DEFAULT false;
ALTER TABLE grn              ADD COLUMN IF NOT EXISTS is_submitted boolean NOT NULL DEFAULT false;

-- Documents already decided were obviously submitted; still-pending ones come back as Draft and can be submitted again.
UPDATE stock_adjustment SET is_submitted = true WHERE is_approved OR approved_by IS NOT NULL;
UPDATE stock_return     SET is_submitted = true WHERE is_approved OR approved_by IS NOT NULL;
UPDATE gin              SET is_submitted = true WHERE is_authorized OR approved_by IS NOT NULL;
UPDATE grn              SET is_submitted = true WHERE is_approved OR checked_by IS NOT NULL;

-- PO keeps its approval_status column as the lifecycle: DRAFT | PENDING (= Requested) | APPROVED | REJECTED.
-- A legacy PENDING PO has no approval request behind it, so it goes back to DRAFT to be submitted again.
UPDATE po SET approval_status = 'DRAFT' WHERE approval_status = 'PENDING' AND NOT is_approved;

-- Default workflows (edit under Workflows): one MANAGER step.
--   ADJ: a manager of the adjusting project
--   RET: a manager of the receiving project (the requesting project when the return has no destination, e.g. supplier returns)
INSERT INTO approval_workflows (doc_type, name)
SELECT 'ADJ', 'Stock adjustment approval'
WHERE NOT EXISTS (SELECT 1 FROM approval_workflows WHERE doc_type = 'ADJ' AND is_active);

INSERT INTO approval_workflows (doc_type, name)
SELECT 'RET', 'Stock return approval'
WHERE NOT EXISTS (SELECT 1 FROM approval_workflows WHERE doc_type = 'RET' AND is_active);

INSERT INTO approval_steps (workflow_id, step_order, role_id, scope, mode)
SELECT w.workflow_id, 1, r.role_id, 'SAME_PROJECT', 'ANY_ONE'
FROM approval_workflows w JOIN roles r ON r.name = 'MANAGER'
WHERE w.doc_type = 'ADJ' AND w.is_active
  AND NOT EXISTS (SELECT 1 FROM approval_steps s WHERE s.workflow_id = w.workflow_id);

INSERT INTO approval_steps (workflow_id, step_order, role_id, scope, mode)
SELECT w.workflow_id, 1, r.role_id, 'DESTINATION_PROJECT', 'ANY_ONE'
FROM approval_workflows w JOIN roles r ON r.name = 'MANAGER'
WHERE w.doc_type = 'RET' AND w.is_active
  AND NOT EXISTS (SELECT 1 FROM approval_steps s WHERE s.workflow_id = w.workflow_id);

--   GIN: a manager of the receiving project (as before; the issuing project when there is no receiving project)
--   GRN: a manager of the project that raises the GRN      PO: a manager of the project that raises (bills) the PO
INSERT INTO approval_workflows (doc_type, name)
SELECT d.doc_type, d.name
FROM (VALUES ('GIN', 'GIN approval'), ('GRN', 'GRN approval'), ('PO', 'Purchase order approval')) AS d(doc_type, name)
WHERE NOT EXISTS (SELECT 1 FROM approval_workflows w WHERE w.doc_type = d.doc_type AND w.is_active);

INSERT INTO approval_steps (workflow_id, step_order, role_id, scope, mode)
SELECT w.workflow_id, 1, r.role_id, 'SAME_PROJECT', 'ANY_ONE'
FROM approval_workflows w JOIN roles r ON r.name = 'MANAGER'
WHERE w.doc_type IN ('GRN', 'PO') AND w.is_active
  AND NOT EXISTS (SELECT 1 FROM approval_steps s WHERE s.workflow_id = w.workflow_id);

INSERT INTO approval_steps (workflow_id, step_order, role_id, scope, mode)
SELECT w.workflow_id, 1, r.role_id, 'DESTINATION_PROJECT', 'ANY_ONE'
FROM approval_workflows w JOIN roles r ON r.name = 'MANAGER'
WHERE w.doc_type = 'GIN' AND w.is_active
  AND NOT EXISTS (SELECT 1 FROM approval_steps s WHERE s.workflow_id = w.workflow_id);
