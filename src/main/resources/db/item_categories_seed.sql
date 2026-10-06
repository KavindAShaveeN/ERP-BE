-- Item categories and sub categories from Categories and Sub categories.xlsx
-- (36 categories, 210 sub categories).
-- Uses the live table/column names (item_category, item_subcategory) as in dummy_item_seed_data.sql.
--
-- item_type_id convention: 1 = Asset, 2 = Consumable, 3 = Inventory.
-- Set v_item_type in the DO block below (default 2 = Consumable) -- it is the only place to change.
--
-- Two codes in the spreadsheet collided with the unique constraints and were changed here:
--   * category "Fencing and Safety Materials": sheet code FSM (already used by "Formwork & Shuttering
--     Materials") -> FNS
--   * sub category "SS Pipes" under Structural Steel: sheet code SSP (already used by "SS Plates") -> SPP
-- Names/codes are otherwise exactly as in the sheet (typos such as "Hydrailuc" and "Fastners" included).
--
-- Safe to re-run: every INSERT is guarded with WHERE NOT EXISTS.

BEGIN;

CREATE TEMP TABLE seed_item_category (
    category_code VARCHAR(50)  NOT NULL,
    category_name VARCHAR(150) NOT NULL,
    sub_code      VARCHAR(50)  NOT NULL,
    sub_name      VARCHAR(150) NOT NULL
) ON COMMIT DROP;

INSERT INTO seed_item_category (category_code, category_name, sub_code, sub_name)
VALUES
    ('CMC', 'Cement & Concrete', 'CEM', 'Cement'),
    ('CMC', 'Cement & Concrete', 'RMC', 'Ready-Mix Concrete'),
    ('CMC', 'Cement & Concrete', 'GRT', 'Grout'),
    ('CMC', 'Cement & Concrete', 'ADM', 'Adhesive and mortar'),
    ('AQM', 'Aggregates & Quarry Materials', 'AGG', 'Aggregate'),
    ('AQM', 'Aggregates & Quarry Materials', 'SAN', 'Sand'),
    ('AQM', 'Aggregates & Quarry Materials', 'BLD', 'Boulders'),
    ('AQM', 'Aggregates & Quarry Materials', 'GRV', 'Gravel'),
    ('AQM', 'Aggregates & Quarry Materials', 'MTL', 'Metal'),
    ('PCP', 'Precast Products', 'BLK', 'Blocks'),
    ('PCP', 'Precast Products', 'KRB', 'Kerb'),
    ('PCP', 'Precast Products', 'PVB', 'Paving Blocks (Interlocking)'),
    ('PCP', 'Precast Products', 'PCB', 'Precast Beams'),
    ('PCP', 'Precast Products', 'HMP', 'Hume Pipes'),
    ('RFS', 'Reinforcement Steel', 'TSB', 'Tor Steel Bars'),
    ('RFS', 'Reinforcement Steel', 'RDB', 'Round Bars'),
    ('RFS', 'Reinforcement Steel', 'SLC', 'Steel Coils'),
    ('RFS', 'Reinforcement Steel', 'WWM', 'Welded Wire Mesh'),
    ('RFS', 'Reinforcement Steel', 'BDW', 'Binding Wire'),
    ('RFS', 'Reinforcement Steel', 'PSS', 'Prestressing Strand'),
    ('RFS', 'Reinforcement Steel', 'PSA', 'Prestressing Accessories'),
    ('STS', 'Structural Steel', 'ANG', 'Angles'),
    ('STS', 'Structural Steel', 'CHN', 'Channels'),
    ('STS', 'Structural Steel', 'MSB', 'MS Box Iron'),
    ('STS', 'Structural Steel', 'SSB', 'SS Box Iron'),
    ('STS', 'Structural Steel', 'HBM', 'H-Beams'),
    ('STS', 'Structural Steel', 'FLT', 'Flats'),
    ('STS', 'Structural Steel', 'MSP', 'MS Plates'),
    ('STS', 'Structural Steel', 'CQP', 'Chequered Plates'),
    ('STS', 'Structural Steel', 'SSP', 'SS Plates'),
    ('STS', 'Structural Steel', 'ALP', 'Aluminum Plates'),
    ('STS', 'Structural Steel', 'HXP', 'Hardox Plates'),
    ('STS', 'Structural Steel', 'ZCS', 'Zinc Coated Sheets'),
    ('STS', 'Structural Steel', 'HLS', 'Hollow Shaft'),
    ('STS', 'Structural Steel', 'STP', 'Steel Pipes'),
    ('STS', 'Structural Steel', 'GIP', 'GI Pipes'),
    ('STS', 'Structural Steel', 'SPP', 'SS Pipes'),
    ('FCM', 'Fabrication & Connection Materials', 'NBP', 'Nut and Bolts'),
    ('FCM', 'Fabrication & Connection Materials', 'ABT', 'Anchor Bolts'),
    ('FCM', 'Fabrication & Connection Materials', 'CHA', 'Chemical Anchors'),
    ('FCM', 'Fabrication & Connection Materials', 'EXA', 'Expansion Anchors'),
    ('FCM', 'Fabrication & Connection Materials', 'THB', 'Thread Bars'),
    ('FCM', 'Fabrication & Connection Materials', 'WEL', 'Welding Electrodes'),
    ('FCM', 'Fabrication & Connection Materials', 'DSW', 'Discs and Wheels'),
    ('FCM', 'Fabrication & Connection Materials', 'WCA', 'Welding & Cutting Accessories'),
    ('FCM', 'Fabrication & Connection Materials', 'DRB', 'Drill Bits'),
    ('FCM', 'Fabrication & Connection Materials', 'FST', 'Fastners'),
    ('FSM', 'Formwork & Shuttering Materials', 'TMB', 'Timber'),
    ('FSM', 'Formwork & Shuttering Materials', 'PWB', 'Plywood Boards'),
    ('FSM', 'Formwork & Shuttering Materials', 'SHB', 'Shutter Boards'),
    ('FSM', 'Formwork & Shuttering Materials', 'SCF', 'Scaffolding'),
    ('FSM', 'Formwork & Shuttering Materials', 'BSJ', 'Base Jacks'),
    ('FSM', 'Formwork & Shuttering Materials', 'TRD', 'Tie Rods'),
    ('FSM', 'Formwork & Shuttering Materials', 'FMA', 'Formwork Accessories'),
    ('ACC', 'Admixtures and Construction Chemicles', 'CAD', 'Concrete Admixtures'),
    ('ACC', 'Admixtures and Construction Chemicles', 'SLN', 'Sealants'),
    ('BAM', 'Bitumen & Asphalt Materials', 'BME', 'Bitumen and Emulsion'),
    ('BAM', 'Bitumen & Asphalt Materials', 'ASP', 'Asphalt'),
    ('BAM', 'Bitumen & Asphalt Materials', 'BTS', 'Bitumen Sealant'),
    ('BFM', 'Building Finishing Materials', 'PLI', 'Plastering Items'),
    ('BFM', 'Building Finishing Materials', 'TLE', 'Tile'),
    ('BFM', 'Building Finishing Materials', 'SNI', 'Sanitary Items'),
    ('BFM', 'Building Finishing Materials', 'SNF', 'Sanitary Fitiings'),
    ('PNT', 'Paint', 'PRM', 'Primer'),
    ('PNT', 'Paint', 'EMP', 'Emulsion Paint'),
    ('PNT', 'Paint', 'ENP', 'Enamel Paint'),
    ('PNT', 'Paint', 'MRP', 'Marine Paint'),
    ('PNT', 'Paint', 'ACC', 'Anti-corrosion Coating'),
    ('PNT', 'Paint', 'RMP', 'Road Marking Paint'),
    ('PNT', 'Paint', 'THN', 'Thinner'),
    ('PNT', 'Paint', 'WDP', 'Wood Paint'),
    ('PNT', 'Paint', 'PAC', 'Paint Accessories (Brush, Sand Paper)'),
    ('RFM', 'Roofing Materials', 'ARS', 'Aluminium Roofing Sheets'),
    ('RFM', 'Roofing Materials', 'BRS', 'Asbastos Roofing Sheets'),
    ('RFM', 'Roofing Materials', 'CLS', 'Ceiling Sheets'),
    ('RFM', 'Roofing Materials', 'RDC', 'Ridge Caps'),
    ('RFM', 'Roofing Materials', 'FLS', 'Flashings'),
    ('RFM', 'Roofing Materials', 'GTR', 'Gutters'),
    ('RFM', 'Roofing Materials', 'DNP', 'Downpipes'),
    ('RFM', 'Roofing Materials', 'SDS', 'Self-drilling Screws'),
    ('PWS', 'Pipes – Water Supply', 'PVP', 'PVC Pipes'),
    ('PWS', 'Pipes – Water Supply', 'PPF', 'PVC Pipe Fittings'),
    ('PWS', 'Pipes – Water Supply', 'HDP', 'HDPE Pipes'),
    ('PWS', 'Pipes – Water Supply', 'HPF', 'HDPE Pipe Fittings'),
    ('PWS', 'Pipes – Water Supply', 'MHC', 'Manhole Covers'),
    ('PWS', 'Pipes – Water Supply', 'GRT', 'Gratings'),
    ('ELM', 'Electrical Materials', 'BAT', 'Battery'),
    ('ELM', 'Electrical Materials', 'ELC', 'Electrical Cables'),
    ('ELM', 'Electrical Materials', 'ELF', 'Electrical Fittings'),
    ('ELM', 'Electrical Materials', 'CND', 'Conduits'),
    ('ELM', 'Electrical Materials', 'JNB', 'Junction Boxes'),
    ('ELM', 'Electrical Materials', 'DBR', 'Distribution Boards'),
    ('ELM', 'Electrical Materials', 'ISO', 'Isolators'),
    ('ELM', 'Electrical Materials', 'MCB', 'MCB'),
    ('ELM', 'Electrical Materials', 'RCC', 'RCCB'),
    ('ELM', 'Electrical Materials', 'LGT', 'Lights'),
    ('ELM', 'Electrical Materials', 'LFT', 'Light Fittings'),
    ('ELM', 'Electrical Materials', 'SWT', 'Switches'),
    ('ELM', 'Electrical Materials', 'INT', 'Insulation Tapes'),
    ('GTM', 'Geotechnical Materials', 'GBN', 'Gabion'),
    ('GTM', 'Geotechnical Materials', 'GTX', 'Geotextile'),
    ('GTM', 'Geotechnical Materials', 'GGD', 'Geogrid'),
    ('GTM', 'Geotechnical Materials', 'GCP', 'Geocomposite'),
    ('GTM', 'Geotechnical Materials', 'SNL', 'Soil Nails'),
    ('GTM', 'Geotechnical Materials', 'SHP', 'Sheet Piles'),
    ('GTM', 'Geotechnical Materials', 'BTN', 'Bentonite'),
    ('GTM', 'Geotechnical Materials', 'RFM', 'Rock Falling Mesh'),
    ('FNS', 'Fencing and Safety Materials', 'CLM', 'Chain Link Mesh'),
    ('FNS', 'Fencing and Safety Materials', 'RDS', 'Road Signs'),
    ('FNS', 'Fencing and Safety Materials', 'GRL', 'Guard Rail'),
    ('FNS', 'Fencing and Safety Materials', 'SGP', 'Sign Posts'),
    ('FNS', 'Fencing and Safety Materials', 'RST', 'Road Studs'),
    ('FNS', 'Fencing and Safety Materials', 'TFC', 'Traffic Cones'),
    ('FNS', 'Fencing and Safety Materials', 'BRC', 'Barricades'),
    ('FNS', 'Fencing and Safety Materials', 'SFB', 'Safety Barriers'),
    ('FNS', 'Fencing and Safety Materials', 'TPM', 'Thermoplastic Marking'),
    ('FNS', 'Fencing and Safety Materials', 'SMR', 'Safety Mirrors'),
    ('FNS', 'Fencing and Safety Materials', 'FRE', 'Fire Extinguisher'),
    ('FNS', 'Fencing and Safety Materials', 'PRC', 'Protective Clothing'),
    ('FLN', 'Fuel and Lubricant', 'FUL', 'Fuel'),
    ('FLN', 'Fuel and Lubricant', 'ENO', 'Engine Oil'),
    ('FLN', 'Fuel and Lubricant', 'GRO', 'Gear Oil'),
    ('FLN', 'Fuel and Lubricant', 'HYO', 'Hydraulic'),
    ('FLN', 'Fuel and Lubricant', 'TRO', 'Transmission Oil'),
    ('FLN', 'Fuel and Lubricant', 'GRS', 'Grease'),
    ('FLN', 'Fuel and Lubricant', 'CLT', 'Coolant'),
    ('FLT', 'Filter', 'OFL', 'Oil Filter'),
    ('FLT', 'Filter', 'FFL', 'Fuel Filter'),
    ('FLT', 'Filter', 'HFL', 'Hydrailuc Filter'),
    ('FLT', 'Filter', 'WSP', 'Water Separator'),
    ('FLT', 'Filter', 'AFL', 'Air Filter'),
    ('FLT', 'Filter', 'ACF', 'AC Filter'),
    ('TYR', 'Tyre', 'TYR', 'Tyre'),
    ('TYR', 'Tyre', 'TUB', 'Tube'),
    ('TYR', 'Tyre', 'FLP', 'Flap'),
    ('TYR', 'Tyre', 'RIM', 'Rim'),
    ('BRG', 'Bearing', 'BBR', 'Ball Bearing'),
    ('BRG', 'Bearing', 'CRB', 'Cylindrical Roller Bearing'),
    ('BRG', 'Bearing', 'NRB', 'Needle Roller Bearing'),
    ('BRG', 'Bearing', 'SRB', 'Spherical Roller Bearing'),
    ('BRG', 'Bearing', 'TRB', 'Taper Roller Bearing'),
    ('SNL', 'Survey and Lab', 'SVE', 'Survey Equipment'),
    ('SNL', 'Survey and Lab', 'LBE', 'Lab Equipment'),
    ('SNL', 'Survey and Lab', 'SVC', 'Service and Calibration'),
    ('EXS', 'Excavator Spares', 'CAT', 'Cat'),
    ('EXS', 'Excavator Spares', 'KOB', 'Kobelco'),
    ('EXS', 'Excavator Spares', 'HIT', 'Hitachi'),
    ('EXS', 'Excavator Spares', 'KOM', 'Komatsu'),
    ('EXS', 'Excavator Spares', 'DEV', 'Devlon'),
    ('EXS', 'Excavator Spares', 'YAN', 'Yanmar'),
    ('EXS', 'Excavator Spares', 'DOS', 'Doosan'),
    ('BLS', 'Backhoe Loader Spares', 'CAT', 'Cat'),
    ('BLS', 'Backhoe Loader Spares', 'JCB', 'JCB'),
    ('WLS', 'Wheel Loader Spares', 'CAT', 'Cat'),
    ('WLS', 'Wheel Loader Spares', 'SEM', 'SEM'),
    ('WLS', 'Wheel Loader Spares', 'HIT', 'Hitachi'),
    ('SLS', 'Skid Loader Spares', 'CAT', 'Cat'),
    ('SLS', 'Skid Loader Spares', 'BOB', 'Bobcat'),
    ('MGS', 'Motor Grader Spares', 'CAT', 'Cat'),
    ('MGS', 'Motor Grader Spares', 'SEM', 'SEM'),
    ('MGS', 'Motor Grader Spares', 'MIT', 'Mitsubishi'),
    ('MGS', 'Motor Grader Spares', 'HDR', 'Hidromek'),
    ('MGS', 'Motor Grader Spares', 'KOM', 'Komatsu'),
    ('MCS', 'Mobile Crane Spares', 'TAD', 'Tadano'),
    ('MCS', 'Mobile Crane Spares', 'XCM', 'XCMG'),
    ('MCS', 'Mobile Crane Spares', 'KOB', 'Kobelco'),
    ('CCS', 'Crawler Crane Spares', 'SUM', 'Sumitomo'),
    ('CCS', 'Crawler Crane Spares', 'KOB', 'Kobelco'),
    ('CCS', 'Crawler Crane Spares', 'SNY', 'Sany'),
    ('APS', 'Asphalt Paver Spares', 'CAT', 'Cat'),
    ('APS', 'Asphalt Paver Spares', 'VLV', 'Volvo'),
    ('APS', 'Asphalt Paver Spares', 'SUM', 'Sumitomo'),
    ('CRS', 'Roller Spares', 'CAT', 'Cat'),
    ('CRS', 'Roller Spares', 'DYN', 'Dynapac'),
    ('CRS', 'Roller Spares', 'SAK', 'Sakai'),
    ('CRS', 'Roller Spares', 'XCM', 'XCMG'),
    ('CRS', 'Roller Spares', 'YUT', 'Yutong'),
    ('CRS', 'Roller Spares', 'FRK', 'Furukawa'),
    ('CPS', 'Crusher Plant Spares', 'SND', 'Sandvik'),
    ('CPS', 'Crusher Plant Spares', 'NRB', 'Nordberg'),
    ('CPS', 'Crusher Plant Spares', 'NKY', 'Nakayama'),
    ('CPS', 'Crusher Plant Spares', 'CVB', 'Conveyor Belt'),
    ('CPS', 'Crusher Plant Spares', 'SCM', 'Screen Mesh'),
    ('CPS', 'Crusher Plant Spares', 'CVR', 'Conveyor Rollers'),
    ('PRS', 'Piling Rig Machine Spares', 'BAU', 'Bauer'),
    ('TKS', 'Truck Spares', 'DTS', 'Dump Truck'),
    ('TKS', 'Truck Spares', 'TMS', 'Truck Mixture'),
    ('TKS', 'Truck Spares', 'PMS', 'Prime Movers'),
    ('TKS', 'Truck Spares', 'HTS', 'Heavy Truck'),
    ('TKS', 'Truck Spares', 'WBS', 'Water Bowser'),
    ('TKS', 'Truck Spares', 'TBS', 'Bitumen Distributor'),
    ('TKS', 'Truck Spares', 'PTS', 'Concrete Pump Car'),
    ('SVS', 'Supporting Vehicle Spares', 'DBC', 'Double Cabs'),
    ('SVS', 'Supporting Vehicle Spares', 'CRC', 'Crew Cabs'),
    ('SVS', 'Supporting Vehicle Spares', 'BMT', 'Boom Trucks'),
    ('SVS', 'Supporting Vehicle Spares', 'DMB', 'Dimo Batta'),
    ('SVS', 'Supporting Vehicle Spares', 'CNT', 'Canter Truck'),
    ('SVS', 'Supporting Vehicle Spares', 'TWL', 'Three Wheeler'),
    ('SVS', 'Supporting Vehicle Spares', 'MTC', 'Motor Cycles'),
    ('PTS', 'Plant Spares', 'BPS', 'Concrete Batching Plant'),
    ('PTS', 'Plant Spares', 'AMS', 'Asphalt Batching Plant'),
    ('PTS', 'Plant Spares', 'SWP', 'Sand Washing Plant'),
    ('PTS', 'Plant Spares', 'ACS', 'Air Compressors'),
    ('PTS', 'Plant Spares', 'PGS', 'Power Generators'),
    ('PTS', 'Plant Spares', 'WGS', 'Welding Generators'),
    ('PTS', 'Plant Spares', 'WTS', 'Welding Transformers'),
    ('PTS', 'Plant Spares', 'MPS', 'Miscellaneous Plants'),
    ('COM', 'Consumables and Other Materials', 'STN', 'Stationery'),
    ('COM', 'Consumables and Other Materials', 'TNR', 'Toner'),
    ('COM', 'Consumables and Other Materials', 'RBP', 'Rubber Pads');

DO $$
DECLARE
    v_item_type INTEGER := 2;  -- ITEM TYPE: 1 = Asset, 2 = Consumable, 3 = Inventory
BEGIN
    -- Categories
    INSERT INTO item_category (item_type_id, item_category_code, item_category_name, code_slug, name_slug)
    SELECT v_item_type, d.category_code, d.category_name,
           regexp_replace(lower(trim(d.category_code)), '[^a-z0-9]', '', 'g'),
           regexp_replace(lower(trim(d.category_name)), '[^a-z0-9]', '', 'g')
    FROM (SELECT DISTINCT category_code, category_name FROM seed_item_category) d
    WHERE NOT EXISTS (
        SELECT 1 FROM item_category c
        WHERE c.item_type_id = v_item_type AND c.item_category_code = d.category_code
    );

    -- Sub categories
    INSERT INTO item_subcategory (item_subcategory_code, item_subcategory_name, item_category_id, code_slug, name_slug)
    SELECT s.sub_code, s.sub_name, c.item_category_id,
           regexp_replace(lower(trim(s.sub_code)), '[^a-z0-9]', '', 'g'),
           regexp_replace(lower(trim(s.sub_name)), '[^a-z0-9]', '', 'g')
    FROM seed_item_category s
    JOIN item_category c ON c.item_type_id = v_item_type AND c.item_category_code = s.category_code
    WHERE NOT EXISTS (
        SELECT 1 FROM item_subcategory x
        WHERE x.item_category_id = c.item_category_id AND x.item_subcategory_code = s.sub_code
    );
END $$;

COMMIT;

-- Check: expect 36 categories and 210 sub categories from this script
-- (CMC and its 4 sub categories are included, so the earlier cement_concrete_category_seed.sql is not needed).
-- SELECT c.item_category_code, c.item_category_name, count(s.*) AS subs
-- FROM item_category c LEFT JOIN item_subcategory s ON s.item_category_id = c.item_category_id
-- GROUP BY 1,2 ORDER BY 1;
