-- Adds a part number to consumable items (same name/type as asset_spare_part.part_number
-- and job_issue_item.part_number). Run this script manually against the application database.

ALTER TABLE consumable_item
    ADD COLUMN IF NOT EXISTS part_number VARCHAR(100);
