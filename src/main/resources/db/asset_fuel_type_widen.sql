-- fuel_type was VARCHAR(20), which only ever fit a bare fuel name. The Asset UI now
-- stores it as "<item code> - <description>" (e.g. "F001 - Automotive Diesel Oil"),
-- which overflowed on save with "value too long for type character varying(20)".
-- Widen it to match the other free-text description columns on these tables.
-- Guarded per-table since only the classes actually registered so far (e.g.
-- vehicle_asset_detail) will exist in a given database.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'vehicle_asset_detail') THEN
        ALTER TABLE vehicle_asset_detail ALTER COLUMN fuel_type TYPE VARCHAR(255);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'plant_equipment_asset_detail') THEN
        ALTER TABLE plant_equipment_asset_detail ALTER COLUMN fuel_type TYPE VARCHAR(255);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'power_tool_asset_detail') THEN
        ALTER TABLE power_tool_asset_detail ALTER COLUMN fuel_type TYPE VARCHAR(255);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'machinery_asset_detail') THEN
        ALTER TABLE machinery_asset_detail ALTER COLUMN fuel_type TYPE VARCHAR(255);
    END IF;
END $$;
