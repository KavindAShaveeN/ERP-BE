-- Extends intra_project_issue / intra_project_issue_item to support the same
-- 4 issue types GIN already has (General / Subcontractor / Personal / Loan),
-- issued from a project's site store to a subcontractor, an employee, or an
-- outside person -- not only to a phase within the same project.
-- Plain JDBC-backed table (see intra_project_issue_schema.sql), not managed by
-- Hibernate ddl-auto:update. Run this script manually against the application
-- database, after intra_project_issue_schema.sql and intra_project_issue_item_pricing.sql.

ALTER TABLE intra_project_issue
    ADD COLUMN IF NOT EXISTS issue_type VARCHAR(20) NOT NULL DEFAULT 'GENERAL';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'intra_project_issue' AND constraint_name = 'intra_project_issue_type_check'
    ) THEN
        ALTER TABLE intra_project_issue
            ADD CONSTRAINT intra_project_issue_type_check
            CHECK (issue_type IN ('GENERAL', 'SUBCONTRACTOR', 'PERSONAL', 'LOAN'));
    END IF;
END $$;

-- Only GENERAL issues go to a project phase with a receiving-employee
-- confirmation, so received_by is optional for the other 3 types
-- (received_project_phase_id is already nullable — see intra_project_issue_schema.sql).
ALTER TABLE intra_project_issue ALTER COLUMN received_by DROP NOT NULL;

-- SUBCONTRACTOR: who the goods are issued to. Subcontractors have no system
-- login, so there is no "received by" confirmation for this type.
ALTER TABLE intra_project_issue
    ADD COLUMN IF NOT EXISTS subcontractor_id INTEGER REFERENCES subcontractor (subcontractor_id);

-- LOAN: the recipient may be an outside person instead of an employee
-- (received_by covers the employee case, same as PERSONAL).
ALTER TABLE intra_project_issue
    ADD COLUMN IF NOT EXISTS receiver_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS receiver_nic VARCHAR(30),
    ADD COLUMN IF NOT EXISTS expected_return_date DATE;

CREATE INDEX IF NOT EXISTS idx_intra_project_issue_issue_type ON intra_project_issue (issue_type);
CREATE INDEX IF NOT EXISTS idx_intra_project_issue_subcontractor_id ON intra_project_issue (subcontractor_id);

-- Deductible / non-deductible / returnable classification per line, the same
-- issue_item_type lookup GIN items already use.
ALTER TABLE intra_project_issue_item
    ADD COLUMN IF NOT EXISTS issue_item_type_id INTEGER REFERENCES issue_item_type (issue_item_type_id);
