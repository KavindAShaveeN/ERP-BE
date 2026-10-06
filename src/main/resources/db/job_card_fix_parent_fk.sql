-- The live database ended up with a FK constraint on job_card.parent_job_card_id
-- pointing at a non-existent table "parent_job_card" (should be job_card itself,
-- self-referencing -- see job_card_hierarchy_and_service_request_link.sql, which
-- was already correct). This drops the bad constraint, if present, and recreates
-- it correctly. Safe to run multiple times. Run manually against the application
-- database.

ALTER TABLE job_card DROP CONSTRAINT IF EXISTS fk_job_card_parent_job_card;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'job_card' AND constraint_name = 'job_card_parent_job_card_id_fkey'
    ) THEN
        ALTER TABLE job_card
            ADD CONSTRAINT job_card_parent_job_card_id_fkey
            FOREIGN KEY (parent_job_card_id) REFERENCES job_card (job_card_id) ON DELETE CASCADE;
    END IF;
END $$;
