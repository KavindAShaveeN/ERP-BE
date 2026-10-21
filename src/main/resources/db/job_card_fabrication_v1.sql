-- Fabrication job cards: a job card with job_type 'Fabrication' has no asset (asset_code stays NULL)
-- and instead records the finished products it produced. When the job is finished each output is
-- credited to the job's project store as a new stock batch (source JOB_CARD_OUTPUT).
CREATE TABLE IF NOT EXISTS job_card_output (
	job_card_output_id uuid DEFAULT gen_random_uuid() NOT NULL,
	job_card_id uuid NOT NULL,
	item_code varchar(100) NOT NULL,
	description varchar(500) NULL,
	quantity numeric(15, 2) NOT NULL,
	unit_cost numeric(15, 4) NULL,
	posted_at timestamp NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT job_card_output_pkey PRIMARY KEY (job_card_output_id),
	CONSTRAINT job_card_output_quantity_check CHECK (quantity > 0),
	CONSTRAINT fk_job_card_output_job_card FOREIGN KEY (job_card_id) REFERENCES job_card(job_card_id) ON DELETE CASCADE,
	CONSTRAINT fk_job_card_output_item_code FOREIGN KEY (item_code) REFERENCES item_code(item_code_code)
);
CREATE INDEX IF NOT EXISTS idx_job_card_output_job_card ON job_card_output (job_card_id);

-- Allow the new batch source (JOB_CARD_ITEM_RETURN is already written by job card item returns).
ALTER TABLE stock_batch DROP CONSTRAINT IF EXISTS stock_batch_source_type_check;
ALTER TABLE stock_batch ADD CONSTRAINT stock_batch_source_type_check CHECK (source_type::text = ANY (ARRAY[
	'SUPPLIER_GRN', 'INTERNAL_GRN', 'STOCK_RETURN', 'STOCK_ADJUSTMENT', 'INTRA_PROJECT_ISSUE_RETURN',
	'PLANT_PRODUCTION', 'CUT_RETURN', 'JOB_CARD_ITEM_RETURN', 'JOB_CARD_OUTPUT'
]::text[]));
