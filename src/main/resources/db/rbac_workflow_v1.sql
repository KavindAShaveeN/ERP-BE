-- Configurable roles, permissions and approval workflows.
-- Re-runnable: every statement is IF NOT EXISTS / ON CONFLICT DO NOTHING.
-- Run manually before deploying the backend that uses it.

-- ---------------------------------------------------------------------------
-- Permission catalogue (fixed by code) and company-defined roles
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS permissions (
    permission_id serial PRIMARY KEY,
    code          varchar(100) NOT NULL UNIQUE,   -- MODULE.ACTION, e.g. MR.APPROVE
    module        varchar(50)  NOT NULL,
    action        varchar(50)  NOT NULL,
    description   varchar(300) NULL
);

CREATE TABLE IF NOT EXISTS roles (
    role_id     serial PRIMARY KEY,
    name        varchar(100) NOT NULL UNIQUE,
    description varchar(300) NULL,
    is_system   boolean NOT NULL DEFAULT false,   -- seeded role: editable, not deletable
    is_active   boolean NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id       int NOT NULL REFERENCES roles(role_id) ON DELETE CASCADE,
    permission_id int NOT NULL REFERENCES permissions(permission_id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- project_code NULL = the role applies in every project (global)
CREATE TABLE IF NOT EXISTS user_roles (
    user_role_id  serial PRIMARY KEY,
    employee_code varchar(50) NOT NULL REFERENCES employee(employee_code),
    role_id       int NOT NULL REFERENCES roles(role_id),
    project_code  varchar(50) NULL REFERENCES project(project_code)
);
CREATE UNIQUE INDEX IF NOT EXISTS ux_user_roles_scope
    ON user_roles (employee_code, role_id, COALESCE(project_code, ''));
CREATE INDEX IF NOT EXISTS idx_user_roles_employee ON user_roles (employee_code);

-- Per-user exceptions on top of roles. A deny (allow = false) always wins.
CREATE TABLE IF NOT EXISTS user_permission_overrides (
    override_id   serial PRIMARY KEY,
    employee_code varchar(50) NOT NULL REFERENCES employee(employee_code),
    permission_id int NOT NULL REFERENCES permissions(permission_id) ON DELETE CASCADE,
    project_code  varchar(50) NULL REFERENCES project(project_code),
    allow         boolean NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS ux_user_permission_overrides_scope
    ON user_permission_overrides (employee_code, permission_id, COALESCE(project_code, ''));

-- ---------------------------------------------------------------------------
-- Approval workflows
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS approval_workflows (
    workflow_id serial PRIMARY KEY,
    doc_type    varchar(50)  NOT NULL,            -- MR, PO, GIN ...
    name        varchar(150) NOT NULL,
    is_active   boolean NOT NULL DEFAULT true
);
CREATE UNIQUE INDEX IF NOT EXISTS ux_approval_workflows_active_doc_type
    ON approval_workflows (doc_type) WHERE is_active;

CREATE TABLE IF NOT EXISTS approval_steps (
    step_id         serial PRIMARY KEY,
    workflow_id     int NOT NULL REFERENCES approval_workflows(workflow_id) ON DELETE CASCADE,
    step_order      int NOT NULL,
    role_id         int NOT NULL REFERENCES roles(role_id),
    scope           varchar(30) NOT NULL DEFAULT 'SAME_PROJECT',  -- SAME_PROJECT | DESTINATION_PROJECT | ANY
    mode            varchar(10) NOT NULL DEFAULT 'ANY_ONE',       -- ANY_ONE | ALL
    condition_field varchar(50)  NULL,            -- e.g. amount; step applies only when the condition holds
    condition_op    varchar(5)   NULL,            -- > >= < <= =
    condition_value numeric(18, 2) NULL,
    CONSTRAINT ck_approval_steps_scope CHECK (scope IN ('SAME_PROJECT', 'DESTINATION_PROJECT', 'ANY')),
    CONSTRAINT ck_approval_steps_mode CHECK (mode IN ('ANY_ONE', 'ALL')),
    CONSTRAINT ck_approval_steps_condition CHECK (
        (condition_field IS NULL AND condition_op IS NULL AND condition_value IS NULL) OR
        (condition_field IS NOT NULL AND condition_op IN ('>', '>=', '<', '<=', '=') AND condition_value IS NOT NULL)),
    CONSTRAINT ux_approval_steps_order UNIQUE (workflow_id, step_order)
);

CREATE TABLE IF NOT EXISTS approval_requests (
    request_id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    doc_type             varchar(50) NOT NULL,
    doc_id               uuid NOT NULL,
    requesting_project   varchar(50) NOT NULL REFERENCES project(project_code),
    destination_project  varchar(50) NULL REFERENCES project(project_code),
    status               varchar(15) NOT NULL DEFAULT 'PENDING',   -- PENDING | APPROVED | REJECTED | CANCELLED
    current_step_id      int NULL REFERENCES approval_steps(step_id),
    submitted_by         varchar(50) NOT NULL REFERENCES employee(employee_code),
    submitted_at         timestamp NOT NULL DEFAULT now(),
    completed_at         timestamp NULL,
    CONSTRAINT ck_approval_requests_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'))
);
CREATE INDEX IF NOT EXISTS idx_approval_requests_doc ON approval_requests (doc_type, doc_id);
CREATE INDEX IF NOT EXISTS idx_approval_requests_pending ON approval_requests (status) WHERE status = 'PENDING';

-- The steps that apply to one request, frozen at submit time so later workflow edits
-- do not change a request that is already in flight.
CREATE TABLE IF NOT EXISTS approval_request_steps (
    request_id uuid NOT NULL REFERENCES approval_requests(request_id) ON DELETE CASCADE,
    step_id    int  NOT NULL REFERENCES approval_steps(step_id),
    seq        int  NOT NULL,
    PRIMARY KEY (request_id, step_id)
);

CREATE TABLE IF NOT EXISTS approval_actions (
    action_id  bigserial PRIMARY KEY,
    request_id uuid NOT NULL REFERENCES approval_requests(request_id) ON DELETE CASCADE,
    step_id    int  NULL REFERENCES approval_steps(step_id),
    actor_code varchar(50) NOT NULL REFERENCES employee(employee_code),
    action     varchar(10) NOT NULL,               -- APPROVE | REJECT | COMMENT
    remark     varchar(500) NULL,
    acted_at   timestamp NOT NULL DEFAULT now(),
    CONSTRAINT ck_approval_actions_action CHECK (action IN ('APPROVE', 'REJECT', 'COMMENT'))
);
CREATE INDEX IF NOT EXISTS idx_approval_actions_request ON approval_actions (request_id);

CREATE TABLE IF NOT EXISTS rbac_audit_log (
    audit_id   bigserial PRIMARY KEY,
    actor_code varchar(50) NULL,
    entity     varchar(50) NOT NULL,
    entity_id  varchar(100) NULL,
    change     text NULL,
    at         timestamp NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------------
-- Seed: permission catalogue
-- ---------------------------------------------------------------------------
INSERT INTO permissions (code, module, action, description)
SELECT m.module || '.' || a.action, m.module, a.action, m.module || ' - ' || a.action
FROM (VALUES ('MR'), ('PO'), ('GIN'), ('GRN')) AS m(module)
CROSS JOIN (VALUES ('VIEW'), ('CREATE'), ('EDIT'), ('DELETE'), ('APPROVE'), ('REJECT')) AS a(action)
ON CONFLICT (code) DO NOTHING;

INSERT INTO permissions (code, module, action, description) VALUES
    ('USER.MANAGE',     'USER',     'MANAGE', 'Assign roles and permission overrides to users'),
    ('ROLE.MANAGE',     'ROLE',     'MANAGE', 'Create and edit roles and their permissions'),
    ('WORKFLOW.MANAGE', 'WORKFLOW', 'MANAGE', 'Configure approval workflows')
ON CONFLICT (code) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Seed: default roles (the company can edit or extend them)
-- ---------------------------------------------------------------------------
INSERT INTO roles (name, description, is_system) VALUES
    ('ADMIN',    'Full access, including managing roles, users and workflows', true),
    ('MD',       'Managing director (default permissions - edit to suit)',    true),
    ('MANAGER',  'Manager (default permissions - edit to suit)',              true),
    ('ENGINEER', 'Engineer (default permissions - edit to suit)',             true),
    ('TO',       'Technical officer (default permissions - edit to suit)',    true)
ON CONFLICT (name) DO NOTHING;

-- ADMIN: everything
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id FROM roles r CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- MD and MANAGER: view, approve and reject documents
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id FROM roles r JOIN permissions p ON p.action IN ('VIEW', 'APPROVE', 'REJECT')
WHERE r.name IN ('MD', 'MANAGER')
ON CONFLICT DO NOTHING;

-- ENGINEER and TO: view, create and edit documents
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id FROM roles r JOIN permissions p ON p.action IN ('VIEW', 'CREATE', 'EDIT')
WHERE r.name IN ('ENGINEER', 'TO')
ON CONFLICT DO NOTHING;

-- Nobody gets locked out: the first three employees become global ADMINs
INSERT INTO user_roles (employee_code, role_id, project_code)
SELECT e.employee_code, r.role_id, NULL
FROM employee e CROSS JOIN roles r
WHERE r.name = 'ADMIN' AND e.employee_code IN ('00001', '00002', '00003')
ON CONFLICT DO NOTHING;

-- Default MR workflow: one step, MANAGER at the destination project (keeps today's behaviour)
INSERT INTO approval_workflows (doc_type, name)
SELECT 'MR', 'Material request approval'
WHERE NOT EXISTS (SELECT 1 FROM approval_workflows WHERE doc_type = 'MR' AND is_active);

INSERT INTO approval_steps (workflow_id, step_order, role_id, scope, mode)
SELECT w.workflow_id, 1, r.role_id, 'DESTINATION_PROJECT', 'ANY_ONE'
FROM approval_workflows w JOIN roles r ON r.name = 'MANAGER'
WHERE w.doc_type = 'MR' AND w.is_active
  AND NOT EXISTS (SELECT 1 FROM approval_steps s WHERE s.workflow_id = w.workflow_id)
;

-- ---------------------------------------------------------------------------
-- Backfill: give every existing MR an approval request so history and the inbox are consistent.
-- Only MRs that have no request yet are touched, so this is safe to re-run.
-- ---------------------------------------------------------------------------
INSERT INTO approval_requests (doc_type, doc_id, requesting_project, destination_project, status,
                               current_step_id, submitted_by, submitted_at, completed_at)
SELECT 'MR', m.mr_id, m.requesting_project_code, m.destination_project_code,
       CASE WHEN m.is_approved THEN 'APPROVED' WHEN m.checked_by IS NOT NULL THEN 'REJECTED' ELSE 'PENDING' END,
       CASE WHEN NOT m.is_approved AND m.checked_by IS NULL THEN
            (SELECT s.step_id FROM approval_steps s JOIN approval_workflows w ON w.workflow_id = s.workflow_id
             WHERE w.doc_type = 'MR' AND w.is_active ORDER BY s.step_order LIMIT 1) END,
       m.requested_by, m.requested_date,
       CASE WHEN m.is_approved OR m.checked_by IS NOT NULL THEN COALESCE(m.approved_date, m.checked_date) END
FROM mr m
WHERE NOT EXISTS (SELECT 1 FROM approval_requests r WHERE r.doc_type = 'MR' AND r.doc_id = m.mr_id);

-- Freeze the default workflow's steps into the still-pending backfilled requests
INSERT INTO approval_request_steps (request_id, step_id, seq)
SELECT r.request_id, s.step_id, s.step_order
FROM approval_requests r
JOIN approval_workflows w ON w.doc_type = 'MR' AND w.is_active
JOIN approval_steps s ON s.workflow_id = w.workflow_id
WHERE r.doc_type = 'MR' AND r.status = 'PENDING'
  AND NOT EXISTS (SELECT 1 FROM approval_request_steps x WHERE x.request_id = r.request_id)
ON CONFLICT DO NOTHING;

-- Record who approved / rejected the already-decided MRs
INSERT INTO approval_actions (request_id, step_id, actor_code, action, remark, acted_at)
SELECT r.request_id, NULL, COALESCE(m.approved_by, m.checked_by),
       CASE WHEN m.is_approved THEN 'APPROVE' ELSE 'REJECT' END,
       'Imported from existing MR', COALESCE(m.approved_date, m.checked_date, now())
FROM approval_requests r JOIN mr m ON m.mr_id = r.doc_id
WHERE r.doc_type = 'MR' AND r.status IN ('APPROVED', 'REJECTED')
  AND COALESCE(m.approved_by, m.checked_by) IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM approval_actions a WHERE a.request_id = r.request_id);
