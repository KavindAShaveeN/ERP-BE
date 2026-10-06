-- Vehicle and "received by" on a Stock Return, mirroring gin.vehicle_asset_code / vehicle_no / received_by.
-- Run once manually against the ERPRR database (no Flyway). Safe to re-run.
--
--   received_by        employee code of whoever receives the goods (internal returns); no foreign key
--   vehicle_asset_code the transporting vehicle's asset code (Vehicle asset master)
--   vehicle_no         free-text mirror of the vehicle's registration number, for search/display

ALTER TABLE stock_return
    ADD COLUMN IF NOT EXISTS received_by        VARCHAR(50),
    ADD COLUMN IF NOT EXISTS vehicle_asset_code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS vehicle_no         VARCHAR(50);
