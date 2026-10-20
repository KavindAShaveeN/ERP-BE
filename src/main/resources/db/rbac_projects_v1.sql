-- Project permissions: view / create / edit. Safe to re-run.
--   PROJECT.VIEW   open a project, its sub phases and subcontractors (only projects the user is assigned to)
--   PROJECT.CREATE register a new project
--   PROJECT.EDIT   edit a project, its sub phases and subcontractors (only projects the user is assigned to)
-- "Assigned" means the role is held for that project (User access page) or is a global role.

INSERT INTO permissions (code, module, action, description) VALUES
    ('PROJECT.VIEW',   'PROJECT', 'VIEW',   'Projects - view'),
    ('PROJECT.CREATE', 'PROJECT', 'CREATE', 'Projects - create'),
    ('PROJECT.EDIT',   'PROJECT', 'EDIT',   'Projects - edit')
ON CONFLICT (code) DO NOTHING;

-- Every existing role keeps view access; which projects it applies to is decided by the assignment
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r CROSS JOIN permissions p
WHERE p.code = 'PROJECT.VIEW'
ON CONFLICT DO NOTHING;

-- ADMIN, MD and MANAGER: create and edit
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r CROSS JOIN permissions p
WHERE p.code IN ('PROJECT.CREATE', 'PROJECT.EDIT') AND r.name IN ('ADMIN', 'MD', 'MANAGER')
ON CONFLICT DO NOTHING;
