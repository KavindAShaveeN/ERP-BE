-- Extends intra_project_issue_return to support:
--  - GENERAL returns (return_type now also accepts 'GENERAL'), and
--  - SUBCONTRACTOR/PERSONAL returns whose items can span several source issues (a
--    subcontractor or employee can hold items issued across multiple intra project
--    issues) -- intra_project_issue_id on the header is no longer required for those,
--    since each return_item already carries its own intra_project_issue_item_id and
--    that is now what stock crediting resolves the source project from.
-- Plain JDBC-backed table (see intra_project_issue_return_schema.sql), not managed by
-- Hibernate ddl-auto:update. Run this script manually against the application database,
-- after intra_project_issue_return_schema.sql.

ALTER TABLE intra_project_issue_return ALTER COLUMN intra_project_issue_id DROP NOT NULL;

ALTER TABLE intra_project_issue_return
    ADD COLUMN IF NOT EXISTS subcontractor_id INTEGER REFERENCES subcontractor (subcontractor_id),
    ADD COLUMN IF NOT EXISTS employee_code VARCHAR(50) REFERENCES employee (employee_code);

ALTER TABLE intra_project_issue_return DROP CONSTRAINT IF EXISTS intra_project_issue_return_type_check;
ALTER TABLE intra_project_issue_return
    ADD CONSTRAINT intra_project_issue_return_type_check CHECK (return_type IN ('GENERAL', 'SUBCONTRACTOR', 'PERSONAL', 'LOAN'));
