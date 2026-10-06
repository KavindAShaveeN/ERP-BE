-- Preventive service plans, one template per asset type code (the prefix of asset_code, e.g.
-- DT for DT-118). A template is a list of services (e.g. 500 km, 2000 km, 5000 km), each with
-- the parts/oils it needs. When cycle_length is set, the whole pattern repeats every
-- cycle_length (services at 500/2000/5000 with a 5000 cycle also fall at 5500/7000/10000...).
-- Which services were performed on a Service job card is recorded in job_card_service and
-- drives "last done" / "next due". Run manually against the application database.
--
-- The first version of this feature (service_schedule_task / job_card_service_task) was never
-- released; these two drops just clear it out so the new structure can replace it.



CREATE TABLE IF NOT EXISTS service_schedule_template (
    template_id UUID PRIMARY KEY,
    asset_type_code VARCHAR(20) NOT NULL,
    name VARCHAR(150) NOT NULL,
    meter_unit VARCHAR(10) NOT NULL CHECK (meter_unit IN ('KM', 'HOURS')),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (asset_type_code, meter_unit)
);

ALTER TABLE service_schedule_template
    ADD COLUMN IF NOT EXISTS cycle_length NUMERIC(12, 2) CHECK (cycle_length > 0);

CREATE TABLE IF NOT EXISTS service_schedule_service (
    service_id UUID PRIMARY KEY,
    template_id UUID NOT NULL REFERENCES service_schedule_template (template_id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    at_value NUMERIC(12, 2) NOT NULL CHECK (at_value > 0),
    remarks VARCHAR(500),
    sort_order INT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_service_schedule_service_template
    ON service_schedule_service (template_id);

-- A part is either picked from the item master (item_code set) or typed in by hand.
CREATE TABLE IF NOT EXISTS service_schedule_service_part (
    part_id UUID PRIMARY KEY,
    service_id UUID NOT NULL REFERENCES service_schedule_service (service_id) ON DELETE CASCADE,
    item_code VARCHAR(50),
    part_name VARCHAR(200) NOT NULL,
    quantity NUMERIC(12, 2) NOT NULL DEFAULT 1,
    unit VARCHAR(30)
);

CREATE INDEX IF NOT EXISTS idx_service_schedule_part_service
    ON service_schedule_service_part (service_id);

CREATE TABLE IF NOT EXISTS job_card_service (
    job_card_id UUID NOT NULL REFERENCES job_card (job_card_id) ON DELETE CASCADE,
    service_id UUID NOT NULL REFERENCES service_schedule_service (service_id) ON DELETE CASCADE,
    PRIMARY KEY (job_card_id, service_id)
);
