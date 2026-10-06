-- Asset registration schema for the remaining asset classes (Furniture, IT Equipment,
-- Plant Equipment, Power Tools, Electrical Equipment, Machinery), following the same
-- pattern as asset_vehicle_schema.sql: each class gets its own *_asset_detail table,
-- 1:1 with the shared "asset" table via asset_code.
--
-- PREREQUISITE: run asset_vehicle_schema.sql first (creates the shared "asset" table).
--
-- Safe to re-run: every statement is guarded (IF NOT EXISTS).

CREATE TABLE IF NOT EXISTS furniture_asset_detail (
    asset_code        VARCHAR(20)   PRIMARY KEY REFERENCES asset (asset_code) ON DELETE CASCADE,
    furniture_type       VARCHAR(100),
    make                  VARCHAR(100),
    model                 VARCHAR(100),
    material              VARCHAR(100),
    colour                VARCHAR(50),
    length                NUMERIC(10, 2),
    width                 NUMERIC(10, 2),
    height                NUMERIC(10, 2),
    supplier              VARCHAR(150),
    invoice_number        VARCHAR(100),
    purchase_date         DATE,
    warranty_period       VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS it_equipment_asset_detail (
    asset_code                    VARCHAR(20)   PRIMARY KEY REFERENCES asset (asset_code) ON DELETE CASCADE,
    equipment_type                  VARCHAR(50)   NOT NULL,
    make                            VARCHAR(100),
    model                           VARCHAR(100),
    manufacturer_serial_number      VARCHAR(100),
    asset_tag                       VARCHAR(100),
    device_name_hostname            VARCHAR(150),
    year_of_manufacture             INT,
    colour                          VARCHAR(50),
    processor_main_specification    VARCHAR(150),
    ram_capacity                    VARCHAR(50),
    storage_capacity                VARCHAR(50),
    operating_system                VARCHAR(100),
    screen_size                     VARCHAR(50),
    network_mac_address             VARCHAR(100),
    ip_address                      VARCHAR(50),
    power_rating_capacity           VARCHAR(50),
    voltage                         NUMERIC(10, 2),
    included_accessories            VARCHAR(500),
    assigned_department             VARCHAR(150),
    assigned_project_location       VARCHAR(150),
    purchase_date                   DATE,
    warranty_expiry_date            DATE,
    supplier                        VARCHAR(150),
    purchase_value                  NUMERIC(14, 2)
);

CREATE TABLE IF NOT EXISTS plant_equipment_asset_detail (
    asset_code                       VARCHAR(20)   PRIMARY KEY REFERENCES asset (asset_code) ON DELETE CASCADE,
    plant_type                         VARCHAR(50)   NOT NULL,
    plant_name                         VARCHAR(150)  NOT NULL,
    plant_code_asset_number            VARCHAR(100),
    make                               VARCHAR(100),
    model                              VARCHAR(100),
    manufacturer                       VARCHAR(150),
    manufacturer_serial_number         VARCHAR(100),
    country_of_manufacture             VARCHAR(100),
    year_of_manufacture                INT,
    plant_configuration                VARCHAR(30),
    production_capacity                NUMERIC(14, 2),
    capacity_unit                      VARCHAR(50),
    main_power_source                  VARCHAR(100),
    fuel_type                          VARCHAR(255),
    assigned_project                   VARCHAR(150),
    installation_date                  DATE,
    commissioning_date                 DATE,
    purchase_date                      DATE,
    supplier                           VARCHAR(150),
    purchase_value                     NUMERIC(14, 2),
    warranty_expiry_date               DATE,
    responsible_employee_department    VARCHAR(150),
    gps_latitude                       NUMERIC(10, 6),
    gps_longitude                      NUMERIC(10, 6),
    plant_images                       VARCHAR(255),
    supporting_documents               VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS power_tool_asset_detail (
    asset_code                       VARCHAR(20)   PRIMARY KEY REFERENCES asset (asset_code) ON DELETE CASCADE,

    -- Tool Details
    tool_type                          VARCHAR(50)   NOT NULL,
    make                               VARCHAR(100),
    model                              VARCHAR(100),
    manufacturer_serial_number         VARCHAR(100),
    country_of_manufacture             VARCHAR(100),
    year_of_manufacture                INT,
    power_source                       VARCHAR(20)   NOT NULL,
    intended_use                       VARCHAR(255),

    -- Technical Specifications
    rated_power                        NUMERIC(10, 2),
    voltage                            NUMERIC(10, 2),
    current_amps                       NUMERIC(10, 2),
    frequency                          NUMERIC(10, 2),
    speed_rpm                          NUMERIC(10, 2),
    capacity_tool_size                 VARCHAR(50),
    capacity_unit                      VARCHAR(50),
    chuck_disc_blade_size              VARCHAR(50),
    weight_kg                          NUMERIC(10, 2),

    -- Battery Details
    battery_type                       VARCHAR(50),
    battery_voltage                    NUMERIC(10, 2),
    battery_capacity_ah                NUMERIC(10, 2),
    number_of_batteries                INT,
    charger_model_serial_number        VARCHAR(100),

    -- Fuel Details
    fuel_type                          VARCHAR(255),
    engine_capacity_cc                 INT,
    fuel_tank_capacity_l               NUMERIC(10, 2),
    engine_number                      VARCHAR(100),

    -- Safety and Accessories
    safety_class_protection_rating     VARCHAR(100),
    included_accessories               VARCHAR(500),
    carrying_case_number               VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS electrical_equipment_asset_detail (
    asset_code                  VARCHAR(20)   PRIMARY KEY REFERENCES asset (asset_code) ON DELETE CASCADE,

    -- Identification Details
    equipment_type                VARCHAR(50)   NOT NULL,
    make                          VARCHAR(100),
    model                         VARCHAR(100),
    manufacturer_serial_number    VARCHAR(100),
    country_of_manufacture        VARCHAR(100),
    year_of_manufacture           INT,
    installation_type             VARCHAR(20),

    -- Electrical Specifications
    rated_power                   NUMERIC(10, 2),
    horsepower                    NUMERIC(10, 2),
    rated_voltage                 NUMERIC(10, 2),
    rated_current                 NUMERIC(10, 2),
    frequency                     NUMERIC(10, 2),
    number_of_phases              VARCHAR(20),
    speed_rpm                     NUMERIC(10, 2),
    power_factor                  NUMERIC(6, 3),
    efficiency_class              VARCHAR(50),
    insulation_class              VARCHAR(50),
    protection_rating             VARCHAR(50),
    connection_type               VARCHAR(100),
    duty_type                     VARCHAR(50),

    -- Equipment-Specific Specifications
    equipment_capacity            VARCHAR(50),
    capacity_unit                 VARCHAR(50),
    input_rating                  VARCHAR(50),
    output_rating                 VARCHAR(50),
    pressure_rating                VARCHAR(50),
    flow_rate                     VARCHAR(50),
    cooling_method                VARCHAR(50),
    transformer_rating_kva        NUMERIC(10, 2),
    generator_rating_kva          NUMERIC(10, 2),
    pump_head_m                   NUMERIC(10, 2),

    -- Installation Details
    installation_date             DATE,
    installation_location         VARCHAR(150),
    panel_circuit_reference       VARCHAR(100),
    connected_load                VARCHAR(100),
    responsible_department        VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS machinery_asset_detail (
    asset_code                  VARCHAR(20)   PRIMARY KEY REFERENCES asset (asset_code) ON DELETE CASCADE,

    -- Identification and Manufacturer Details
    machinery_type                VARCHAR(50)   NOT NULL,
    make                          VARCHAR(100),
    model                         VARCHAR(100),
    country_of_manufacture        VARCHAR(100),
    year_of_manufacture           INT,
    manufacturer_serial_number    VARCHAR(100),
    chassis_frame_number          VARCHAR(100),
    registration_number           VARCHAR(50),
    colour                        VARCHAR(50),

    -- Engine Details
    engine_number                 VARCHAR(100),
    engine_make                   VARCHAR(100),
    engine_model                  VARCHAR(100),
    fuel_type                     VARCHAR(255),
    engine_capacity_cc            INT,
    number_of_cylinders           INT,
    engine_power_hp               NUMERIC(10, 2),
    engine_power_kw               NUMERIC(10, 2),
    rated_rpm                     NUMERIC(10, 2),
    fuel_tank_capacity_l          NUMERIC(10, 2),
    average_fuel_consumption      NUMERIC(10, 2),
    fuel_consumption_unit         VARCHAR(20),

    -- Operational Details
    meter_type                    VARCHAR(30),
    initial_meter_reading         VARCHAR(50),
    operating_capacity            VARCHAR(50),
    operating_capacity_unit       VARCHAR(50),
    machine_weight_kg             NUMERIC(10, 2),
    load_lift_capacity            VARCHAR(50),
    bucket_blade_capacity         VARCHAR(50),

    -- Tyre or Track Details
    running_system                VARCHAR(20),
    number_of_tyres               INT,
    front_tyre_size               VARCHAR(50),
    rear_tyre_size                VARCHAR(50),
    track_size_width              VARCHAR(50),
    tyre_track_brand              VARCHAR(100),

    -- Battery Details
    battery_brand                 VARCHAR(100),
    battery_model                 VARCHAR(100),
    battery_voltage               NUMERIC(10, 2),
    battery_capacity_ah           NUMERIC(10, 2),
    number_of_batteries           INT
);
