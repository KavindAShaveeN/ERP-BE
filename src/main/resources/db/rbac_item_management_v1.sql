-- Item Management permissions: view / create / edit. Safe to re-run.
--   ITEM.VIEW   see the Item Management menu and its lists
--   ITEM.CREATE create item codes, code-master entries and assets
--   ITEM.EDIT   edit existing item codes, consumables, inventory, non-stock, services and assets

INSERT INTO permissions (code, module, action, description) VALUES
    ('ITEM.VIEW',   'ITEM', 'VIEW',   'Item Management - view'),
    ('ITEM.CREATE', 'ITEM', 'CREATE', 'Item Management - create'),
    ('ITEM.EDIT',   'ITEM', 'EDIT',   'Item Management - edit')
ON CONFLICT (code) DO NOTHING;

-- Everyone could use Item Management before, so every existing role keeps view access
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r CROSS JOIN permissions p
WHERE p.code = 'ITEM.VIEW'
ON CONFLICT DO NOTHING;

-- ADMIN: everything; ENGINEER and TO: create and edit (same pattern as the document permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r CROSS JOIN permissions p
WHERE p.code IN ('ITEM.CREATE', 'ITEM.EDIT') AND r.name IN ('ADMIN', 'ENGINEER', 'TO')
ON CONFLICT DO NOTHING;
