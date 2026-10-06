-- Links a Service Receive Note back to the Service Request that originated its job card, so the
-- requesting project can track what it asked the workshop for against what has actually come
-- back. Nullable — older/edge-case job cards may not have a service_request_id. Run manually
-- against the application database.

ALTER TABLE service_receive_note
    ADD COLUMN IF NOT EXISTS service_request_id UUID REFERENCES service_request (service_request_id);
