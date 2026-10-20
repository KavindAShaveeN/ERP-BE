-- Asset coupling: assets that must always travel together (e.g. water bowser <-> water pump).
-- A couple is symmetric and stored once per pair (asset_code_a < asset_code_b). Safe to re-run.
CREATE TABLE IF NOT EXISTS asset_couple (
	couple_id BIGSERIAL PRIMARY KEY,
	asset_code_a VARCHAR(20) NOT NULL REFERENCES asset(asset_code) ON DELETE CASCADE,
	asset_code_b VARCHAR(20) NOT NULL REFERENCES asset(asset_code) ON DELETE CASCADE,
	created_at TIMESTAMP DEFAULT now(),
	CONSTRAINT chk_asset_couple_order CHECK (asset_code_a < asset_code_b),
	CONSTRAINT uq_asset_couple_pair UNIQUE (asset_code_a, asset_code_b)
);

CREATE INDEX IF NOT EXISTS idx_asset_couple_b ON asset_couple (asset_code_b);
