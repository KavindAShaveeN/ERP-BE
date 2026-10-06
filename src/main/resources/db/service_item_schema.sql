-- ServiceItem table for the Service item category.
-- Plain JDBC-backed table (see non_stock_item_schema.sql), not managed by
-- Hibernate ddl-auto:update. Run this script manually against the application database
-- before using the /api/service-items/** endpoints.
-- Service items have no unit of measure or quantity/batch tracking, so there is no
-- uom_id/unit_price/current_stock equivalent here.

CREATE TABLE IF NOT EXISTS service_item (
    service_item_id     BIGSERIAL     PRIMARY KEY,
    item_code_id         BIGINT        NOT NULL REFERENCES item_code (item_code_id) ON DELETE RESTRICT,
    is_active            BOOLEAN       NOT NULL DEFAULT TRUE,
    remarks              VARCHAR(500),
    CONSTRAINT service_item_item_code_id_key UNIQUE (item_code_id)
);
