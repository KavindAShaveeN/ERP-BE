-- A lightweight document auto-created on the requesting project's side whenever a job card's
-- asset is delivered back (job_status_type.is_terminal_status). One header row per job card
-- delivery, with the returned asset/item embedded directly on it (no separate line-item table
-- — a job card only ever returns the single asset/item it was raised against, mirroring the
-- asset_code/item_code duality added in three_p_service_item_link.sql). Run manually against
-- the application database.

CREATE TABLE IF NOT EXISTS service_receive_note (
    service_receive_note_id UUID PRIMARY KEY,
    service_receive_note_code VARCHAR(50) NOT NULL UNIQUE,
    job_card_id UUID NOT NULL REFERENCES job_card (job_card_id),
    job_card_code VARCHAR(50) NOT NULL,
    requesting_project_code VARCHAR(50) NOT NULL,
    asset_code VARCHAR(50),
    item_code VARCHAR(50),
    quantity NUMERIC(12, 2) NOT NULL DEFAULT 1,
    received_date TIMESTAMP NOT NULL,
    received_by VARCHAR(100),
    remarks VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_service_receive_note_job_card
    ON service_receive_note (job_card_id);

CREATE INDEX IF NOT EXISTS idx_service_receive_note_requesting_project
    ON service_receive_note (requesting_project_code);
