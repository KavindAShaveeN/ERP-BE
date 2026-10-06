-- job_issue_item_return: items previously issued to a job card (job_issue_item)
-- that are handed back to the project store. Each row references the exact
-- issue line it's returning against, so the outstanding (issued - returned)
-- quantity for that line can be computed and enforced. Plain JDBC-backed
-- table (matching the job_card family in this codebase), so it is NOT
-- created automatically by Hibernate's ddl-auto:update. Run this script
-- manually against the application database, after job_card_hierarchy_and_
-- service_request_link.sql.

CREATE TABLE IF NOT EXISTS job_issue_item_return (
    job_issue_item_return_id UUID PRIMARY KEY,
    job_card_id       UUID           NOT NULL REFERENCES job_card (job_card_id) ON DELETE CASCADE,
    job_issue_item_id UUID           NOT NULL REFERENCES job_issue_item (job_issue_item_id),
    item_code         VARCHAR(50)    NOT NULL,
    quantity          NUMERIC(14,3)  NOT NULL CHECK (quantity > 0),
    unit_price        NUMERIC(14,4),
    returned_by       VARCHAR(50)    REFERENCES employee (employee_code),
    returned_date     TIMESTAMP      NOT NULL,
    remarks           VARCHAR(200),
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_job_issue_item_return_job_card_id ON job_issue_item_return (job_card_id);
CREATE INDEX IF NOT EXISTS idx_job_issue_item_return_job_issue_item_id ON job_issue_item_return (job_issue_item_id);
