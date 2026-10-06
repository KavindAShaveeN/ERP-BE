-- Adds unit price / line amount to GIN items, so the value of stock issued is
-- recorded on the GIN itself (mirroring po_item / grn_item) instead of only
-- being derivable from today's item-master price.
-- Plain JDBC-backed table (see gin/gin_item creation), not managed by Hibernate
-- ddl-auto:update. Run this script manually against the application database.

ALTER TABLE gin_item
    ADD COLUMN IF NOT EXISTS unit_price NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS amount NUMERIC(14,2);
