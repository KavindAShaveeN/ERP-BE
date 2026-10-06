-- Links an asset (vehicle, machine, ...) to the coded inventory/consumable items that
-- are its spare parts, e.g. Truck V-C-002 -> "Air filter" -> item code FLT-AIR-001.
-- Many-to-many: one item can be a spare part of many assets.
--
-- PREREQUISITE: the "asset" and "item_code" tables (see asset_vehicle_schema.sql and
-- item_migration_v2_id_primary_keys.sql).
--
-- Safe to re-run: every statement is guarded (IF NOT EXISTS).

CREATE TABLE IF NOT EXISTS asset_spare_part (
    asset_spare_part_id  BIGSERIAL     PRIMARY KEY,
    asset_code           VARCHAR(20)   NOT NULL REFERENCES asset (asset_code) ON DELETE CASCADE,
    item_code_id         BIGINT        NOT NULL REFERENCES item_code (item_code_id) ON DELETE RESTRICT,
    part_role            VARCHAR(255)  NOT NULL,   -- free-text description of the part
    part_number          VARCHAR(100),
    serial_number        VARCHAR(100),
    quantity_per_unit    NUMERIC(12, 3) NOT NULL DEFAULT 1,
    remarks              VARCHAR(500),
    is_active            BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP     NOT NULL DEFAULT now()
);
-- For databases where the table already exists from the first version of this script.
ALTER TABLE asset_spare_part ADD COLUMN IF NOT EXISTS part_number   VARCHAR(100);
ALTER TABLE asset_spare_part ADD COLUMN IF NOT EXISTS serial_number VARCHAR(100);
ALTER TABLE asset_spare_part ALTER COLUMN part_role TYPE VARCHAR(255);
ALTER TABLE asset_spare_part DROP CONSTRAINT IF EXISTS asset_spare_part_unique;
-- Same item may appear twice on an asset (e.g. two batteries) as long as the serial numbers differ.
CREATE UNIQUE INDEX IF NOT EXISTS asset_spare_part_unique
    ON asset_spare_part (asset_code, item_code_id, part_role, COALESCE(serial_number, ''));

CREATE INDEX IF NOT EXISTS idx_asset_spare_part_asset ON asset_spare_part (asset_code);
CREATE INDEX IF NOT EXISTS idx_asset_spare_part_item  ON asset_spare_part (item_code_id);
