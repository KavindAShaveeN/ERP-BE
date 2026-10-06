-- Aligns asset coding with the company's existing WMS asset register
-- ("WMS Total Assets as at 06.08.2026.xlsx"), which codes every asset as
-- <TYPE_CODE>-<SEQ> (e.g. "AC-05", "EX-124"), sequence restarting per type
-- code and zero-padded to at least 2 digits (never re-padded past 99), with
-- 5 equipment families using a nested <TYPE_CODE>-<SUBTYPE>-<SEQ> scheme
-- (e.g. "OE-CM-102"). See AssetCodeService / ItemSubCategory.
--
-- This script:
--   1) adds a "starting_sequence" column to item_subcategory, so the app can
--      compute the next code as max(existing codes for this type, starting_sequence) + 1
--      instead of always starting at 1 — letting new assets continue the
--      register's numbering without importing all 5184 historical rows.
--   2) seeds the 5 broad asset categories (Machine/Vehicle/Tool/Software/Other/Review)
--      and the ~109 real type codes found in the register as item_category /
--      item_subcategory rows, each with starting_sequence = the highest
--      sequence number already used for that type in the register.
--
-- The 5 nested families (OE, LI, SI, HH, LS) are seeded as compound codes
-- (e.g. "OE-CM", "OE-DC", ...) so the generated code naturally comes out as
-- "OE-CM-103" (type-subtype code + "-" + sequence).
--
-- HJ (Hydraulic Jack) and WR (Chain Roller) use an irregular "set/item"
-- kit-numbering scheme in the register (e.g. "HJ - 11/09") that doesn't fit
-- a simple incrementing sequence; they are seeded with starting_sequence = 0
-- so staff should expect to manually adjust the suggested code for these two.
--
-- Safe to re-run: every statement is guarded (IF NOT EXISTS / WHERE NOT EXISTS).

ALTER TABLE item_subcategory ADD COLUMN IF NOT EXISTS starting_sequence INT NOT NULL DEFAULT 0;

-- ---------------------------------------------------------------------------
-- Categories (itemTypeId = 1 is the existing "Asset" item type, see
-- AddAssetPage.tsx's ASSET_ITEM_TYPE_ID).
-- ---------------------------------------------------------------------------
-- Matched by name (not code): the live DB enforces uniqueness on (item_type_id,
-- name_slug), and a category with one of these names may already exist under a
-- different code (e.g. from earlier manual testing) — reuse it rather than
-- collide with it. The subcategory block below likewise resolves the parent
-- category by name, so whatever code an existing row already has is fine.
INSERT INTO item_category (item_type_id, item_category_code, item_category_name, code_slug, name_slug)
SELECT 1, v.code, v.name, lower(v.code), lower(v.name)
FROM (VALUES
    ('MACHINE',  'Machine'),
    ('VEHICLE',  'Vehicle'),
    ('TOOL',     'Tool'),
    ('SOFTWARE', 'Software'),
    ('OTHER',    'Other'),
    ('REVIEW',   'Review')
) AS v(code, name)
WHERE NOT EXISTS (
    SELECT 1 FROM item_category c WHERE c.item_type_id = 1 AND c.name_slug = lower(v.name)
);

-- ---------------------------------------------------------------------------
-- Type codes (item_subcategory rows). code = the register's TYPE_CODE (or
-- TYPE-SUBTYPE for the 5 nested families), starting_sequence = highest
-- sequence number already used for that code in the register.
-- ---------------------------------------------------------------------------
INSERT INTO item_subcategory (item_subcategory_code, item_subcategory_name, item_category_id, code_slug, name_slug, starting_sequence)
SELECT v.code, v.name, c.item_category_id, lower(v.code), lower(regexp_replace(v.name, '[^a-zA-Z0-9]', '', 'g')), v.seq
FROM (VALUES
    ('AA',   'RR WMS Assets (trial) Proto Type',     'SOFTWARE', 0),
    ('AC',   'Air Compressor, Engine Driven',         'MACHINE',  64),
    ('AM',   'Asphalt Mixing Plant',                  'MACHINE',  2),
    ('AP',   'Asphalt Paver Track',                   'MACHINE',  9),
    ('AT',   'Articulated Dump Truck (Off-Highway)',  'VEHICLE',  22),
    ('BA',   'Boat Anchoring',                        'TOOL',     1),
    ('BB',   'Trailer - Beam Bed',                    'VEHICLE',  1),
    ('BC',   'Battery Charger',                       'TOOL',     10),
    ('BG',   'Pontoon Unit',                           'OTHER',    1),
    ('BL',   'Backhoe Loader',                        'MACHINE',  44),
    ('BP',   'Batching Plant',                        'MACHINE',  8),
    ('BT',   'Tractor Trailer - Bowser',              'VEHICLE',  18),
    ('BW',   'Weigh Bridge',                           'MACHINE',  8),
    ('CC',   'Crawler Crane',                         'MACHINE',  8),
    ('CM',   'Concrete Mixer',                        'MACHINE',  30),
    ('CN',   'Convertainer (Office container)',       'OTHER',    115),
    ('CNP',  'Concrete Paver',                        'MACHINE',  2),
    ('CP',   'Screener Crusher',                      'MACHINE',  16),
    ('CR',   'Vibratory Soil Compactor (Roller)',     'MACHINE',  48),
    ('CS',   'Cola/Chip Sprayer',                     'MACHINE',  5),
    ('CSJ',  'Cable Stressing Jack',                  'TOOL',     5),
    ('CW',   'Chiller Water Plant',                   'MACHINE',  3),
    ('DC',   'Barrel Heating Decanter',                'MACHINE',  2),
    ('DP',   'Dredger Pump',                          'MACHINE',  4),
    ('DR',   'Vibratory Hand Roller',                 'MACHINE',  38),
    ('DT',   'Dump Truck',                            'VEHICLE',  118),
    ('EC',   'Air Compressor, Electrical',             'MACHINE',  18),
    ('EP',   'Exploder, Blasting',                    'TOOL',     4),
    ('EX',   'Crawler Excavator',                     'MACHINE',  124),
    ('FD',   'Face Recognition Terminal',              'TOOL',     60),
    ('FL',   'Fork Lift',                              'MACHINE',  7),
    ('FT',   'Diesel Storage Tank',                    'OTHER',    20),
    ('GS',   'Guard Tour System',                      'TOOL',     17),
    ('HB',   'Trailer - High Bed',                     'VEHICLE',  2),
    ('HJ',   'Hydraulic Jack',                         'TOOL',     0),
    ('HT',   'Heavy Truck',                            'VEHICLE',  10),
    ('JH',   'Jack Hammer',                            'TOOL',     44),
    ('LB',   'Trailer - Low Bed',                      'VEHICLE',  2),
    ('LG',   'Lighting Generator/Tower',                'MACHINE',  63),
    ('LM',   'Tractor 2-Wheeler',                      'MACHINE',  1),
    ('LT',   'Lighting Tower',                         'MACHINE',  4),
    ('MB',   'Motor Cycle',                            'VEHICLE',  76),
    ('MC',   'Mobile Crane',                           'MACHINE',  7),
    ('MG',   'Motor Grader',                           'MACHINE',  14),
    ('MP',   'Concrete Pump',                          'MACHINE',  238),
    ('PC',   'Plate Compactor',                        'MACHINE',  12),
    ('PG',   'Power Generator',                        'MACHINE',  81),
    ('PH',   'Piling Hammer',                          'MACHINE',  9),
    ('PM',   'Prime Mover',                            'VEHICLE',  7),
    ('PR',   'Piling Rig',                             'MACHINE',  1),
    ('PT',   'Concrete Pump Car',                      'VEHICLE',  1),
    ('PV',   'Engine, Poker Vibrator',                  'MACHINE',  198),
    ('RB',   'Hydraulic Rock Breaker',                  'MACHINE',  37),
    ('RS',   'Road Sweeper',                           'MACHINE',  7),
    ('SL',   'Skid Steer Loader',                      'MACHINE',  32),
    ('SM',   'Soil Mixing Plant',                      'MACHINE',  1),
    ('SP',   'Submersible Pump',                       'MACHINE',  202),
    ('ST',   'Sprayer Tank, Chemical',                 'OTHER',    22),
    ('SV',   'Boom Truck',                             'VEHICLE',  151),
    ('SWP',  'Sand Washing Machine',                   'MACHINE',  2),
    ('TAG',  'Angle Grinder',                          'TOOL',     359),
    ('TAH',  'Air Hammer',                             'TOOL',     1),
    ('TAM',  'Digital Anemometer',                     'TOOL',     1),
    ('TB',   'Tar Bowser',                             'VEHICLE',  7),
    ('TBD',  'Bench Drill',                            'TOOL',     5),
    ('TCD',  'Diamond Core Drill',                     'TOOL',     2),
    ('TCM',  'Digital Clamp Meter',                    'TOOL',     23),
    ('TCO',  'Cut Off Grinder',                        'TOOL',     58),
    ('TCS',  'Circular Saw',                           'TOOL',     115),
    ('TD',   'Track Drill',                            'MACHINE',  7),
    ('TDG',  'Dial Gauge',                             'TOOL',     3),
    ('TDIG', 'Die Grinder',                            'TOOL',     1),
    ('TDT',  'Ferrodetector',                          'TOOL',     1),
    ('TED',  'Electric Hand Drill',                    'TOOL',     103),
    ('TEP',  'Electric Planer',                        'TOOL',     2),
    ('THB',  'Demolition Hammer',                      'TOOL',     42),
    ('THD',  'Rotary Hammer',                          'TOOL',     103),
    ('THT',  'Digital Hydraulic Tester',                'TOOL',     1),
    ('THW',  'Torque Wrench',                          'TOOL',     1),
    ('TIT',  'Insulation Tester',                      'TOOL',     2),
    ('TIW',  'Impact Wrench',                          'TOOL',     29),
    ('TJS',  'Jig Saw',                                'TOOL',     15),
    ('TL',   'Tractor Trailer, Tipping',               'VEHICLE',  10),
    ('TM',   'Concrete Transit Mixer',                  'VEHICLE',  26),
    ('TMD',  'Magnet Drill',                            'TOOL',     7),
    ('TMM',  'Digital Multi Meter',                    'TOOL',     26),
    ('TPG',  'Pneumatic Grinder',                      'TOOL',     2),
    ('TR',   'Pneumatic Tired Roller',                  'MACHINE',  14),
    ('TRS',  'Radar Level Sensor',                     'TOOL',     2),
    ('TSM',  'Sander Machine',                         'TOOL',     3),
    ('TT',   'Tractor 4-Wheeler',                      'VEHICLE',  16),
    ('TTD',  'Digital Tachometer',                     'TOOL',     1),
    ('TTM',  'Review Item (pending classification)',   'REVIEW',   4),
    ('TVC',  'Vehicle Scanner',                        'TOOL',     2),
    ('TWSD', 'Wind Speed/Direction Sensor',             'TOOL',     2),
    ('VM',   'Vibratory Motor',                        'MACHINE',  72),
    ('VR',   'Vibratory Rammer',                       'MACHINE',  132),
    ('WB',   'Water Bowser',                           'VEHICLE',  19),
    ('WE',   'Winch, Electric',                        'TOOL',     6),
    ('WG',   'Welding Generator',                      'MACHINE',  48),
    ('WL',   'Wheel Loader',                           'MACHINE',  11),
    ('WP',   'Water Pump, Mechanical',                  'MACHINE',  192),
    ('WR',   'Chain Roller',                           'REVIEW',   0),
    ('WT',   'Welding Transformer',                     'MACHINE',  65),

    -- Office/IT Equipment (OE) sub-types
    ('OE-CM', 'Office Equipment - Camera',              'TOOL', 144),
    ('OE-DC', 'Office Equipment - Desktop Computer',    'TOOL', 253),
    ('OE-FX', 'Office Equipment - Fax Machine',          'TOOL', 4),
    ('OE-MO', 'Office Equipment - Monitor',             'TOOL', 245),
    ('OE-MP', 'Office Equipment - Multimedia Projector', 'TOOL', 1),
    ('OE-NC', 'Office Equipment - Notebook/Laptop',      'TOOL', 171),
    ('OE-NS', 'Office Equipment - Notebook',             'TOOL', 126),
    ('OE-PC', 'Office Equipment - Photocopier',          'TOOL', 36),
    ('OE-PR', 'Office Equipment - Printer',              'TOOL', 145),
    ('OE-SC', 'Office Equipment - Scanner',              'TOOL', 65),
    ('OE-SE', 'Office Equipment - Server',               'TOOL', 48),

    -- Lab Instruments (LI) sub-types
    ('LI-AI',  'Lab Instrument - Aggregate Impact Value',   'TOOL', 4),
    ('LI-CBR', 'Lab Instrument - CBR Test Machine',         'TOOL', 6),
    ('LI-CC',  'Lab Instrument - Core Cutter',              'TOOL', 9),
    ('LI-CE',  'Lab Instrument - Centrifuge Extractor',     'TOOL', 6),
    ('LI-CF',  'Lab Instrument - Flash Point Tester',       'TOOL', 2),
    ('LI-CT',  'Lab Instrument - Compression Testing Machine', 'TOOL', 13),
    ('LI-DM',  'Lab Instrument - Ductility Test Machine',   'TOOL', 3),
    ('LI-DP',  'Lab Instrument - Dynamic Cone Penetrometer', 'TOOL', 8),
    ('LI-FC',  'Lab Instrument - Flow Cone',                'TOOL', 5),
    ('LI-FP',  'Lab Instrument - Filter Press',             'TOOL', 1),
    ('LI-GF',  'Lab Instrument - Specific Gravity Frame',   'TOOL', 6),
    ('LI-GT',  'Lab Instrument - Specific Gravity Test Equipment', 'TOOL', 5),
    ('LI-LA',  'Lab Instrument - Los Angeles Abrasion Machine', 'TOOL', 1),
    ('LI-MC',  'Lab Instrument - Marshall Compactor',       'TOOL', 6),
    ('LI-MM',  'Lab Instrument - Mortar Mixer',             'TOOL', 2),
    ('LI-MS',  'Lab Instrument - Marshall Stability',       'TOOL', 6),
    ('LI-OV',  'Lab Instrument - Oven',                     'TOOL', 25),
    ('LI-PH',  'Lab Instrument - Proctor Hammer/Soil Compactor', 'TOOL', 6),
    ('LI-SE',  'Lab Instrument - Sand Equivalent Apparatus', 'TOOL', 1),
    ('LI-SP',  'Lab Instrument - Softening Point Apparatus', 'TOOL', 1),
    ('LI-SS',  'Lab Instrument - Sieve Shaker',             'TOOL', 2),
    ('LI-TH',  'Lab Instrument - Schmidt Hammer',           'TOOL', 1),
    ('LI-WB',  'Lab Instrument - Water Bath',               'TOOL', 8),
    ('LI-WS',  'Lab Instrument - Weighing Scale',           'TOOL', 99),

    -- Survey Instruments (SI) sub-types
    ('SI-AL', 'Survey Instrument - Auto Level',            'TOOL', 84),
    ('SI-MP', 'Survey Instrument - Mini Prism',             'TOOL', 12),
    ('SI-TL', 'Survey Instrument - Theodolite',             'TOOL', 1),
    ('SI-TS', 'Survey Instrument - Total Station',          'TOOL', 23),

    -- Household Appliances (HH) sub-types
    ('HH-AC', 'Household Appliance - Air Conditioner',      'MACHINE', 80),
    ('HH-RE', 'Household Appliance - Refrigerator',         'MACHINE', 21),
    ('HH-TE', 'Household Appliance - Television',           'MACHINE', 4),
    ('HH-WM', 'Household Appliance - Washing Machine',      'MACHINE', 14),

    -- Landslide Equipment (LS) sub-types
    ('LS-GM',  'Landslide Equipment - Grout Mixer',          'MACHINE', 4),
    ('LS-GP',  'Landslide Equipment - Grout Pump',           'MACHINE', 14),
    ('LS-SC',  'Landslide Equipment - Shotcrete Machine',    'MACHINE', 3),
    ('LS-SNM', 'Landslide Equipment - Soil Nail Machine',    'MACHINE', 12),
    ('LS-SF',  'Landslide Equipment - Safety Kit',           'MACHINE', 10)
-- category_key here is 'MACHINE'/'VEHICLE'/etc — happens to equal the category
-- name (just cased differently), which is all lower(name_slug) matching needs.
) AS v(code, name, category_key, seq)
JOIN item_category c ON c.item_type_id = 1 AND c.name_slug = lower(v.category_key)
WHERE NOT EXISTS (
    SELECT 1 FROM item_subcategory s
    WHERE s.item_category_id = c.item_category_id AND s.item_subcategory_code = v.code
)
AND NOT EXISTS (
    SELECT 1 FROM item_subcategory s
    WHERE s.item_category_id = c.item_category_id AND s.name_slug = lower(regexp_replace(v.name, '[^a-zA-Z0-9]', '', 'g'))
);
