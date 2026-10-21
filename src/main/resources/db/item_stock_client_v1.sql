BEGIN;

-- Creates the consumable_item / inventory_item catalog rows for the client item codes (run item_code_client_v1.sql first).
-- Consumable-type codes -> consumable_item, Inventory-type codes -> inventory_item; Asset, Spare Part and Uncategorized
-- have no catalog table. The client list gives no UOM, so one is assigned by rule: "Nos" unless the category/sub category
-- below says otherwise. This is a starting point - correct a UOM afterwards from the item screens if needed.
-- Idempotent: items that already have a row are skipped; missing UOMs are added.

INSERT INTO uom (uom_name)
SELECT v.name FROM (VALUES ('Nos'), ('Set'), ('MT'), ('Ltr'), ('Kg'), ('m3')) AS v(name)
WHERE NOT EXISTS (SELECT 1 FROM uom u WHERE lower(u.uom_name) = lower(v.name));

CREATE TEMP TABLE uom_rule (cat_code text, sub_code text, uom_name text) ON COMMIT DROP;
INSERT INTO uom_rule VALUES
  ('CEM','RMC','m3'),
  ('AGG','AGG','m3'), ('AGG','ERT','m3'),
  ('RNF','RBR','MT'), ('RNF','SLC','MT'), ('RNF','BDW','Kg'),
  ('BIT','BIT','MT'), ('BIT','ASP','MT'),
  ('FUL','FUE','Ltr'), ('FUL','OIL','Ltr'),
  ('PNT','PNT','Ltr'), ('ADM','CHM','Ltr'),
  ('WLC','WLC','Kg'),
  ('MEQ','SOC','Set'), ('MEQ','ALK','Set'), ('MEQ','FLG','Set'), ('MEQ','PUL','Set'), ('HND','TLT','Set');

CREATE TEMP TABLE stock_codes ON COMMIT DROP AS
SELECT ic.item_code_id, lower(t.item_type_name) AS type_name,
       COALESCE(r.uom_name, 'Nos') AS uom_name
FROM item_code ic
JOIN item_category cat ON cat.item_category_id = ic.item_category_id
JOIN item_type t ON t.item_type_id = cat.item_type_id
JOIN item_subcategory sub ON sub.item_subcategory_id = ic.item_subcategory_id
LEFT JOIN uom_rule r ON r.cat_code = cat.item_category_code AND r.sub_code = sub.item_subcategory_code
WHERE lower(t.item_type_name) IN ('consumable', 'inventory');

INSERT INTO consumable_item (item_code_id, uom_id, unit_price, current_stock, is_active, remarks)
SELECT s.item_code_id, u.uom_id, 0, 0, true, ''
FROM stock_codes s JOIN uom u ON lower(u.uom_name) = lower(s.uom_name)
WHERE s.type_name = 'consumable'
  AND NOT EXISTS (SELECT 1 FROM consumable_item c WHERE c.item_code_id = s.item_code_id);

INSERT INTO inventory_item (item_code_id, uom_id, quantity_on_hand, maximum_stock_level, average_unit_price, last_purchase_price, remarks, is_active)
SELECT s.item_code_id, u.uom_id, 0, 0, 0, 0, '', true
FROM stock_codes s JOIN uom u ON lower(u.uom_name) = lower(s.uom_name)
WHERE s.type_name = 'inventory'
  AND NOT EXISTS (SELECT 1 FROM inventory_item i WHERE i.item_code_id = s.item_code_id);

COMMIT;
