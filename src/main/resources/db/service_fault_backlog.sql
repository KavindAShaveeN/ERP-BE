-- MANUAL migration: run once before deploying the corresponding backend/frontend.
-- Do not run application startup to apply this script. No automatic migration is registered.
-- Back up the database first. Existing links are retained; no repairs are inferred.
BEGIN;
CREATE TABLE IF NOT EXISTS fault_type (
    fault_code varchar(60) PRIMARY KEY,
    label varchar(500) NOT NULL UNIQUE,
    category varchar(100) NOT NULL,
    is_active boolean NOT NULL DEFAULT true
);
INSERT INTO fault_type(fault_code,label,category) VALUES
('FAULT_001','AC not work','Electrical and cabin'),
('FAULT_002','Air leak','Attachments and other'),
('FAULT_003','Arm boom bucket jack seal leak','Hydraulics'),
('FAULT_004','Arm boom slow','Hydraulics'),
('FAULT_005','Bucket teeth worn out','Attachments and other'),
('FAULT_006','Circle blade defects','Attachments and other'),
('FAULT_007','Clutch worn out','Brakes and drivetrain'),
('FAULT_008','Coupling motor damage','Electrical and cabin'),
('FAULT_009','Crank seal leak','Engine'),
('FAULT_010','Drum worn out','Attachments and other'),
('FAULT_011','Ducon seal leak','Hydraulics'),
('FAULT_012','Engine heating','Engine'),
('FAULT_013','Engine oil leaking','Engine'),
('FAULT_014','Ex-dig pad worn out','Attachments and other'),
('FAULT_015','Fan belt damage','Engine'),
('FAULT_016','Gear tighten','Brakes and drivetrain'),
('FAULT_017','HYD heating','Hydraulics'),
('FAULT_018','HYD leaking','Hydraulics'),
('FAULT_019','Leaf spring brushes damage','Tyres and undercarriage'),
('FAULT_020','Less brake','Brakes and drivetrain'),
('FAULT_021','Lights not work','Electrical and cabin'),
('FAULT_022','Lost bucket jack pressure','Hydraulics'),
('FAULT_023','No battery charging','Electrical and cabin'),
('FAULT_024','No compaction','Attachments and other'),
('FAULT_025','No parking brake','Brakes and drivetrain'),
('FAULT_026','No pulling power','Brakes and drivetrain'),
('FAULT_027','Propeller shaft abnormal noise','Brakes and drivetrain'),
('FAULT_028','PTO leak','Brakes and drivetrain'),
('FAULT_029','Ripper defect','Attachments and other'),
('FAULT_030','Scrapers damage','Attachments and other'),
('FAULT_031','Shims worn out','Attachments and other'),
('FAULT_032','Shock not work','Tyres and undercarriage'),
('FAULT_033','Stabilizer jack pad worn out','Hydraulics'),
('FAULT_034','Stepping motor defect','Electrical and cabin'),
('FAULT_035','Swing grease seal leak','Hydraulics'),
('FAULT_036','Taking the load engine slow','Engine'),
('FAULT_037','Tandem defect','Brakes and drivetrain'),
('FAULT_038','Tire worn out','Tyres and undercarriage'),
('FAULT_039','Track shifting','Tyres and undercarriage'),
('FAULT_040','White smoke','Engine'),
('FAULT_041','Winder''s not work','Electrical and cabin')
ON CONFLICT (fault_code) DO NOTHING;

CREATE TABLE IF NOT EXISTS service_request_fault (
    request_fault_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    service_request_id uuid NOT NULL REFERENCES service_request(service_request_id),
    fault_code varchar(60) REFERENCES fault_type(fault_code),
    description text NOT NULL,
    review_required boolean NOT NULL DEFAULT false,
    reviewed_resolved boolean NOT NULL DEFAULT false,
    review_note text,
    reviewed_at timestamp,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(service_request_id,description)
);
ALTER TABLE defect ADD COLUMN IF NOT EXISTS fault_code varchar(60) REFERENCES fault_type(fault_code);
ALTER TABLE defect ADD COLUMN IF NOT EXISTS outcome varchar(20) NOT NULL DEFAULT 'ASSIGNED';
ALTER TABLE defect ADD COLUMN IF NOT EXISTS outcome_reason text;
ALTER TABLE defect ADD COLUMN IF NOT EXISTS outcome_at timestamp;
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='defect_fault_outcome_check' AND conrelid='defect'::regclass) THEN
        ALTER TABLE defect ADD CONSTRAINT defect_fault_outcome_check CHECK(outcome IN ('ASSIGNED','RESOLVED','DEFERRED'));
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS job_defect_source (
    defect_id uuid NOT NULL REFERENCES defect(defect_id),
    request_fault_id uuid NOT NULL REFERENCES service_request_fault(request_fault_id),
    assigned_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    released_at timestamp,
    PRIMARY KEY(defect_id,request_fault_id)
);
-- A resolved source remains claimed; a deferred source is released and can be assigned again.
CREATE UNIQUE INDEX IF NOT EXISTS uq_request_fault_claim
    ON job_defect_source(request_fault_id) WHERE released_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_request_fault_request ON service_request_fault(service_request_id);
CREATE INDEX IF NOT EXISTS idx_request_asset_approved ON service_request(asset_code,is_approved);
CREATE INDEX IF NOT EXISTS idx_defect_job ON defect(job_card_id);
CREATE INDEX IF NOT EXISTS idx_fault_source_report ON job_defect_source(request_fault_id);
CREATE INDEX IF NOT EXISTS idx_job_asset_open ON job_card(asset_code) WHERE NOT COALESCE(is_finished,false) AND NOT COALESCE(is_delivered,false);
-- Text remains for compatibility, but no longer limits the number of selected faults.
ALTER TABLE service_request ALTER COLUMN maintenance_works TYPE text;

-- Use the same comma delimiter as the current UI. Unknown labels are retained for review.
-- Requests already linked to any job are REVIEW, never automatically pending or repaired.
INSERT INTO service_request_fault(service_request_id,fault_code,description,review_required)
SELECT DISTINCT r.service_request_id,t.fault_code,btrim(parts.description),
    t.fault_code IS NULL OR EXISTS(SELECT 1 FROM job_card j WHERE j.service_request_id=r.service_request_id)
FROM service_request r
CROSS JOIN LATERAL regexp_split_to_table(COALESCE(r.maintenance_works,''),',') parts(description)
LEFT JOIN fault_type t ON t.label=btrim(parts.description)
WHERE btrim(parts.description)<>''
ON CONFLICT(service_request_id,description) DO NOTHING;
-- Empty legacy requests must also stay visible for review.
INSERT INTO service_request_fault(service_request_id,description,review_required)
SELECT r.service_request_id,'Legacy request: fault description missing',true
FROM service_request r WHERE NOT EXISTS(SELECT 1 FROM service_request_fault f WHERE f.service_request_id=r.service_request_id)
ON CONFLICT(service_request_id,description) DO NOTHING;
COMMIT;

-- Read-only verification / manual review report:
SELECT count(*) AS fault_types FROM fault_type;
SELECT review_required,count(*) AS reported_faults FROM service_request_fault GROUP BY review_required;
SELECT j.job_card_code,j.asset_code AS job_asset,r.service_request_code,r.asset_code AS request_asset
FROM job_card j JOIN service_request r USING(service_request_id)
WHERE j.asset_code IS DISTINCT FROM r.asset_code;
-- Review unknown labels before releasing them to the backlog.
SELECT DISTINCT description FROM service_request_fault WHERE fault_code IS NULL;
