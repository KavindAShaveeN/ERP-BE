-- Approval permissions for stock adjustments (ADJ) and stock returns (RET), same shape as MR/PO/GIN/GRN.
INSERT INTO permissions (code, module, action, description)
SELECT m.module || '.' || a.action, m.module, a.action, m.module || ' - ' || a.action
FROM (VALUES ('ADJ'), ('RET')) AS m(module)
CROSS JOIN (VALUES ('VIEW'), ('CREATE'), ('EDIT'), ('DELETE'), ('APPROVE'), ('REJECT')) AS a(action)
ON CONFLICT (code) DO NOTHING;

-- ADMIN: everything
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id FROM roles r CROSS JOIN permissions p
WHERE r.name = 'ADMIN' AND p.module IN ('ADJ', 'RET')
ON CONFLICT DO NOTHING;

-- MD and MANAGER: view, approve and reject
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id FROM roles r JOIN permissions p ON p.action IN ('VIEW', 'APPROVE', 'REJECT')
WHERE r.name IN ('MD', 'MANAGER') AND p.module IN ('ADJ', 'RET')
ON CONFLICT DO NOTHING;

-- ENGINEER and TO: view, create and edit
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id FROM roles r JOIN permissions p ON p.action IN ('VIEW', 'CREATE', 'EDIT')
WHERE r.name IN ('ENGINEER', 'TO') AND p.module IN ('ADJ', 'RET')
ON CONFLICT DO NOTHING;
