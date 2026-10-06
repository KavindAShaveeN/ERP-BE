-- Recipe outputs can now be flagged as waste (no stock impact), matching the
-- is_waste flag already tracked on actual production outputs. Purely additive.

ALTER TABLE plant_recipe_output ADD COLUMN IF NOT EXISTS is_waste BOOLEAN DEFAULT false;
