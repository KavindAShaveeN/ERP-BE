-- job_card.project_code used to mean "the project that requested this job" (set from
-- the service request / picked on the create form). That's now renamed to
-- requesting_project_code, and project_code becomes a new column meaning "the project
-- the creating user was logged into" (their active project at creation time) — the
-- project whose store issues/returns for this job card operate against (see
-- JobIssueItemService / JobIssueItemReturnService, which already read job_card.project_code
-- for that). Run manually against the application database (this codebase has no
-- Hibernate ddl-auto / Flyway — see the other scripts in this folder).

ALTER TABLE job_card RENAME COLUMN project_code TO requesting_project_code;

ALTER TABLE job_card
    ADD COLUMN IF NOT EXISTS project_code VARCHAR(60);
