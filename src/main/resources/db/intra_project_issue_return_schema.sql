-- IntraProjectIssueReturn and IntraProjectIssueReturnItem tables.
-- Records items returned by a subcontractor, an employee, or an outside
-- loan recipient back to the project that originally issued them via an
-- intra_project_issue -- structurally a copy of stock_return / stock_return_item,
-- approval-gated the same way (stock only moves once approved).
-- Plain JDBC-backed tables (matching the intra_project_issue/stock_return pattern
-- in this codebase), so they are NOT created automatically by Hibernate's
-- ddl-auto:update. Run this script manually against the application database,
-- after intra_project_issue_extended_types.sql.

CREATE TABLE IF NOT EXISTS intra_project_issue_return (
    intra_project_issue_return_id   UUID           PRIMARY KEY,
    intra_project_issue_return_code VARCHAR(40)    NOT NULL,
    intra_project_issue_id          UUID           NOT NULL REFERENCES intra_project_issue (intra_project_issue_id),
    issued_project_code             VARCHAR(50)    NOT NULL REFERENCES project (project_code),
    return_type                     VARCHAR(20)    NOT NULL,
    return_date                     DATE           NOT NULL,
    returned_by                     VARCHAR(50)    REFERENCES employee (employee_code),
    remarks                         VARCHAR(200),
    approved_by                     VARCHAR(50)    REFERENCES employee (employee_code),
    approved_date                   DATE,
    is_approved                     BOOLEAN        NOT NULL DEFAULT FALSE,
    CONSTRAINT intra_project_issue_return_code_key UNIQUE (intra_project_issue_return_code),
    CONSTRAINT intra_project_issue_return_type_check CHECK (return_type IN ('SUBCONTRACTOR', 'PERSONAL', 'LOAN'))
);
CREATE INDEX IF NOT EXISTS idx_intra_project_issue_return_issue_id ON intra_project_issue_return (intra_project_issue_id);
CREATE INDEX IF NOT EXISTS idx_intra_project_issue_return_issued_project_code ON intra_project_issue_return (issued_project_code);

CREATE TABLE IF NOT EXISTS intra_project_issue_return_item (
    intra_project_issue_return_item_id  UUID           PRIMARY KEY,
    intra_project_issue_return_id       UUID           NOT NULL REFERENCES intra_project_issue_return (intra_project_issue_return_id) ON DELETE CASCADE,
    intra_project_issue_item_id         UUID           REFERENCES intra_project_issue_item (intra_project_issue_item_id),
    item_code                           VARCHAR(50)    NOT NULL,
    description                         VARCHAR(200),
    size                                VARCHAR(50),
    uom_id                              INTEGER        REFERENCES uom (uom_id),
    quantity                            NUMERIC(12,2)  NOT NULL,
    unit_price                          NUMERIC(12,2),
    amount                              NUMERIC(14,2),
    remarks                             VARCHAR(200)
);
CREATE INDEX IF NOT EXISTS idx_intra_project_issue_return_item_return_id ON intra_project_issue_return_item (intra_project_issue_return_id);
CREATE INDEX IF NOT EXISTS idx_intra_project_issue_return_item_issue_item_id ON intra_project_issue_return_item (intra_project_issue_item_id);
