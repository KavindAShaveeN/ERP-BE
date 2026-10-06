-- Item hierarchy tables.
-- These are plain JDBC-backed tables (matching the Supplier/Employee/Project pattern in
-- this codebase), so they are NOT created automatically by Hibernate's ddl-auto:update.
-- Run this script manually against the application database before using the
-- /api/item/** endpoints.
--
-- For a database that already has these tables in the OLDER code-primary-key shape
-- (code as PRIMARY KEY, parent referenced by code), do NOT run this script — run
-- item_migration_v2_id_primary_keys.sql instead, which upgrades in place
-- without losing data. This file is only for a brand new database.

CREATE TABLE IF NOT EXISTS item_category (
    id         BIGSERIAL    PRIMARY KEY,
    code       VARCHAR(50)  NOT NULL,
    name       VARCHAR(150) NOT NULL,
    type       VARCHAR(20)  NOT NULL CHECK (type IN ('Asset', 'Consumable')),
    asset_class VARCHAR(50),
    CONSTRAINT item_category_type_code_key UNIQUE (type, code)
);

CREATE TABLE IF NOT EXISTS item_sub_category (
    id          BIGSERIAL    PRIMARY KEY,
    category_id BIGINT       NOT NULL REFERENCES item_category (id) ON DELETE RESTRICT,
    code        VARCHAR(50)  NOT NULL,
    name        VARCHAR(150) NOT NULL,
    CONSTRAINT item_sub_category_category_id_code_key UNIQUE (category_id, code)
);
CREATE INDEX IF NOT EXISTS idx_item_sub_category_category_id ON item_sub_category (category_id);

CREATE TABLE IF NOT EXISTS item_sub_sub_category (
    id              BIGSERIAL    PRIMARY KEY,
    sub_category_id BIGINT       NOT NULL REFERENCES item_sub_category (id) ON DELETE RESTRICT,
    code            VARCHAR(50)  NOT NULL,
    name            VARCHAR(150) NOT NULL,
    CONSTRAINT item_sub_sub_category_sub_category_id_code_key UNIQUE (sub_category_id, code)
);
CREATE INDEX IF NOT EXISTS idx_item_sub_sub_category_sub_category_id ON item_sub_sub_category (sub_category_id);

CREATE TABLE IF NOT EXISTS item_brand (
    id                  BIGSERIAL    PRIMARY KEY,
    sub_sub_category_id BIGINT       NOT NULL REFERENCES item_sub_sub_category (id) ON DELETE RESTRICT,
    code                VARCHAR(50)  NOT NULL,
    name                VARCHAR(150) NOT NULL,
    CONSTRAINT item_brand_sub_sub_category_id_code_key UNIQUE (sub_sub_category_id, code)
);
CREATE INDEX IF NOT EXISTS idx_item_brand_sub_sub_category_id ON item_brand (sub_sub_category_id);

CREATE TABLE IF NOT EXISTS item_model (
    id       BIGSERIAL    PRIMARY KEY,
    brand_id BIGINT       NOT NULL REFERENCES item_brand (id) ON DELETE RESTRICT,
    code     VARCHAR(50)  NOT NULL,
    name     VARCHAR(150) NOT NULL,
    CONSTRAINT item_model_brand_id_code_key UNIQUE (brand_id, code)
);
CREATE INDEX IF NOT EXISTS idx_item_model_brand_id ON item_model (brand_id);

CREATE TABLE IF NOT EXISTS item_optional_one (
    id       BIGSERIAL    PRIMARY KEY,
    model_id BIGINT       NOT NULL REFERENCES item_model (id) ON DELETE RESTRICT,
    code     VARCHAR(50)  NOT NULL,
    name     VARCHAR(150) NOT NULL,
    CONSTRAINT item_optional_one_model_id_code_key UNIQUE (model_id, code)
);
CREATE INDEX IF NOT EXISTS idx_item_optional_one_model_id ON item_optional_one (model_id);

CREATE TABLE IF NOT EXISTS item_optional_two (
    id              BIGSERIAL    PRIMARY KEY,
    optional_one_id BIGINT       NOT NULL REFERENCES item_optional_one (id) ON DELETE RESTRICT,
    code            VARCHAR(50)  NOT NULL,
    name            VARCHAR(150) NOT NULL,
    CONSTRAINT item_optional_two_optional_one_id_code_key UNIQUE (optional_one_id, code)
);
CREATE INDEX IF NOT EXISTS idx_item_optional_two_optional_one_id ON item_optional_two (optional_one_id);

CREATE TABLE IF NOT EXISTS item_optional_three (
    id              BIGSERIAL    PRIMARY KEY,
    optional_two_id BIGINT       NOT NULL REFERENCES item_optional_two (id) ON DELETE RESTRICT,
    code            VARCHAR(50)  NOT NULL,
    name            VARCHAR(150) NOT NULL,
    CONSTRAINT item_optional_three_optional_two_id_code_key UNIQUE (optional_two_id, code)
);
CREATE INDEX IF NOT EXISTS idx_item_optional_three_optional_two_id ON item_optional_three (optional_two_id);
