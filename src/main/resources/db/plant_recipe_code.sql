-- User-entered recipe code (e.g. "GR25-BATCH") shown alongside recipe name so recipes can
-- be looked up/selected by a short code, same as item codes. Purely additive.

ALTER TABLE plant_recipe ADD COLUMN IF NOT EXISTS recipe_code VARCHAR(40);
