-- Asset registration schema for the remaining asset classes not yet covered by
-- asset_vehicle_schema.sql (Vehicle) or asset_other_classes_schema.sql (Furniture,
-- IT Equipment, Plant Equipment, Power Tools, Electrical Equipment, Machinery):
-- Survey Instrument, Lab Equipment, Reusable Tool, Building, Land, and Other.
--
-- Follows the same pattern as the other two files: each class gets its own
-- *_asset_detail table, 1:1 with the shared "asset" table via asset_code.
--
-- PREREQUISITE: run asset_vehicle_schema.sql first (creates the shared "asset" table).
--
-- Safe to re-run: every statement is guarded (IF NOT EXISTS).

CREATE TABLE IF NOT EXISTS survey_instrument_asset_detail (
    asset_code                  VARCHAR(20)   PRIMARY KEY REFERENCES asset (asset_code) ON DELETE CASCADE,

    -- Identification Details
    instrument_type                VARCHAR(50)   NOT NULL,
    make                           VARCHAR(100),
    model                          VARCHAR(100),
    manufacturer_serial_number     VARCHAR(100),
    country_of_manufacture         VARCHAR(100),
    year_of_manufacture            INT,

    -- Technical Specifications
    measurement_range              VARCHAR(100),
    accuracy                       VARCHAR(50),
    unit_of_measurement            VARCHAR(50),
    power_source                   VARCHAR(20),
    battery_type                   VARCHAR(50),

    -- Calibration Details
    calibration_date               DATE,
    calibration_due_date           DATE,
    calibration_certificate_number VARCHAR(100),
    calibration_certificate_document VARCHAR(255),

    -- Accessories
    carrying_case_included         BOOLEAN,
    included_accessories           VARCHAR(500),

    -- Purchase and Ownership Details
    supplier                       VARCHAR(150),
    purchase_date                  DATE,
    purchase_value                 NUMERIC(14, 2),
    warranty_expiry_date           DATE,
    supporting_documents           VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS lab_equipment_asset_detail (
    asset_code                  VARCHAR(20)   PRIMARY KEY REFERENCES asset (asset_code) ON DELETE CASCADE,

    -- Identification Details
    equipment_type                 VARCHAR(50)   NOT NULL,
    make                           VARCHAR(100),
    model                          VARCHAR(100),
    manufacturer_serial_number     VARCHAR(100),
    country_of_manufacture         VARCHAR(100),
    year_of_manufacture            INT,

    -- Technical Specifications
    capacity                       VARCHAR(50),
    capacity_unit                  VARCHAR(50),
    accuracy                       VARCHAR(50),
    operating_temperature_range    VARCHAR(50),
    power_rating                   NUMERIC(10, 2),
    voltage                        NUMERIC(10, 2),

    -- Calibration and Safety
    calibration_date               DATE,
    calibration_due_date           DATE,
    calibration_certificate_number VARCHAR(100),
    calibration_certificate_document VARCHAR(255),
    safety_certification           VARCHAR(150),

    -- Accessories
    included_accessories           VARCHAR(500),

    -- Purchase and Ownership Details
    supplier                       VARCHAR(150),
    purchase_date                  DATE,
    purchase_value                 NUMERIC(14, 2),
    warranty_expiry_date           DATE,
    supporting_documents           VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS reusable_tool_asset_detail (
    asset_code                  VARCHAR(20)   PRIMARY KEY REFERENCES asset (asset_code) ON DELETE CASCADE,

    -- Identification Details
    tool_type                      VARCHAR(50)   NOT NULL,
    make                           VARCHAR(100),
    model                          VARCHAR(100),
    manufacturer_serial_number     VARCHAR(100),
    material                       VARCHAR(100),
    size_dimensions                VARCHAR(100),
    weight_kg                      NUMERIC(10, 2),
    quantity_in_set                INT,

    -- Storage and Condition
    storage_location                VARCHAR(150),
    condition_notes                 VARCHAR(500),
    included_accessories            VARCHAR(500),

    -- Purchase and Ownership Details
    supplier                       VARCHAR(150),
    purchase_date                  DATE,
    purchase_value                 NUMERIC(14, 2),
    warranty_expiry_date           DATE,
    supporting_documents           VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS building_asset_detail (
    asset_code                  VARCHAR(20)   PRIMARY KEY REFERENCES asset (asset_code) ON DELETE CASCADE,

    -- Identification Details
    building_type                  VARCHAR(50)   NOT NULL,
    address_location               VARCHAR(255)  NOT NULL,
    land_area                      NUMERIC(14, 2),
    floor_area_sqft                NUMERIC(14, 2),
    number_of_floors               INT,
    year_built                     INT,
    construction_type              VARCHAR(100),

    -- Ownership and Legal Details
    ownership_type                  VARCHAR(50),
    title_deed_number               VARCHAR(100),
    registration_number             VARCHAR(100),
    encumbrances                    VARCHAR(255),

    -- Valuation Details
    valuation_amount                NUMERIC(16, 2),
    valuation_date                  DATE,

    -- Insurance Details
    insurance_company               VARCHAR(150),
    insurance_policy_number         VARCHAR(100),
    insurance_expiry_date           DATE,

    -- Occupancy and Purchase Details
    occupancy_status                 VARCHAR(30),
    responsible_department           VARCHAR(150),
    purchase_date                    DATE,
    purchase_value                   NUMERIC(16, 2),
    supporting_documents             VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS land_asset_detail (
    asset_code                  VARCHAR(20)   PRIMARY KEY REFERENCES asset (asset_code) ON DELETE CASCADE,

    -- Identification Details
    land_type                      VARCHAR(50)   NOT NULL,
    address_location                VARCHAR(255)  NOT NULL,
    land_area                       NUMERIC(14, 2),
    area_unit                       VARCHAR(20),
    survey_plan_number              VARCHAR(100),
    boundary_description            VARCHAR(500),
    gps_latitude                    NUMERIC(10, 6),
    gps_longitude                   NUMERIC(10, 6),

    -- Ownership and Legal Details
    ownership_type                   VARCHAR(50),
    title_deed_number                VARCHAR(100),
    registration_number              VARCHAR(100),
    encumbrances                     VARCHAR(255),

    -- Valuation Details
    valuation_amount                 NUMERIC(16, 2),
    valuation_date                   DATE,

    -- Purchase Details
    purchase_date                    DATE,
    purchase_value                   NUMERIC(16, 2),
    supporting_documents             VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS other_asset_detail (
    asset_code                  VARCHAR(20)   PRIMARY KEY REFERENCES asset (asset_code) ON DELETE CASCADE,

    -- Identification Details
    item_description                VARCHAR(255)  NOT NULL,
    specification                   VARCHAR(500),
    make                            VARCHAR(100),
    model                           VARCHAR(100),
    manufacturer_serial_number      VARCHAR(100),
    quantity                        INT,
    unit_of_measure                 VARCHAR(50),

    -- Purchase and Ownership Details
    supplier                        VARCHAR(150),
    purchase_date                   DATE,
    purchase_value                  NUMERIC(14, 2),
    warranty_expiry_date            DATE,
    supporting_documents            VARCHAR(255)
);
