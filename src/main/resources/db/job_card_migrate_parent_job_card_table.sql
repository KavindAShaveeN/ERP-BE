-- A coworker built a separate, disconnected "parent_job_card" table (its own PK,
-- own columns, no FK to job_card) instead of using job_card's own self-reference
-- (parent_job_card_id -> job_card.job_card_id, see
-- job_card_hierarchy_and_service_request_link.sql). 4 existing job_card rows
-- (JC-0002, JC-0003, JC-0004, RR-JOB-12543) were then pointed at that table's one
-- row (id 39367392-1cb9-4de8-812f-ca7b2c799570) via parent_job_card_id, which is
-- why job_card_fix_parent_fk.sql's FK recreation failed: that id doesn't exist in
-- job_card.
--
-- This migrates that one row into job_card itself -- reusing the SAME id, so the
-- 4 existing parent_job_card_id references become valid without touching them --
-- then (re)creates the correct self-referencing FK, then drops the now-redundant
-- parent_job_card table. Run manually against the application database, after
-- job_card_fix_parent_fk.sql (whose constraint-add half will have failed and can
-- be ignored -- this script redoes it).

BEGIN;

INSERT INTO job_card (
    job_card_id, job_card_code, created_date, job_status_type_id, updated_at,
    remarks, is_finished, cost, job_type, parent_job_card_id, department,
    service_request_id, requesting_project_code, asset_code, make, type,
    meter_reading, informed_by, phone_number, is_delivered, project_code
)
SELECT
    parent_job_card_id, 'JC-0007', created_at, 4, updated_at,
    NULL, is_finished, total_cost, 'Breakdown', NULL, NULL,
    service_request_id, project_code, asset_code, make, type,
    meter_reading, informed_by, phone_number, is_delivered, project_code
FROM parent_job_card
WHERE parent_job_card_id = '39367392-1cb9-4de8-812f-ca7b2c799570'
ON CONFLICT (job_card_id) DO NOTHING;

ALTER TABLE job_card DROP CONSTRAINT IF EXISTS job_card_parent_job_card_id_fkey;
ALTER TABLE job_card
    ADD CONSTRAINT job_card_parent_job_card_id_fkey
    FOREIGN KEY (parent_job_card_id) REFERENCES job_card (job_card_id) ON DELETE CASCADE;

DROP TABLE IF EXISTS parent_job_card;

COMMIT;
