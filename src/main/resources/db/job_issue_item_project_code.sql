-- Job cards can be worked on by a user whose currently active project differs
-- from the job card's own project (e.g. HQ/shared staff). Issuing and
-- returning items must move stock in *that user's active project's* store,
-- not necessarily the job card's own project — see JobIssueItemService /
-- JobIssueItemReturnService, which now read this column instead of always
-- using the job card's project_code. Run manually against the application
-- database (this codebase has no Hibernate ddl-auto / Flyway — see the other
-- scripts in this folder).

ALTER TABLE job_issue_item
    ADD COLUMN IF NOT EXISTS project_code VARCHAR(60);

ALTER TABLE job_issue_item_return
    ADD COLUMN IF NOT EXISTS project_code VARCHAR(60);
