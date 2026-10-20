-- Reverses rbac_workflow_v1.sql: drops every table it created (and all rows in them,
-- including roles, user role assignments, workflows and approval history).
-- Re-runnable. It does not touch the mr table or any other existing table.
-- Run manually; this cannot be undone.

DROP TABLE IF EXISTS approval_actions;
DROP TABLE IF EXISTS approval_request_steps;
DROP TABLE IF EXISTS approval_requests;
DROP TABLE IF EXISTS approval_steps;
DROP TABLE IF EXISTS approval_workflows;
DROP TABLE IF EXISTS rbac_audit_log;
DROP TABLE IF EXISTS user_permission_overrides;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS permissions;
