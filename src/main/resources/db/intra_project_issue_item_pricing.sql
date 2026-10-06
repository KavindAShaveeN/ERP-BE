-- Adds unit price / line amount to intra-project issue items, mirroring
-- gin_item_pricing.sql and stock_return_item_pricing.sql.
-- Plain JDBC-backed table (see intra_project_issue_schema.sql), not managed by Hibernate
-- ddl-auto:update. Run this script manually against the application database.

ALTER TABLE intra_project_issue_item
    ADD COLUMN IF NOT EXISTS unit_price NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS amount NUMERIC(14,2);
