-- ItemCode and ConsumableItem tables.
-- Plain JDBC-backed tables (matching the code_master_*/Supplier/Employee/Project pattern
-- in this codebase), so they are NOT created automatically by Hibernate's ddl-auto:update.
-- Run this script manually against the application database before using the
-- /api/item-codes/** and /api/consumable-items/** endpoints.
--
-- The foreign keys below reference the code_master_* tables because that is what the
-- existing repository classes (ItemCategoryRepository, ItemBrandRepository, etc.) actually
-- query today. If item_migration_v2_id_primary_keys.sql is ever run to rename those
-- tables to item_*, the REFERENCES clauses here must be updated to match.

CREATE TABLE IF NOT EXISTS item_code (
    id                   BIGSERIAL     PRIMARY KEY,
    code                 VARCHAR(50)   NOT NULL,
    name                 VARCHAR(150)  NOT NULL,
    category_id          BIGINT        NOT NULL REFERENCES code_master_category (id) ON DELETE RESTRICT,
    sub_category_id      BIGINT        NOT NULL REFERENCES code_master_sub_category (id) ON DELETE RESTRICT,
    sub_sub_category_id  BIGINT        NOT NULL REFERENCES code_master_sub_sub_category (id) ON DELETE RESTRICT,
    brand_id             BIGINT        NOT NULL REFERENCES code_master_brand (id) ON DELETE RESTRICT,
    model_id             BIGINT        NOT NULL REFERENCES code_master_model (id) ON DELETE RESTRICT,
    optional_one_id      BIGINT        REFERENCES code_master_optional_one (id) ON DELETE RESTRICT,
    optional_two_id      BIGINT        REFERENCES code_master_optional_two (id) ON DELETE RESTRICT,
    optional_three_id    BIGINT        REFERENCES code_master_optional_three (id) ON DELETE RESTRICT,
    CONSTRAINT item_code_code_key UNIQUE (code)
);
CREATE INDEX IF NOT EXISTS idx_item_code_category_id ON item_code (category_id);
CREATE INDEX IF NOT EXISTS idx_item_code_sub_category_id ON item_code (sub_category_id);
CREATE INDEX IF NOT EXISTS idx_item_code_sub_sub_category_id ON item_code (sub_sub_category_id);
CREATE INDEX IF NOT EXISTS idx_item_code_brand_id ON item_code (brand_id);
CREATE INDEX IF NOT EXISTS idx_item_code_model_id ON item_code (model_id);
CREATE INDEX IF NOT EXISTS idx_item_code_optional_one_id ON item_code (optional_one_id);
CREATE INDEX IF NOT EXISTS idx_item_code_optional_two_id ON item_code (optional_two_id);
CREATE INDEX IF NOT EXISTS idx_item_code_optional_three_id ON item_code (optional_three_id);

CREATE TABLE IF NOT EXISTS consumable_item (
    id                BIGSERIAL      PRIMARY KEY,
    item_code_id      BIGINT         NOT NULL REFERENCES item_code (id) ON DELETE RESTRICT,
    uom_id            INTEGER        NOT NULL REFERENCES uom (uomid),
    unit_price        NUMERIC(12,2)  NOT NULL,
    minimal_quantity  INTEGER        NOT NULL,
    current_stock     INTEGER        NOT NULL DEFAULT 0,
    CONSTRAINT consumable_item_item_code_id_key UNIQUE (item_code_id)
);
CREATE INDEX IF NOT EXISTS idx_consumable_item_uom_id ON consumable_item (uom_id);
