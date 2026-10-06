-- Adds job card hierarchy (a job card can be a "sub job card" of another,
-- one level deep) and an optional link from a job card back to the service
-- request that triggered it, plus a human-readable service request code
-- (SR-{projectCode}-{sequence}, generated client-side the same way job card
-- codes are). Run manually against the application database (this codebase
-- has no Hibernate ddl-auto / Flyway — see the other scripts in this folder).

ALTER TABLE job_card
    ADD COLUMN IF NOT EXISTS parent_job_card_id UUID NULL REFERENCES job_card (job_card_id) ON DELETE CASCADE,
    ADD COLUMN IF NOT EXISTS service_request_id UUID NULL REFERENCES service_request (service_request_id);

CREATE INDEX IF NOT EXISTS idx_job_card_parent_job_card_id ON job_card (parent_job_card_id);
CREATE INDEX IF NOT EXISTS idx_job_card_service_request_id ON job_card (service_request_id);

ALTER TABLE service_request
    ADD COLUMN IF NOT EXISTS service_request_code VARCHAR(60);

CREATE UNIQUE INDEX IF NOT EXISTS uq_service_request_code
    ON service_request (service_request_code)
    WHERE service_request_code IS NOT NULL;
