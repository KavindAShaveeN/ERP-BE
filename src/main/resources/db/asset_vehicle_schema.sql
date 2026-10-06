-- Asset registration schema: a generic "asset" table for fields common to every
-- asset class, plus one class-specific detail table per asset class. Only the
-- Vehicle class is implemented so far (vehicle_asset_detail); other classes
-- (PlantEquipment, Machinery, ...) get their own *_asset_detail table the same
-- way when they're implemented.
--
-- PREREQUISITE: this assumes the "asset_code" table already exists with the
-- item_category_id / item_subcategory_id / asset_code_id schema (see
-- item_migration_v2_id_primary_keys.sql and AssetCode.java) — asset.asset_code_id
-- references it. Run this only after that table is in place.
--
-- Safe to re-run: every statement is guarded (IF NOT EXISTS).

CREATE TABLE IF NOT EXISTS asset (
    asset_code            VARCHAR(20)   PRIMARY KEY,
    asset_code_id           BIGINT        NOT NULL REFERENCES asset_code (asset_code_id) ON DELETE RESTRICT,
    asset_class             VARCHAR(50)   NOT NULL,
    description              VARCHAR(255),
    serial_number            VARCHAR(100),
    current_location         VARCHAR(150)  NOT NULL,
    assigned_operator        VARCHAR(150),
    project_or_department    VARCHAR(150),
    ownership_type           VARCHAR(20),
    status                   VARCHAR(30)   NOT NULL,
    condition                VARCHAR(20),
    remarks                  VARCHAR(500),
    document_name            VARCHAR(255),
    created_at               TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at               TIMESTAMP     NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_asset_asset_code_id ON asset (asset_code_id);
CREATE INDEX IF NOT EXISTS idx_asset_asset_class    ON asset (asset_class);

CREATE TABLE IF NOT EXISTS vehicle_asset_detail (
    asset_code                 VARCHAR(20)   PRIMARY KEY REFERENCES asset (asset_code) ON DELETE CASCADE,

    -- Basic Asset Details
    vehicle_class                VARCHAR(50)   NOT NULL,
    registration_number          VARCHAR(50)   NOT NULL,
    year_of_manufacture          INT           NOT NULL,
    colour                       VARCHAR(50),
    weight_kg                    NUMERIC(12, 2),

    -- Manufacturer Details
    make                         VARCHAR(100)  NOT NULL,
    model                        VARCHAR(100)  NOT NULL,
    country_of_manufacture       VARCHAR(100),
    manufacturer_serial_number   VARCHAR(100),
    chassis_number               VARCHAR(100)  NOT NULL,
    body_type                    VARCHAR(50),
    seating_loading_capacity     VARCHAR(50),

    -- Engine Details
    engine_number                VARCHAR(100)  NOT NULL,
    engine_make                  VARCHAR(100),
    engine_model                 VARCHAR(100),
    engine_capacity_cc           INT,
    fuel_type                    VARCHAR(255)  NOT NULL,
    number_of_cylinders          INT,
    fuel_tank_capacity_l         NUMERIC(10, 2),
    average_fuel_consumption     NUMERIC(10, 2),
    fuel_consumption_unit        VARCHAR(20),

    -- Power Details
    horsepower                   NUMERIC(10, 2),
    power_kw                     NUMERIC(10, 2),
    voltage                      NUMERIC(10, 2),
    current_amps                 NUMERIC(10, 2),
    rpm                          INT,

    -- Tyre Details
    number_of_tyres              INT,
    front_tyre_size              VARCHAR(50),
    rear_tyre_size                VARCHAR(50),
    spare_tyre_size               VARCHAR(50),
    tyre_make_brand               VARCHAR(100),

    -- Battery Details
    battery_make_brand           VARCHAR(100),
    battery_model                VARCHAR(100),
    battery_serial_number        VARCHAR(100),
    battery_voltage               NUMERIC(10, 2),
    battery_capacity_ah          NUMERIC(10, 2),
    number_of_batteries          INT,

    -- Purchase and Ownership Details
    supplier                     VARCHAR(150),
    purchase_order_number        VARCHAR(100),
    invoice_receipt_number       VARCHAR(100),
    purchase_date                DATE,
    purchase_value               NUMERIC(14, 2),
    currency                     VARCHAR(10),
    received_date                 DATE,
    warranty_expiry_date          DATE,

    -- Registration and Insurance Details
    registration_date             DATE,
    registration_expiry_date      DATE,
    registration_document         VARCHAR(255),
    insurance_company              VARCHAR(150),
    insurance_policy_number        VARCHAR(100),
    insurance_start_date           DATE,
    insurance_expiry_date          DATE,
    revenue_licence_number         VARCHAR(100),
    revenue_licence_start_date     DATE,
    revenue_licence_expiry_date    DATE,
    emission_test_start_date       DATE,
    emission_test_expiry_date      DATE,
    insurance_licence_documents    VARCHAR(255),

    -- Assignment and Location
    assigned_date                 DATE,
    owning_department             VARCHAR(150),
    cost_centre_account_code      VARCHAR(100),
    account_description           VARCHAR(255),

    -- Documents and Images
    vehicle_photograph            VARCHAR(255),
    registration_certificate      VARCHAR(255),
    insurance_document             VARCHAR(255),
    revenue_licence_document       VARCHAR(255),
    purchase_invoice_document      VARCHAR(255),
    warranty_document              VARCHAR(255),
    other_supporting_documents     VARCHAR(255)
);
