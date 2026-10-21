-- Third-party service via purchasing: request groups, issue mode, provider code, service description.
ALTER TABLE three_p_service ADD COLUMN IF NOT EXISTS request_group_id uuid;
ALTER TABLE three_p_service ADD COLUMN IF NOT EXISTS issue_mode varchar(20) NOT NULL DEFAULT 'DIRECT';
ALTER TABLE three_p_service ADD COLUMN IF NOT EXISTS service_provider_code varchar(50);
ALTER TABLE three_p_service ADD COLUMN IF NOT EXISTS description text;

CREATE INDEX IF NOT EXISTS idx_three_p_service_status ON three_p_service (status);
CREATE INDEX IF NOT EXISTS idx_three_p_service_po_code ON three_p_service (po_code);

-- status values are plain strings: Requested, PO Created, Pending, In Progress, Completed, Cancelled ...
