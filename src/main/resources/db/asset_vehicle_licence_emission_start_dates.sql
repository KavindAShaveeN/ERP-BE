ALTER TABLE vehicle_asset_detail ADD COLUMN IF NOT EXISTS revenue_licence_start_date DATE;
ALTER TABLE vehicle_asset_detail ADD COLUMN IF NOT EXISTS emission_test_start_date DATE;
