-- Moves the expiry date for special items from GIN items to GRN items: the expiry is known when
-- goods are received, not when they are issued. Adds it to grn_item and drops the earlier
-- gin_item column. Plain JDBC-backed tables, not managed by Hibernate ddl-auto:update. Run this
-- script manually against the application database.

ALTER TABLE grn_item
    ADD COLUMN IF NOT EXISTS expiry_date DATE;

ALTER TABLE gin_item
    DROP COLUMN IF EXISTS expiry_date;
