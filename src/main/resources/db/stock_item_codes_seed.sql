-- Stock item codes from "Categories and Sub categories.xlsx" (sheet "Stock codes"), registered as Consumables (item_code + consumable_item).
-- 118 items. Item code = <category>-<sub category>-<sheet code>, e.g. CMC-CEM-0001 (unique across item_code).
-- Item name = Description, with Part No appended when present ("Cement, Bulk, OPC, Nippon"); blank Part No stays blank.
-- Prerequisite: categories/sub categories already loaded (item_categories_seed.sql). Category is matched
-- by code within item_type_id 2 (Consumable); the script aborts if any category/sub category is missing.
-- Also adds the UOMs the sheet uses that are missing: Metric Ton (sheet "MT"), Cube, Reel.
-- Safe to re-run: every INSERT is guarded with WHERE NOT EXISTS.

BEGIN;

INSERT INTO uom (uom_name)
SELECT v.n FROM (VALUES ('Metric Ton'), ('Cube'), ('Reel')) AS v(n)
WHERE NOT EXISTS (SELECT 1 FROM uom u WHERE lower(u.uom_name) = lower(v.n));

CREATE TEMP TABLE seed_item_code (
    category_code VARCHAR(50)  NOT NULL,
    sub_code      VARCHAR(50)  NOT NULL,
    seq           VARCHAR(20)  NOT NULL,
    item_name     VARCHAR(200) NOT NULL,
    uom_name      VARCHAR(50)  NOT NULL
) ON COMMIT DROP;

INSERT INTO seed_item_code (category_code, sub_code, seq, item_name, uom_name) VALUES
    ('CMC', 'CEM', '0001', 'Cement, Bulk, OPC, Nippon', 'Metric Ton'),
    ('CMC', 'CEM', '0002', 'Cement, Bulk, OPC, Powertech', 'Metric Ton'),
    ('CMC', 'CEM', '0003', 'Cement, Bulk, BHC, Tokyo Super', 'Metric Ton'),
    ('CMC', 'CEM', '0004', 'Cement, Bulk, BHC, Insee Rapid Flow Plus', 'Metric Ton'),
    ('CMC', 'CEM', '0005', 'Cement, Bagged, OPC', 'Nos'),
    ('CMC', 'CEM', '0006', 'Cement, Bagged, BHC', 'Nos'),
    ('CMC', 'RMC', '0001', 'Concrete, Ready Mix, Grade 15', 'Cubic Meter'),
    ('CMC', 'RMC', '0002', 'Concrete, Ready Mix, Grade 20', 'Cubic Meter'),
    ('CMC', 'RMC', '0003', 'Concrete, Ready Mix, Grade 25', 'Cubic Meter'),
    ('CMC', 'RMC', '0004', 'Concrete, Ready Mix, Grade 30', 'Cubic Meter'),
    ('CMC', 'RMC', '0005', 'Concrete, Ready Mix, Grade 35', 'Cubic Meter'),
    ('CMC', 'RMC', '0006', 'Concrete, Ready Mix, Grade 40', 'Cubic Meter'),
    ('CMC', 'GRT', '0001', 'Grout, Construction, Finex GP', 'Bag'),
    ('AQM', 'AGG', '0001', 'Aggregate, Base Course, ABC', 'Cube'),
    ('AQM', 'AGG', '0002', 'Aggregate, Concrete, 19mm', 'Cube'),
    ('AQM', 'SAN', '0001', 'Quarry Dust, 0 - 5mm', 'Cube'),
    ('AQM', 'SAN', '0002', 'Sand, River', 'Cube'),
    ('AQM', 'SAN', '0003', 'Sand, Sea', 'Cube'),
    ('AQM', 'SAN', '0004', 'Sand, Manufactured', 'Cube'),
    ('AQM', 'BLD', '0001', 'C1', 'Metric Ton'),
    ('AQM', 'GRV', '0001', 'Gravel, Sub Base', 'Cube'),
    ('AQM', 'GRV', '0002', 'Gravel, Embankment', 'Cube'),
    ('AQM', 'MTL', '0001', 'Metal, 6" x 9"', 'Cube'),
    ('PCP', 'BLK', '0001', 'Block, Hollow, 390 x 190 x 200mm', 'Nos'),
    ('PCP', 'BLK', '0002', 'Block, Hollow, 390 x 190 x 150mm', 'Nos'),
    ('PCP', 'BLK', '0003', 'Block, Hollow, 390 x 190 x 100mm', 'Nos'),
    ('PCP', 'BLK', '0004', 'Block, Solid, 390 x 190 x 200mm', 'Nos'),
    ('PCP', 'BLK', '0005', 'Block, Solid, 390 x 190 x 150mm', 'Nos'),
    ('PCP', 'BLK', '0006', 'Block, Solid, 390 x 190 x 100mm', 'Nos'),
    ('PCP', 'KRB', '0001', 'Kerb, Standard, 900 x 125 x 255mm', 'Nos'),
    ('PCP', 'KRB', '0002', 'Kerb, Inlet, 900 x 125 x 255mm', 'Nos'),
    ('PCP', 'KRB', '0003', 'Kerb, Dropped, 900 x 125 x 150mm', 'Nos'),
    ('PCP', 'KRB', '0004', 'Kerb, Dropper, Left, 900 x 125 x 255mm', 'Nos'),
    ('PCP', 'KRB', '0005', 'Kerb, Dropper, Right, 900 x 125 x 255mm', 'Nos'),
    ('PCP', 'KRB', '0006', 'Kerb, Bridge, 900 x 280 x 280mm', 'Nos'),
    ('PCP', 'PVB', '0001', 'Interlocking, Uni, Rough Finish, 220 x 110 x 60mm', 'Nos'),
    ('PCP', 'PVB', '0002', 'Interlocking, Uni, Smooth Finish, 220 x 110 x 60mm', 'Nos'),
    ('PCP', 'PVB', '0003', 'Interlocking, Cobble, Rough Finish, 200 x 100 x 60mm', 'Nos'),
    ('PCP', 'PVB', '0004', 'Interlocking, Cobble, Smooth Finish, 200 x 100 x 60mm', 'Nos'),
    ('PCP', 'PCB', '0001', 'Beam, Bridge, PSC, 11.5m (T/B/506)', 'Nos'),
    ('PCP', 'PCB', '0002', 'Beam, Bridge, PSC, 13.5m (T/B/505)', 'Nos'),
    ('PCP', 'HMP', '0001', 'Pipe, Hume, NP2, 300mm x 1200mm', 'Nos'),
    ('PCP', 'HMP', '0002', 'Pipe, Hume, NP2, 300mm x 2400mm', 'Nos'),
    ('PCP', 'HMP', '0003', 'Pipe, Hume, NP2, 450mm x 1200mm', 'Nos'),
    ('PCP', 'HMP', '0004', 'Pipe, Hume, NP2, 450mm x 2400mm', 'Nos'),
    ('PCP', 'HMP', '0005', 'Pipe, Hume, NP2, 600mm x 1200mm', 'Nos'),
    ('PCP', 'HMP', '0006', 'Pipe, Hume, NP2, 600mm x 2400mm', 'Nos'),
    ('PCP', 'HMP', '0007', 'Pipe, Hume, NP2, 900mm x 1200mm', 'Nos'),
    ('PCP', 'HMP', '0008', 'Pipe, Hume, NP2, 900mm x 2400mm', 'Nos'),
    ('PCP', 'HMP', '0009', 'Pipe, Hume, NP2, 1200mm x 1200mm', 'Nos'),
    ('PCP', 'HMP', '0010', 'Pipe, Hume, NP2, 1200mm x 2400mm', 'Nos'),
    ('PCP', 'HMP', '0011', 'Pipe, Hume, NP3, 300mm x 1200mm', 'Nos'),
    ('PCP', 'HMP', '0012', 'Pipe, Hume, NP3, 300mm x 2400mm', 'Nos'),
    ('PCP', 'HMP', '0013', 'Pipe, Hume, NP3, 450mm x 1200mm', 'Nos'),
    ('PCP', 'HMP', '0014', 'Pipe, Hume, NP3, 450mm x 2400mm', 'Nos'),
    ('PCP', 'HMP', '0015', 'Pipe, Hume, NP3, 600mm x 1200mm', 'Nos'),
    ('PCP', 'HMP', '0016', 'Pipe, Hume, NP3, 600mm x 2400mm', 'Nos'),
    ('PCP', 'HMP', '0017', 'Pipe, Hume, NP3, 900mm x 1200mm', 'Nos'),
    ('PCP', 'HMP', '0018', 'Pipe, Hume, NP3, 900mm x 2400mm', 'Nos'),
    ('PCP', 'HMP', '0019', 'Pipe, Hume, NP3, 1200mm x 1200mm', 'Nos'),
    ('PCP', 'HMP', '0020', 'Pipe, Hume, NP3, 1200mm x 2400mm', 'Nos'),
    ('RFS', 'TSB', '0001', 'Tor Steel Bar, 08mm x 6mm', 'Metric Ton'),
    ('RFS', 'TSB', '0002', 'Tor Steel Bar, 10mm x 6m', 'Metric Ton'),
    ('RFS', 'TSB', '0003', 'Tor Steel Bar, 12mm x 6m', 'Metric Ton'),
    ('RFS', 'TSB', '0004', 'Tor Steel Bar, 16mm x 6m', 'Metric Ton'),
    ('RFS', 'TSB', '0005', 'Tor Steel Bar, 20mm x 6m', 'Metric Ton'),
    ('RFS', 'TSB', '0006', 'Tor Steel Bar, 25mm x 6m', 'Metric Ton'),
    ('RFS', 'TSB', '0007', 'Tor Steel Bar, 32mm x 6m', 'Metric Ton'),
    ('RFS', 'TSB', '0008', 'Tor Steel Bar, 10mm x 12m', 'Metric Ton'),
    ('RFS', 'TSB', '0009', 'Tor Steel Bar, 12mm x 12m', 'Metric Ton'),
    ('RFS', 'TSB', '0010', 'Tor Steel Bar, 16mm x 12m', 'Metric Ton'),
    ('RFS', 'TSB', '0011', 'Tor Steel Bar, 20mm x 12m', 'Metric Ton'),
    ('RFS', 'TSB', '0012', 'Tor Steel Bar, 25mm x 12m', 'Metric Ton'),
    ('RFS', 'TSB', '0013', 'Tor Steel Bar, 32mm x 12m', 'Metric Ton'),
    ('RFS', 'RDB', '0001', 'Round Steel Bar, 10mm x 6m', 'Nos'),
    ('RFS', 'RDB', '0002', 'Round Steel Bar, 12mm x 6m', 'Nos'),
    ('RFS', 'RDB', '0003', 'Round Steel Bar, 16mm x 6m', 'Nos'),
    ('RFS', 'RDB', '0004', 'Round Steel Bar, 20mm x 6m', 'Nos'),
    ('RFS', 'RDB', '0005', 'Round Steel Bar, 25mm x 6m', 'Nos'),
    ('RFS', 'SLC', '0001', 'Coil, Mild Steel, 06mm', 'Metric Ton'),
    ('RFS', 'SLC', '0002', 'Coil, Mild Steel, 08mm', 'Metric Ton'),
    ('RFS', 'WWM', '0001', 'Mesh, Welded, 7'' x 12'', 50mm x 50mm x 2.8mm', 'Nos'),
    ('RFS', 'WWM', '0002', 'Mesh, Welded, 2m x 7.5m, 50mm x 50mm x 2mm', 'Roll'),
    ('RFS', 'BDW', '0001', 'Wire, Bnding, 1.2mm', 'Kilogram'),
    ('RFS', 'PSS', '0001', 'Strand, Pre Stressing, 15.7mm', 'Metric Ton'),
    ('RFS', 'PSA', '0001', 'Wedge, 3 Jaw, XL3', 'Nos'),
    ('RFS', 'PSA', '0002', 'Barrel/Chuck, XL3', 'Nos'),
    ('FCM', 'THB', '0001', 'Thread Bar, 10mm', 'Nos'),
    ('FCM', 'THB', '0002', 'Thread Bar, 12mm', 'Nos'),
    ('FCM', 'WEL', '0001', 'Electrode, LH 7018, 2.6mm', 'Kilogram'),
    ('FCM', 'WEL', '0002', 'Electrode, LH 7018, 3.2mm', 'Kilogram'),
    ('FCM', 'WEL', '0003', 'Electrode, LH 7018, 04mm', 'Kilogram'),
    ('FCM', 'WEL', '0004', 'Electrode, MS 6013, 2.6mm', 'Kilogram'),
    ('FCM', 'WEL', '0005', 'Electrode, MS 6013, 3.2mm', 'Kilogram'),
    ('FCM', 'WEL', '0006', 'Electrode, MS 6013, 04mm', 'Kilogram'),
    ('FCM', 'WEL', '0007', 'Electrode, Cast Iron 400, 2.6mm', 'Kilogram'),
    ('FCM', 'WEL', '0008', 'Electrode, Cast Iron 400, 3.2mm', 'Kilogram'),
    ('FCM', 'WEL', '0009', 'Electrode, Cast Iron 500, 2.6mm', 'Kilogram'),
    ('FCM', 'WEL', '0010', 'Electrode, Cast Iron 500, 3.2mm', 'Kilogram'),
    ('FCM', 'WEL', '0011', 'Electrode, Cast Iron 500, 04mm', 'Kilogram'),
    ('FCM', 'WEL', '0012', 'Electrode, SS 308L, 2.6mm', 'Kilogram'),
    ('FCM', 'WEL', '0013', 'Electrode, SS 308L, 3.2mm', 'Kilogram'),
    ('FCM', 'WEL', '0014', 'Electrode, SS 316L, 2.6mm', 'Kilogram'),
    ('FCM', 'WEL', '0015', 'Electrode, SS 316L, 3.2mm', 'Kilogram'),
    ('FCM', 'WEL', '0016', 'Wire, Mig, Solid, 15kg, 0.8mm', 'Reel'),
    ('FCM', 'WEL', '0017', 'Wire, Mig, Solid, 15kg, 1.2mm', 'Reel'),
    ('FCM', 'WEL', '0018', 'Wire, Mig, Flux, 15kg, 0.8mm', 'Reel'),
    ('FCM', 'WEL', '0019', 'Wire, Mig, Flux, 15kg, 1.2mm', 'Reel'),
    ('FCM', 'DSW', '0001', 'Dics, Cutting, 4.5"', 'Nos'),
    ('FCM', 'DSW', '0002', 'Dics, Cutting, 07"', 'Nos'),
    ('FCM', 'DSW', '0003', 'Dics, Cutting, 14"', 'Nos'),
    ('FCM', 'DSW', '0004', 'Disc, Diamond, 4.5"', 'Nos'),
    ('FCM', 'DSW', '0005', 'Disc, Diamond, 07"', 'Nos'),
    ('FCM', 'DSW', '0006', 'Disc, Diamond, 14"', 'Nos'),
    ('FCM', 'DSW', '0007', 'Disc, Diamond, 16"', 'Nos'),
    ('FCM', 'DSW', '0008', 'Disc, Grinding, 4.5"', 'Nos'),
    ('FCM', 'DSW', '0009', 'Disc, Grinding, 07"', 'Nos'),
    ('FCM', 'DSW', '0010', 'Disc, Cup, Diamond, 4.5"', 'Nos');

DO $$
DECLARE missing TEXT;
BEGIN
    SELECT string_agg(DISTINCT s.category_code || '/' || s.sub_code, ', ') INTO missing
    FROM seed_item_code s
    WHERE NOT EXISTS (
        SELECT 1 FROM item_category c
        JOIN item_subcategory sc ON sc.item_category_id = c.item_category_id
        WHERE c.item_type_id = 2 AND c.item_category_code = s.category_code AND sc.item_subcategory_code = s.sub_code);
    IF missing IS NOT NULL THEN
        RAISE EXCEPTION 'Missing category/sub category: %', missing;
    END IF;
END $$;

INSERT INTO item_code (item_code_code, item_code_name, item_category_id, item_subcategory_id)
SELECT s.category_code || '-' || s.sub_code || '-' || s.seq, s.item_name, c.item_category_id, sc.item_subcategory_id
FROM seed_item_code s
JOIN item_category c ON c.item_type_id = 2 AND c.item_category_code = s.category_code
JOIN item_subcategory sc ON sc.item_category_id = c.item_category_id AND sc.item_subcategory_code = s.sub_code
WHERE NOT EXISTS (
    SELECT 1 FROM item_code ic WHERE ic.item_code_code = s.category_code || '-' || s.sub_code || '-' || s.seq);

-- Register every seeded item code as a Consumable (unit price 0 and stock 0 until set in the app).
-- UOM mapping from the sheet: MT->Metric Ton, Kg->Kilogram, m3->Cubic Meter, 25kg/Bag->Bag; others by name.
DO $$
DECLARE missing TEXT;
BEGIN
    SELECT string_agg(DISTINCT s.uom_name, ', ') INTO missing FROM seed_item_code s
    WHERE NOT EXISTS (SELECT 1 FROM uom u WHERE u.uom_name = s.uom_name);
    IF missing IS NOT NULL THEN RAISE EXCEPTION 'Missing UOM: %', missing; END IF;
END $$;

INSERT INTO consumable_item (item_code_id, uom_id, unit_price, current_stock, is_active)
SELECT ic.item_code_id, u.uom_id, 0, 0, TRUE
FROM seed_item_code s
JOIN item_code ic ON ic.item_code_code = s.category_code || '-' || s.sub_code || '-' || s.seq
JOIN uom u ON u.uom_name = s.uom_name
WHERE NOT EXISTS (SELECT 1 FROM consumable_item ci WHERE ci.item_code_id = ic.item_code_id);

COMMIT;
