-- IntraProjectIssue and IntraProjectIssueItem tables (Site Store "Intra Project Issue" feature).
-- Goods issued from a project's site store to a phase within the same project.
-- Plain JDBC-backed tables (matching the fuel_issue/gin pattern in this codebase),
-- so they are NOT created automatically by Hibernate's ddl-auto:update.
-- Run this script manually against the application database before using the
-- /api/intra_project_issue/** endpoints.

CREATE TABLE IF NOT EXISTS intra_project_issue (
    intra_project_issue_id        UUID           PRIMARY KEY,
    intra_project_issue_code      VARCHAR(40)    NOT NULL,
    issued_by                     VARCHAR(50)    NOT NULL REFERENCES employee (employee_code),
    issued_date                   DATE           NOT NULL,
    issued_project_code           VARCHAR(50)    NOT NULL REFERENCES project (project_code),
    received_project_phase_id     INTEGER        REFERENCES project_phase (project_phase_id),
    received_by                   VARCHAR(50)    NOT NULL REFERENCES employee (employee_code),
    received_date                 DATE,
    approved_by                   VARCHAR(50)    REFERENCES employee (employee_code),
    approved_date                 DATE,
    is_authorized                 BOOLEAN        NOT NULL DEFAULT FALSE,
    is_issued                     BOOLEAN        NOT NULL DEFAULT TRUE,
    is_received                   BOOLEAN        NOT NULL DEFAULT FALSE,
    CONSTRAINT intra_project_issue_code_key UNIQUE (intra_project_issue_code)
);
CREATE INDEX IF NOT EXISTS idx_intra_project_issue_issued_project_code ON intra_project_issue (issued_project_code);
CREATE INDEX IF NOT EXISTS idx_intra_project_issue_received_project_phase_id ON intra_project_issue (received_project_phase_id);

CREATE TABLE IF NOT EXISTS intra_project_issue_item (
    intra_project_issue_item_id  UUID           PRIMARY KEY,
    intra_project_issue_id       UUID           NOT NULL REFERENCES intra_project_issue (intra_project_issue_id) ON DELETE CASCADE,
    item_code                    VARCHAR(50)    NOT NULL,
    description                  VARCHAR(200),
    size                         VARCHAR(50),
    uom_id                       INTEGER        REFERENCES uom (uom_id),
    quantity                     NUMERIC(12,2)  NOT NULL,
    remarks                      VARCHAR(200)
);
CREATE INDEX IF NOT EXISTS idx_intra_project_issue_item_issue_id ON intra_project_issue_item (intra_project_issue_id);
CREATE INDEX IF NOT EXISTS idx_intra_project_issue_item_item_code ON intra_project_issue_item (item_code);
