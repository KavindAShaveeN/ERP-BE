-- Dummy but practical item-code seed data for a construction company.
-- Covers both Consumable (item_type_id = 2) and Inventory (item_type_id = 3) items,
-- built through the full hierarchy: item_category -> item_subcategory ->
-- item_subsubcategory -> item_brand -> item_model -> item_code, then a matching
-- consumable_item or inventory_item row.
--
-- Column/table names here match what the live application actually reads/writes
-- (ItemCategoryRepository, ItemSubCategoryRepository, ItemSubSubCategoryRepository,
-- ItemBrandRepository, ItemModelRepository, ItemCodeRepository, ConsumableItemRepository,
-- InventoryItemRepository) -- NOT the older item_schema.sql / item_code_consumable_item_schema.sql
-- files in this folder, whose column names (id/code/name) are stale and no longer used by
-- the app's JDBC repositories.
--
-- item_type_id convention used by the frontend (src/pages/item-management/*):
--   1 = Asset, 2 = Consumable, 3 = Inventory
--
-- Safe to re-run: every INSERT is guarded with WHERE NOT EXISTS, mirroring the
-- existsByXxx() checks the application itself does before inserting.

BEGIN;

-- ============================================================
-- UOM (units of measure) - insert only if missing
-- ============================================================
INSERT INTO uom (uom_name)
SELECT v.name FROM (VALUES
    ('Bag'), ('Cubic Meter'), ('Pack'), ('Pair'),
    ('Each'), ('Box'), ('Drum'), ('Length')
) AS v(name)
WHERE NOT EXISTS (SELECT 1 FROM uom u WHERE u.uom_name = v.name);

-- ============================================================
-- CONSUMABLE ITEMS (item_type_id = 2)
-- ============================================================

-- 1) Ordinary Portland Cement --------------------------------------------------
INSERT INTO item_category (item_type_id, item_category_code, item_category_name, code_slug, name_slug)
SELECT 2, 'BM', 'Building Materials',
       regexp_replace(lower(trim('BM')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Building Materials')), '[^a-z0-9]', '', 'g')
WHERE NOT EXISTS (
    SELECT 1 FROM item_category WHERE item_type_id = 2 AND item_category_code = 'BM'
);

INSERT INTO item_subcategory (item_subcategory_code, item_subcategory_name, item_category_id, code_slug, name_slug)
SELECT 'CEM', 'Cement & Concrete', c.item_category_id,
       regexp_replace(lower(trim('CEM')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Cement & Concrete')), '[^a-z0-9]', '', 'g')
FROM item_category c
WHERE c.item_type_id = 2 AND c.item_category_code = 'BM'
  AND NOT EXISTS (
      SELECT 1 FROM item_subcategory s WHERE s.item_category_id = c.item_category_id AND s.item_subcategory_code = 'CEM'
  );

INSERT INTO item_subsubcategory (item_subsubcategory_code, item_subsubcategory_name, item_subcategory_id, code_slug, name_slug)
SELECT 'CEM-GEN', 'Cement', sc.item_subcategory_id,
       regexp_replace(lower(trim('CEM-GEN')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('CEM-GEN')), '[^a-z0-9]', '', 'g')
FROM item_subcategory sc
WHERE sc.item_subcategory_code = 'CEM'
  AND NOT EXISTS (
      SELECT 1 FROM item_subsubcategory ssc WHERE ssc.item_subcategory_id = sc.item_subcategory_id AND ssc.item_subsubcategory_code = 'CEM-GEN'
  );

INSERT INTO item_brand (item_brand_code, item_brand_name, item_subsubcategory_id, code_slug, name_slug)
SELECT 'HOLCIM', 'Holcim', ssc.item_subsubcategory_id,
       regexp_replace(lower(trim('HOLCIM')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Holcim')), '[^a-z0-9]', '', 'g')
FROM item_subsubcategory ssc
WHERE ssc.item_subsubcategory_code = 'CEM-GEN'
  AND NOT EXISTS (
      SELECT 1 FROM item_brand b WHERE b.item_subsubcategory_id = ssc.item_subsubcategory_id AND b.item_brand_code = 'HOLCIM'
  );

INSERT INTO item_model (item_model_code, item_model_name, item_brand_id, code_slug, name_slug)
SELECT 'OPC425', 'OPC 42.5N 50kg', br.item_brand_id,
       regexp_replace(lower(trim('OPC425')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('OPC 42.5N 50kg')), '[^a-z0-9]', '', 'g')
FROM item_brand br
WHERE br.item_brand_code = 'HOLCIM'
  AND NOT EXISTS (
      SELECT 1 FROM item_model m WHERE m.item_brand_id = br.item_brand_id AND m.item_model_code = 'OPC425'
  );

INSERT INTO item_code (
    item_code_code, item_code_name, item_category_id, item_subcategory_id,
    item_subSubCategory_id, item_brand_id, item_model_id
)
SELECT 'BM-CEM-001', 'Ordinary Portland Cement 42.5N 50kg Bag',
       c.item_category_id, sc.item_subcategory_id, ssc.item_subsubcategory_id, br.item_brand_id, m.item_model_id
FROM item_category c
JOIN item_subcategory sc ON sc.item_category_id = c.item_category_id AND sc.item_subcategory_code = 'CEM'
JOIN item_subsubcategory ssc ON ssc.item_subcategory_id = sc.item_subcategory_id AND ssc.item_subsubcategory_code = 'CEM-GEN'
JOIN item_brand br ON br.item_subsubcategory_id = ssc.item_subsubcategory_id AND br.item_brand_code = 'HOLCIM'
JOIN item_model m ON m.item_brand_id = br.item_brand_id AND m.item_model_code = 'OPC425'
WHERE c.item_type_id = 2 AND c.item_category_code = 'BM'
  AND NOT EXISTS (SELECT 1 FROM item_code ic WHERE ic.item_code_code = 'BM-CEM-001');

INSERT INTO consumable_item (item_code_id, uom_id, unit_price, minimal_quantity, current_stock)
SELECT ic.item_code_id, u.uom_id, 1850.00, 100, 250
FROM item_code ic, uom u
WHERE ic.item_code_code = 'BM-CEM-001' AND u.uom_name = 'Bag'
  AND NOT EXISTS (SELECT 1 FROM consumable_item ci WHERE ci.item_code_id = ic.item_code_id);


-- 2) River Sand -----------------------------------------------------------------
INSERT INTO item_subcategory (item_subcategory_code, item_subcategory_name, item_category_id, code_slug, name_slug)
SELECT 'AGG', 'Aggregates', c.item_category_id,
       regexp_replace(lower(trim('AGG')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Aggregates')), '[^a-z0-9]', '', 'g')
FROM item_category c
WHERE c.item_type_id = 2 AND c.item_category_code = 'BM'
  AND NOT EXISTS (
      SELECT 1 FROM item_subcategory s WHERE s.item_category_id = c.item_category_id AND s.item_subcategory_code = 'AGG'
  );

INSERT INTO item_subsubcategory (item_subsubcategory_code, item_subsubcategory_name, item_subcategory_id, code_slug, name_slug)
SELECT 'SAND', 'Sand', sc.item_subcategory_id,
       regexp_replace(lower(trim('SAND')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Sand')), '[^a-z0-9]', '', 'g')
FROM item_subcategory sc
WHERE sc.item_subcategory_code = 'AGG'
  AND NOT EXISTS (
      SELECT 1 FROM item_subsubcategory ssc WHERE ssc.item_subcategory_id = sc.item_subcategory_id AND ssc.item_subsubcategory_code = 'SAND'
  );

INSERT INTO item_brand (item_brand_code, item_brand_name, item_subsubcategory_id, code_slug, name_slug)
SELECT 'LOCAL', 'Local Supplier', ssc.item_subsubcategory_id,
       regexp_replace(lower(trim('LOCAL')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Local Supplier')), '[^a-z0-9]', '', 'g')
FROM item_subsubcategory ssc
WHERE ssc.item_subsubcategory_code = 'SAND'
  AND NOT EXISTS (
      SELECT 1 FROM item_brand b WHERE b.item_subsubcategory_id = ssc.item_subsubcategory_id AND b.item_brand_code = 'LOCAL'
  );

INSERT INTO item_model (item_model_code, item_model_name, item_brand_id, code_slug, name_slug)
SELECT 'RSAND', 'River Sand - Graded', br.item_brand_id,
       regexp_replace(lower(trim('RSAND')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('River Sand - Graded')), '[^a-z0-9]', '', 'g')
FROM item_brand br
WHERE br.item_brand_code = 'LOCAL'
  AND NOT EXISTS (
      SELECT 1 FROM item_model m WHERE m.item_brand_id = br.item_brand_id AND m.item_model_code = 'RSAND'
  );

INSERT INTO item_code (
    item_code_code, item_code_name, item_category_id, item_subcategory_id,
    item_subSubCategory_id, item_brand_id, item_model_id
)
SELECT 'BM-SAND-001', 'River Sand (Graded) - per Cubic Meter',
       c.item_category_id, sc.item_subcategory_id, ssc.item_subsubcategory_id, br.item_brand_id, m.item_model_id
FROM item_category c
JOIN item_subcategory sc ON sc.item_category_id = c.item_category_id AND sc.item_subcategory_code = 'AGG'
JOIN item_subsubcategory ssc ON ssc.item_subcategory_id = sc.item_subcategory_id AND ssc.item_subsubcategory_code = 'SAND'
JOIN item_brand br ON br.item_subsubcategory_id = ssc.item_subsubcategory_id AND br.item_brand_code = 'LOCAL'
JOIN item_model m ON m.item_brand_id = br.item_brand_id AND m.item_model_code = 'RSAND'
WHERE c.item_type_id = 2 AND c.item_category_code = 'BM'
  AND NOT EXISTS (SELECT 1 FROM item_code ic WHERE ic.item_code_code = 'BM-SAND-001');

INSERT INTO consumable_item (item_code_id, uom_id, unit_price, minimal_quantity, current_stock)
SELECT ic.item_code_id, u.uom_id, 9500.00, 5, 20
FROM item_code ic, uom u
WHERE ic.item_code_code = 'BM-SAND-001' AND u.uom_name = 'Cubic Meter'
  AND NOT EXISTS (SELECT 1 FROM consumable_item ci WHERE ci.item_code_id = ic.item_code_id);


-- 3) Welding Electrodes ----------------------------------------------------------
INSERT INTO item_category (item_type_id, item_category_code, item_category_name, code_slug, name_slug)
SELECT 2, 'WLD', 'Welding Consumables',
       regexp_replace(lower(trim('WLD')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Welding Consumables')), '[^a-z0-9]', '', 'g')
WHERE NOT EXISTS (
    SELECT 1 FROM item_category WHERE item_type_id = 2 AND item_category_code = 'WLD'
);

INSERT INTO item_subcategory (item_subcategory_code, item_subcategory_name, item_category_id, code_slug, name_slug)
SELECT 'ELEC', 'Electrodes', c.item_category_id,
       regexp_replace(lower(trim('ELEC')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Electrodes')), '[^a-z0-9]', '', 'g')
FROM item_category c
WHERE c.item_type_id = 2 AND c.item_category_code = 'WLD'
  AND NOT EXISTS (
      SELECT 1 FROM item_subcategory s WHERE s.item_category_id = c.item_category_id AND s.item_subcategory_code = 'ELEC'
  );

INSERT INTO item_subsubcategory (item_subsubcategory_code, item_subsubcategory_name, item_subcategory_id, code_slug, name_slug)
SELECT 'MMA', 'MMA Rods', sc.item_subcategory_id,
       regexp_replace(lower(trim('MMA')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('MMA Rods')), '[^a-z0-9]', '', 'g')
FROM item_subcategory sc
WHERE sc.item_subcategory_code = 'ELEC'
  AND NOT EXISTS (
      SELECT 1 FROM item_subsubcategory ssc WHERE ssc.item_subcategory_id = sc.item_subcategory_id AND ssc.item_subsubcategory_code = 'MMA'
  );

INSERT INTO item_brand (item_brand_code, item_brand_name, item_subsubcategory_id, code_slug, name_slug)
SELECT 'LINCOLN', 'Lincoln Electric', ssc.item_subsubcategory_id,
       regexp_replace(lower(trim('LINCOLN')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Lincoln Electric')), '[^a-z0-9]', '', 'g')
FROM item_subsubcategory ssc
WHERE ssc.item_subsubcategory_code = 'MMA'
  AND NOT EXISTS (
      SELECT 1 FROM item_brand b WHERE b.item_subsubcategory_id = ssc.item_subsubcategory_id AND b.item_brand_code = 'LINCOLN'
  );

INSERT INTO item_model (item_model_code, item_model_name, item_brand_id, code_slug, name_slug)
SELECT 'E601332', 'E6013 3.2mm', br.item_brand_id,
       regexp_replace(lower(trim('E601332')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('E6013 3.2mm')), '[^a-z0-9]', '', 'g')
FROM item_brand br
WHERE br.item_brand_code = 'LINCOLN'
  AND NOT EXISTS (
      SELECT 1 FROM item_model m WHERE m.item_brand_id = br.item_brand_id AND m.item_model_code = 'E601332'
  );

INSERT INTO item_code (
    item_code_code, item_code_name, item_category_id, item_subcategory_id,
    item_subSubCategory_id, item_brand_id, item_model_id
)
SELECT 'WLD-E6013-001', 'Welding Electrode E6013 3.2mm (5kg Pack)',
       c.item_category_id, sc.item_subcategory_id, ssc.item_subsubcategory_id, br.item_brand_id, m.item_model_id
FROM item_category c
JOIN item_subcategory sc ON sc.item_category_id = c.item_category_id AND sc.item_subcategory_code = 'ELEC'
JOIN item_subsubcategory ssc ON ssc.item_subcategory_id = sc.item_subcategory_id AND ssc.item_subsubcategory_code = 'MMA'
JOIN item_brand br ON br.item_subsubcategory_id = ssc.item_subsubcategory_id AND br.item_brand_code = 'LINCOLN'
JOIN item_model m ON m.item_brand_id = br.item_brand_id AND m.item_model_code = 'E601332'
WHERE c.item_type_id = 2 AND c.item_category_code = 'WLD'
  AND NOT EXISTS (SELECT 1 FROM item_code ic WHERE ic.item_code_code = 'WLD-E6013-001');

INSERT INTO consumable_item (item_code_id, uom_id, unit_price, minimal_quantity, current_stock)
SELECT ic.item_code_id, u.uom_id, 3200.00, 20, 60
FROM item_code ic, uom u
WHERE ic.item_code_code = 'WLD-E6013-001' AND u.uom_name = 'Pack'
  AND NOT EXISTS (SELECT 1 FROM consumable_item ci WHERE ci.item_code_id = ic.item_code_id);


-- 4) PPE - Work Gloves -------------------------------------------------------------
INSERT INTO item_category (item_type_id, item_category_code, item_category_name, code_slug, name_slug)
SELECT 2, 'PPE', 'PPE & Safety',
       regexp_replace(lower(trim('PPE')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('PPE & Safety')), '[^a-z0-9]', '', 'g')
WHERE NOT EXISTS (
    SELECT 1 FROM item_category WHERE item_type_id = 2 AND item_category_code = 'PPE'
);

INSERT INTO item_subcategory (item_subcategory_code, item_subcategory_name, item_category_id, code_slug, name_slug)
SELECT 'HAND', 'Hand Protection', c.item_category_id,
       regexp_replace(lower(trim('HAND')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Hand Protection')), '[^a-z0-9]', '', 'g')
FROM item_category c
WHERE c.item_type_id = 2 AND c.item_category_code = 'PPE'
  AND NOT EXISTS (
      SELECT 1 FROM item_subcategory s WHERE s.item_category_id = c.item_category_id AND s.item_subcategory_code = 'HAND'
  );

INSERT INTO item_subsubcategory (item_subsubcategory_code, item_subsubcategory_name, item_subcategory_id, code_slug, name_slug)
SELECT 'GLOVE', 'Work Gloves', sc.item_subcategory_id,
       regexp_replace(lower(trim('GLOVE')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Work Gloves')), '[^a-z0-9]', '', 'g')
FROM item_subcategory sc
WHERE sc.item_subcategory_code = 'HAND'
  AND NOT EXISTS (
      SELECT 1 FROM item_subsubcategory ssc WHERE ssc.item_subcategory_id = sc.item_subcategory_id AND ssc.item_subsubcategory_code = 'GLOVE'
  );

INSERT INTO item_brand (item_brand_code, item_brand_name, item_subsubcategory_id, code_slug, name_slug)
SELECT '3M', '3M', ssc.item_subsubcategory_id,
       regexp_replace(lower(trim('3M')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('3M')), '[^a-z0-9]', '', 'g')
FROM item_subsubcategory ssc
WHERE ssc.item_subsubcategory_code = 'GLOVE'
  AND NOT EXISTS (
      SELECT 1 FROM item_brand b WHERE b.item_subsubcategory_id = ssc.item_subsubcategory_id AND b.item_brand_code = '3M'
  );

INSERT INTO item_model (item_model_code, item_model_name, item_brand_id, code_slug, name_slug)
SELECT 'CG40', 'Comfort Grip 40', br.item_brand_id,
       regexp_replace(lower(trim('CG40')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Comfort Grip 40')), '[^a-z0-9]', '', 'g')
FROM item_brand br
WHERE br.item_brand_code = '3M'
  AND NOT EXISTS (
      SELECT 1 FROM item_model m WHERE m.item_brand_id = br.item_brand_id AND m.item_model_code = 'CG40'
  );

INSERT INTO item_code (
    item_code_code, item_code_name, item_category_id, item_subcategory_id,
    item_subSubCategory_id, item_brand_id, item_model_id
)
SELECT 'PPE-GLV-001', '3M Comfort Grip Work Gloves - Pair',
       c.item_category_id, sc.item_subcategory_id, ssc.item_subsubcategory_id, br.item_brand_id, m.item_model_id
FROM item_category c
JOIN item_subcategory sc ON sc.item_category_id = c.item_category_id AND sc.item_subcategory_code = 'HAND'
JOIN item_subsubcategory ssc ON ssc.item_subcategory_id = sc.item_subcategory_id AND ssc.item_subsubcategory_code = 'GLOVE'
JOIN item_brand br ON br.item_subsubcategory_id = ssc.item_subsubcategory_id AND br.item_brand_code = '3M'
JOIN item_model m ON m.item_brand_id = br.item_brand_id AND m.item_model_code = 'CG40'
WHERE c.item_type_id = 2 AND c.item_category_code = 'PPE'
  AND NOT EXISTS (SELECT 1 FROM item_code ic WHERE ic.item_code_code = 'PPE-GLV-001');

INSERT INTO consumable_item (item_code_id, uom_id, unit_price, minimal_quantity, current_stock)
SELECT ic.item_code_id, u.uom_id, 450.00, 50, 120
FROM item_code ic, uom u
WHERE ic.item_code_code = 'PPE-GLV-001' AND u.uom_name = 'Pair'
  AND NOT EXISTS (SELECT 1 FROM consumable_item ci WHERE ci.item_code_id = ic.item_code_id);


-- 5) Fasteners - Hex Bolts ----------------------------------------------------------
INSERT INTO item_category (item_type_id, item_category_code, item_category_name, code_slug, name_slug)
SELECT 2, 'FAS', 'Fasteners',
       regexp_replace(lower(trim('FAS')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Fasteners')), '[^a-z0-9]', '', 'g')
WHERE NOT EXISTS (
    SELECT 1 FROM item_category WHERE item_type_id = 2 AND item_category_code = 'FAS'
);

INSERT INTO item_subcategory (item_subcategory_code, item_subcategory_name, item_category_id, code_slug, name_slug)
SELECT 'BOLT', 'Bolts & Nuts', c.item_category_id,
       regexp_replace(lower(trim('BOLT')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Bolts & Nuts')), '[^a-z0-9]', '', 'g')
FROM item_category c
WHERE c.item_type_id = 2 AND c.item_category_code = 'FAS'
  AND NOT EXISTS (
      SELECT 1 FROM item_subcategory s WHERE s.item_category_id = c.item_category_id AND s.item_subcategory_code = 'BOLT'
  );

INSERT INTO item_subsubcategory (item_subsubcategory_code, item_subsubcategory_name, item_subcategory_id, code_slug, name_slug)
SELECT 'HEX', 'Hex Bolts', sc.item_subcategory_id,
       regexp_replace(lower(trim('HEX')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Hex Bolts')), '[^a-z0-9]', '', 'g')
FROM item_subcategory sc
WHERE sc.item_subcategory_code = 'BOLT'
  AND NOT EXISTS (
      SELECT 1 FROM item_subsubcategory ssc WHERE ssc.item_subcategory_id = sc.item_subcategory_id AND ssc.item_subsubcategory_code = 'HEX'
  );

INSERT INTO item_brand (item_brand_code, item_brand_name, item_subsubcategory_id, code_slug, name_slug)
SELECT 'GEN', 'Generic', ssc.item_subsubcategory_id,
       regexp_replace(lower(trim('GEN')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Generic')), '[^a-z0-9]', '', 'g')
FROM item_subsubcategory ssc
WHERE ssc.item_subsubcategory_code = 'HEX'
  AND NOT EXISTS (
      SELECT 1 FROM item_brand b WHERE b.item_subsubcategory_id = ssc.item_subsubcategory_id AND b.item_brand_code = 'GEN'
  );

INSERT INTO item_model (item_model_code, item_model_name, item_brand_id, code_slug, name_slug)
SELECT 'M12X50', 'M12x50 Galvanized', br.item_brand_id,
       regexp_replace(lower(trim('M12X50')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('M12x50 Galvanized')), '[^a-z0-9]', '', 'g')
FROM item_brand br
WHERE br.item_brand_code = 'GEN'
  AND NOT EXISTS (
      SELECT 1 FROM item_model m WHERE m.item_brand_id = br.item_brand_id AND m.item_model_code = 'M12X50'
  );

INSERT INTO item_code (
    item_code_code, item_code_name, item_category_id, item_subcategory_id,
    item_subSubCategory_id, item_brand_id, item_model_id
)
SELECT 'FAS-HEX-001', 'Hex Bolt M12x50mm Galvanized (Box of 100)',
       c.item_category_id, sc.item_subcategory_id, ssc.item_subsubcategory_id, br.item_brand_id, m.item_model_id
FROM item_category c
JOIN item_subcategory sc ON sc.item_category_id = c.item_category_id AND sc.item_subcategory_code = 'BOLT'
JOIN item_subsubcategory ssc ON ssc.item_subcategory_id = sc.item_subcategory_id AND ssc.item_subsubcategory_code = 'HEX'
JOIN item_brand br ON br.item_subsubcategory_id = ssc.item_subsubcategory_id AND br.item_brand_code = 'GEN'
JOIN item_model m ON m.item_brand_id = br.item_brand_id AND m.item_model_code = 'M12X50'
WHERE c.item_type_id = 2 AND c.item_category_code = 'FAS'
  AND NOT EXISTS (SELECT 1 FROM item_code ic WHERE ic.item_code_code = 'FAS-HEX-001');

INSERT INTO consumable_item (item_code_id, uom_id, unit_price, minimal_quantity, current_stock)
SELECT ic.item_code_id, u.uom_id, 2800.00, 10, 35
FROM item_code ic, uom u
WHERE ic.item_code_code = 'FAS-HEX-001' AND u.uom_name = 'Box'
  AND NOT EXISTS (SELECT 1 FROM consumable_item ci WHERE ci.item_code_id = ic.item_code_id);


-- ============================================================
-- INVENTORY ITEMS (item_type_id = 3)
-- ============================================================

-- 6) Rotary Hammer Drill ------------------------------------------------------------
INSERT INTO item_category (item_type_id, item_category_code, item_category_name, code_slug, name_slug)
SELECT 3, 'PWT', 'Power Tools',
       regexp_replace(lower(trim('PWT')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Power Tools')), '[^a-z0-9]', '', 'g')
WHERE NOT EXISTS (
    SELECT 1 FROM item_category WHERE item_type_id = 3 AND item_category_code = 'PWT'
);

INSERT INTO item_subcategory (item_subcategory_code, item_subcategory_name, item_category_id, code_slug, name_slug)
SELECT 'DRL', 'Drilling Equipment', c.item_category_id,
       regexp_replace(lower(trim('DRL')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Drilling Equipment')), '[^a-z0-9]', '', 'g')
FROM item_category c
WHERE c.item_type_id = 3 AND c.item_category_code = 'PWT'
  AND NOT EXISTS (
      SELECT 1 FROM item_subcategory s WHERE s.item_category_id = c.item_category_id AND s.item_subcategory_code = 'DRL'
  );

INSERT INTO item_subsubcategory (item_subsubcategory_code, item_subsubcategory_name, item_subcategory_id, code_slug, name_slug)
SELECT 'RHD', 'Rotary Hammer Drills', sc.item_subcategory_id,
       regexp_replace(lower(trim('RHD')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Rotary Hammer Drills')), '[^a-z0-9]', '', 'g')
FROM item_subcategory sc
WHERE sc.item_subcategory_code = 'DRL'
  AND NOT EXISTS (
      SELECT 1 FROM item_subsubcategory ssc WHERE ssc.item_subcategory_id = sc.item_subcategory_id AND ssc.item_subsubcategory_code = 'RHD'
  );

INSERT INTO item_brand (item_brand_code, item_brand_name, item_subsubcategory_id, code_slug, name_slug)
SELECT 'BOSCH', 'Bosch', ssc.item_subsubcategory_id,
       regexp_replace(lower(trim('BOSCH')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Bosch')), '[^a-z0-9]', '', 'g')
FROM item_subsubcategory ssc
WHERE ssc.item_subsubcategory_code = 'RHD'
  AND NOT EXISTS (
      SELECT 1 FROM item_brand b WHERE b.item_subsubcategory_id = ssc.item_subsubcategory_id AND b.item_brand_code = 'BOSCH'
  );

INSERT INTO item_model (item_model_code, item_model_name, item_brand_id, code_slug, name_slug)
SELECT 'GBH226', 'GBH 2-26 DRE', br.item_brand_id,
       regexp_replace(lower(trim('GBH226')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('GBH 2-26 DRE')), '[^a-z0-9]', '', 'g')
FROM item_brand br
WHERE br.item_brand_code = 'BOSCH'
  AND NOT EXISTS (
      SELECT 1 FROM item_model m WHERE m.item_brand_id = br.item_brand_id AND m.item_model_code = 'GBH226'
  );

INSERT INTO item_code (
    item_code_code, item_code_name, item_category_id, item_subcategory_id,
    item_subSubCategory_id, item_brand_id, item_model_id
)
SELECT 'PWT-DRL-001', 'Bosch GBH 2-26 DRE Rotary Hammer Drill',
       c.item_category_id, sc.item_subcategory_id, ssc.item_subsubcategory_id, br.item_brand_id, m.item_model_id
FROM item_category c
JOIN item_subcategory sc ON sc.item_category_id = c.item_category_id AND sc.item_subcategory_code = 'DRL'
JOIN item_subsubcategory ssc ON ssc.item_subcategory_id = sc.item_subcategory_id AND ssc.item_subsubcategory_code = 'RHD'
JOIN item_brand br ON br.item_subsubcategory_id = ssc.item_subsubcategory_id AND br.item_brand_code = 'BOSCH'
JOIN item_model m ON m.item_brand_id = br.item_brand_id AND m.item_model_code = 'GBH226'
WHERE c.item_type_id = 3 AND c.item_category_code = 'PWT'
  AND NOT EXISTS (SELECT 1 FROM item_code ic WHERE ic.item_code_code = 'PWT-DRL-001');

INSERT INTO inventory_item (
    item_code_id, uom_id, quantity_on_hand, maximum_stock_level,
    average_unit_price, last_purchase_price, re_order_level, remarks
)
SELECT ic.item_code_id, u.uom_id, 8, 15, 42000.00, 43500.00, 3, 'Site power tool pool'
FROM item_code ic, uom u
WHERE ic.item_code_code = 'PWT-DRL-001' AND u.uom_name = 'Each'
  AND NOT EXISTS (SELECT 1 FROM inventory_item ii WHERE ii.item_code_id = ic.item_code_id);


-- 7) Total Station (Survey Equipment) -----------------------------------------------
INSERT INTO item_category (item_type_id, item_category_code, item_category_name, code_slug, name_slug)
SELECT 3, 'SUR', 'Survey Equipment',
       regexp_replace(lower(trim('SUR')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Survey Equipment')), '[^a-z0-9]', '', 'g')
WHERE NOT EXISTS (
    SELECT 1 FROM item_category WHERE item_type_id = 3 AND item_category_code = 'SUR'
);

INSERT INTO item_subcategory (item_subcategory_code, item_subcategory_name, item_category_id, code_slug, name_slug)
SELECT 'MSR', 'Measuring Instruments', c.item_category_id,
       regexp_replace(lower(trim('MSR')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Measuring Instruments')), '[^a-z0-9]', '', 'g')
FROM item_category c
WHERE c.item_type_id = 3 AND c.item_category_code = 'SUR'
  AND NOT EXISTS (
      SELECT 1 FROM item_subcategory s WHERE s.item_category_id = c.item_category_id AND s.item_subcategory_code = 'MSR'
  );

INSERT INTO item_subsubcategory (item_subsubcategory_code, item_subsubcategory_name, item_subcategory_id, code_slug, name_slug)
SELECT 'TS', 'Total Stations', sc.item_subcategory_id,
       regexp_replace(lower(trim('TS')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Total Stations')), '[^a-z0-9]', '', 'g')
FROM item_subcategory sc
WHERE sc.item_subcategory_code = 'MSR'
  AND NOT EXISTS (
      SELECT 1 FROM item_subsubcategory ssc WHERE ssc.item_subcategory_id = sc.item_subcategory_id AND ssc.item_subsubcategory_code = 'TS'
  );

INSERT INTO item_brand (item_brand_code, item_brand_name, item_subsubcategory_id, code_slug, name_slug)
SELECT 'LEICA', 'Leica', ssc.item_subsubcategory_id,
       regexp_replace(lower(trim('LEICA')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Leica')), '[^a-z0-9]', '', 'g')
FROM item_subsubcategory ssc
WHERE ssc.item_subsubcategory_code = 'TS'
  AND NOT EXISTS (
      SELECT 1 FROM item_brand b WHERE b.item_subsubcategory_id = ssc.item_subsubcategory_id AND b.item_brand_code = 'LEICA'
  );

INSERT INTO item_model (item_model_code, item_model_name, item_brand_id, code_slug, name_slug)
SELECT 'TS06', 'TS06 Plus', br.item_brand_id,
       regexp_replace(lower(trim('TS06')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('TS06 Plus')), '[^a-z0-9]', '', 'g')
FROM item_brand br
WHERE br.item_brand_code = 'LEICA'
  AND NOT EXISTS (
      SELECT 1 FROM item_model m WHERE m.item_brand_id = br.item_brand_id AND m.item_model_code = 'TS06'
  );

INSERT INTO item_code (
    item_code_code, item_code_name, item_category_id, item_subcategory_id,
    item_subSubCategory_id, item_brand_id, item_model_id
)
SELECT 'SUR-TS-001', 'Leica TS06 Plus Total Station',
       c.item_category_id, sc.item_subcategory_id, ssc.item_subsubcategory_id, br.item_brand_id, m.item_model_id
FROM item_category c
JOIN item_subcategory sc ON sc.item_category_id = c.item_category_id AND sc.item_subcategory_code = 'MSR'
JOIN item_subsubcategory ssc ON ssc.item_subcategory_id = sc.item_subcategory_id AND ssc.item_subsubcategory_code = 'TS'
JOIN item_brand br ON br.item_subsubcategory_id = ssc.item_subsubcategory_id AND br.item_brand_code = 'LEICA'
JOIN item_model m ON m.item_brand_id = br.item_brand_id AND m.item_model_code = 'TS06'
WHERE c.item_type_id = 3 AND c.item_category_code = 'SUR'
  AND NOT EXISTS (SELECT 1 FROM item_code ic WHERE ic.item_code_code = 'SUR-TS-001');

INSERT INTO inventory_item (
    item_code_id, uom_id, quantity_on_hand, maximum_stock_level,
    average_unit_price, last_purchase_price, re_order_level, remarks
)
SELECT ic.item_code_id, u.uom_id, 3, 5, 850000.00, 875000.00, 1, 'Survey department - calibrate annually'
FROM item_code ic, uom u
WHERE ic.item_code_code = 'SUR-TS-001' AND u.uom_name = 'Each'
  AND NOT EXISTS (SELECT 1 FROM inventory_item ii WHERE ii.item_code_id = ic.item_code_id);


-- 8) Armoured Cable (Electrical Supplies) --------------------------------------------
INSERT INTO item_category (item_type_id, item_category_code, item_category_name, code_slug, name_slug)
SELECT 3, 'ELC', 'Electrical Supplies',
       regexp_replace(lower(trim('ELC')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Electrical Supplies')), '[^a-z0-9]', '', 'g')
WHERE NOT EXISTS (
    SELECT 1 FROM item_category WHERE item_type_id = 3 AND item_category_code = 'ELC'
);

INSERT INTO item_subcategory (item_subcategory_code, item_subcategory_name, item_category_id, code_slug, name_slug)
SELECT 'CBL', 'Cables & Wires', c.item_category_id,
       regexp_replace(lower(trim('CBL')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Cables & Wires')), '[^a-z0-9]', '', 'g')
FROM item_category c
WHERE c.item_type_id = 3 AND c.item_category_code = 'ELC'
  AND NOT EXISTS (
      SELECT 1 FROM item_subcategory s WHERE s.item_category_id = c.item_category_id AND s.item_subcategory_code = 'CBL'
  );

INSERT INTO item_subsubcategory (item_subsubcategory_code, item_subsubcategory_name, item_subcategory_id, code_slug, name_slug)
SELECT 'ARM', 'Armoured Cable', sc.item_subcategory_id,
       regexp_replace(lower(trim('ARM')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Armoured Cable')), '[^a-z0-9]', '', 'g')
FROM item_subcategory sc
WHERE sc.item_subcategory_code = 'CBL'
  AND NOT EXISTS (
      SELECT 1 FROM item_subsubcategory ssc WHERE ssc.item_subcategory_id = sc.item_subcategory_id AND ssc.item_subsubcategory_code = 'ARM'
  );

INSERT INTO item_brand (item_brand_code, item_brand_name, item_subsubcategory_id, code_slug, name_slug)
SELECT 'KELANI', 'Kelani Cables', ssc.item_subsubcategory_id,
       regexp_replace(lower(trim('KELANI')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Kelani Cables')), '[^a-z0-9]', '', 'g')
FROM item_subsubcategory ssc
WHERE ssc.item_subsubcategory_code = 'ARM'
  AND NOT EXISTS (
      SELECT 1 FROM item_brand b WHERE b.item_subsubcategory_id = ssc.item_subsubcategory_id AND b.item_brand_code = 'KELANI'
  );

INSERT INTO item_model (item_model_code, item_model_name, item_brand_id, code_slug, name_slug)
SELECT '4MM3C', '4mm2 3-Core SWA', br.item_brand_id,
       regexp_replace(lower(trim('4MM3C')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('4mm2 3-Core SWA')), '[^a-z0-9]', '', 'g')
FROM item_brand br
WHERE br.item_brand_code = 'KELANI'
  AND NOT EXISTS (
      SELECT 1 FROM item_model m WHERE m.item_brand_id = br.item_brand_id AND m.item_model_code = '4MM3C'
  );

INSERT INTO item_code (
    item_code_code, item_code_name, item_category_id, item_subcategory_id,
    item_subSubCategory_id, item_brand_id, item_model_id
)
SELECT 'ELC-CBL-001', 'Kelani 4mm2 3-Core SWA Armoured Cable (100m Drum)',
       c.item_category_id, sc.item_subcategory_id, ssc.item_subsubcategory_id, br.item_brand_id, m.item_model_id
FROM item_category c
JOIN item_subcategory sc ON sc.item_category_id = c.item_category_id AND sc.item_subcategory_code = 'CBL'
JOIN item_subsubcategory ssc ON ssc.item_subcategory_id = sc.item_subcategory_id AND ssc.item_subsubcategory_code = 'ARM'
JOIN item_brand br ON br.item_subsubcategory_id = ssc.item_subsubcategory_id AND br.item_brand_code = 'KELANI'
JOIN item_model m ON m.item_brand_id = br.item_brand_id AND m.item_model_code = '4MM3C'
WHERE c.item_type_id = 3 AND c.item_category_code = 'ELC'
  AND NOT EXISTS (SELECT 1 FROM item_code ic WHERE ic.item_code_code = 'ELC-CBL-001');

INSERT INTO inventory_item (
    item_code_id, uom_id, quantity_on_hand, maximum_stock_level,
    average_unit_price, last_purchase_price, re_order_level, remarks
)
SELECT ic.item_code_id, u.uom_id, 25, 60, 38000.00, 39500.00, 10, 'Stored in dry store - Bay 3'
FROM item_code ic, uom u
WHERE ic.item_code_code = 'ELC-CBL-001' AND u.uom_name = 'Drum'
  AND NOT EXISTS (SELECT 1 FROM inventory_item ii WHERE ii.item_code_id = ic.item_code_id);


-- 9) PVC Pipe (Plumbing Fittings) -----------------------------------------------------
INSERT INTO item_category (item_type_id, item_category_code, item_category_name, code_slug, name_slug)
SELECT 3, 'PLM', 'Plumbing Fittings',
       regexp_replace(lower(trim('PLM')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Plumbing Fittings')), '[^a-z0-9]', '', 'g')
WHERE NOT EXISTS (
    SELECT 1 FROM item_category WHERE item_type_id = 3 AND item_category_code = 'PLM'
);

INSERT INTO item_subcategory (item_subcategory_code, item_subcategory_name, item_category_id, code_slug, name_slug)
SELECT 'PVC', 'PVC Pipes & Fittings', c.item_category_id,
       regexp_replace(lower(trim('PVC')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('PVC Pipes & Fittings')), '[^a-z0-9]', '', 'g')
FROM item_category c
WHERE c.item_type_id = 3 AND c.item_category_code = 'PLM'
  AND NOT EXISTS (
      SELECT 1 FROM item_subcategory s WHERE s.item_category_id = c.item_category_id AND s.item_subcategory_code = 'PVC'
  );

INSERT INTO item_subsubcategory (item_subsubcategory_code, item_subsubcategory_name, item_subcategory_id, code_slug, name_slug)
SELECT 'PIPE', 'PVC Pipes', sc.item_subcategory_id,
       regexp_replace(lower(trim('PIPE')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('PVC Pipes')), '[^a-z0-9]', '', 'g')
FROM item_subcategory sc
WHERE sc.item_subcategory_code = 'PVC'
  AND NOT EXISTS (
      SELECT 1 FROM item_subsubcategory ssc WHERE ssc.item_subcategory_id = sc.item_subcategory_id AND ssc.item_subsubcategory_code = 'PIPE'
  );

INSERT INTO item_brand (item_brand_code, item_brand_name, item_subsubcategory_id, code_slug, name_slug)
SELECT 'ROYAL', 'Royal', ssc.item_subsubcategory_id,
       regexp_replace(lower(trim('ROYAL')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('Royal')), '[^a-z0-9]', '', 'g')
FROM item_subsubcategory ssc
WHERE ssc.item_subsubcategory_code = 'PIPE'
  AND NOT EXISTS (
      SELECT 1 FROM item_brand b WHERE b.item_subsubcategory_id = ssc.item_subsubcategory_id AND b.item_brand_code = 'ROYAL'
  );

INSERT INTO item_model (item_model_code, item_model_name, item_brand_id, code_slug, name_slug)
SELECT '110CD', '110mm Class D', br.item_brand_id,
       regexp_replace(lower(trim('110CD')), '[^a-z0-9]', '', 'g'),
       regexp_replace(lower(trim('110mm Class D')), '[^a-z0-9]', '', 'g')
FROM item_brand br
WHERE br.item_brand_code = 'ROYAL'
  AND NOT EXISTS (
      SELECT 1 FROM item_model m WHERE m.item_brand_id = br.item_brand_id AND m.item_model_code = '110CD'
  );

INSERT INTO item_code (
    item_code_code, item_code_name, item_category_id, item_subcategory_id,
    item_subSubCategory_id, item_brand_id, item_model_id
)
SELECT 'PLM-PVC-001', 'Royal PVC Pipe 110mm Class D (6m Length)',
       c.item_category_id, sc.item_subcategory_id, ssc.item_subsubcategory_id, br.item_brand_id, m.item_model_id
FROM item_category c
JOIN item_subcategory sc ON sc.item_category_id = c.item_category_id AND sc.item_subcategory_code = 'PVC'
JOIN item_subsubcategory ssc ON ssc.item_subcategory_id = sc.item_subcategory_id AND ssc.item_subsubcategory_code = 'PIPE'
JOIN item_brand br ON br.item_subsubcategory_id = ssc.item_subsubcategory_id AND br.item_brand_code = 'ROYAL'
JOIN item_model m ON m.item_brand_id = br.item_brand_id AND m.item_model_code = '110CD'
WHERE c.item_type_id = 3 AND c.item_category_code = 'PLM'
  AND NOT EXISTS (SELECT 1 FROM item_code ic WHERE ic.item_code_code = 'PLM-PVC-001');

INSERT INTO inventory_item (
    item_code_id, uom_id, quantity_on_hand, maximum_stock_level,
    average_unit_price, last_purchase_price, re_order_level, remarks
)
SELECT ic.item_code_id, u.uom_id, 150, 300, 2400.00, 2550.00, 50, 'Plumbing store rack B'
FROM item_code ic, uom u
WHERE ic.item_code_code = 'PLM-PVC-001' AND u.uom_name = 'Length'
  AND NOT EXISTS (SELECT 1 FROM inventory_item ii WHERE ii.item_code_id = ic.item_code_id);

COMMIT;
