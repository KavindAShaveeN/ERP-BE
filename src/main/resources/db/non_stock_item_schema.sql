-- NonStockItem table for the Non-Stock item category.
-- Plain JDBC-backed table (see item_code_consumable_item_schema.sql), not managed by
-- Hibernate ddl-auto:update. Run this script manually against the application database
-- before using the /api/non-stock-items/** endpoints.
-- Non-stock items are not quantity/batch tracked, so there is no unit_price/current_stock
-- equivalent here (unlike consumable_item).

CREATE TABLE IF NOT EXISTS non_stock_item (
    non_stock_item_id  BIGSERIAL     PRIMARY KEY,
    item_code_id        BIGINT        NOT NULL REFERENCES item_code (id) ON DELETE RESTRICT,
    uom_id               INTEGER       NOT NULL REFERENCES uom (uomid),
    is_active            BOOLEAN       NOT NULL DEFAULT TRUE,
    remarks              VARCHAR(500),
    CONSTRAINT non_stock_item_item_code_id_key UNIQUE (item_code_id)
);
CREATE INDEX IF NOT EXISTS idx_non_stock_item_uom_id ON non_stock_item (uom_id);
